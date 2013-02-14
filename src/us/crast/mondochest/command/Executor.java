package us.crast.mondochest.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.crast.mondochest.MessageWithStatus;
import us.crast.mondochest.MondoChest;
import us.crast.mondochest.MondoConstants;
import us.crast.mondochest.MondoListener;
import us.crast.mondochest.MondoMessage;
import us.crast.mondochest.Status;
import us.crast.mondochest.util.ChatMagic;

public class Executor implements CommandExecutor {
	
	private MondoListener listener;
	private MondoChest mondoChest;
	
	public java.util.Map<String, SubCommand> subcommands = new LinkedHashMap<String, SubCommand>();


	public Executor(MondoChest mondoChest, MondoListener listener) {
		this.mondoChest = mondoChest;
		this.listener = listener;
		setupCommands();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		String slash = "";
		if (sender instanceof Player) {
			player = (Player) sender;
			slash = "/";
		}
		
		if (args.length == 0) {
			sender.sendMessage("Usage: " + slash +  commandLabel + " <command> [<args>]");

			for (SubCommand sub: availableCommands(sender, player)) {
				String usage = "";
				if (sub.getUsage() != null) {
					usage = ChatMagic.colorize(" {LIGHT_PURPLE}%s", sub.getUsage());
				}
				sender.sendMessage(ChatMagic.colorize(
						"{GREEN}%s%s %s%s {BLUE}%s", 
						slash, commandLabel, 
						sub.getName(),
						usage,
						sub.getDescription()
				));
			}
			return false;
		}
		String subcommandName = args[0].toLowerCase();
		SubCommand sub = subcommands.get(subcommandName);
		if (sub == null) {
			// TODO return usage
			return false;
		} else if (!sub.getChecker().checkSender(sender)) {
			sender.sendMessage(ChatMagic.colorize("{GOLD}MondoChest: {WARNING}Stop being sneaky."));
			return false;
		} else if ((args.length -1 ) < sub.getMinArgs()) {
			sender.sendMessage(ChatMagic.colorize("{GOLD}Usage: {GREEN}%s%s %s {USAGE}%s", slash, commandLabel, sub.getName(), sub.getUsage()));
			return false;
		}
		CallInfo call = new CallInfo(sender, player, args);
		try {
			sub.getHandler().handle(call);
		} catch (MondoMessage m) {
			call.append(m);
		}
		for (MessageWithStatus m: call.getMessages()) {
			sender.sendMessage(BasicMessage.render(m, true));
		}
		return false;
	}
	
	private SubCommand addSub(String name, String permission, SubHandler handler) {
		SubCommand cmd = new SubCommand(name, permission, handler);
		subcommands.put(name, cmd);
		return cmd;
	}
	
	private SubCommand addSub(String name, String permission) {
		return addSub(name, permission, null);
	}
	
	private List<SubCommand> availableCommands(CommandSender sender, Player player) {
		ArrayList<SubCommand> items = new ArrayList<SubCommand>();
		boolean has_player = (player != null);
		for (SubCommand sub: subcommands.values()) {
			if ((has_player || sub.isConsoleAllowed())
					&& sub.getChecker().checkSender(sender)) {
				items.add(sub);
			}
		}
		return items;
	}
	
	private void setupCommands() {
		addSub("remove", "mondochest.remove_slave", new SubHandler(){
			public void handle(CallInfo call) {
				// TODO
			}
		});
		
		addSub("access", "mondochest.use")
		    .setDescription("Manage access to a MondoChest")
		    .setHandler(new SubHandler() {
                public void handle(CallInfo call) throws MondoMessage {
                    listener.manageAccess(call, call.getPlayer());
                }
		    });
		
		addSub("reload", "mondochest.admin.reload")
			.allowConsole()
			.setDescription("Reload MondoChest Config")
			.setHandler(new SubHandler() {
				public void handle(CallInfo call) {
					mondoChest.reloadMondoChest();
					call.success("MondoChest config reloaded");
				}	
			});
		
		addSub("find", "mondochest.find")
		    .setMinArgs(1)
		    .setUsage("<item name>")
		    .setDescription("Find how much of an item you have.")
		    .setHandler(new SubHandler() {
                @Override
                public void handle(CallInfo call) throws MondoMessage {
                    listener.findItems(call, call.getPlayer());
                }
		    });
		
		addSub("version", "mondochest.admin.console")
		    .allowConsole()
		    .setDescription("Version Info")
		    .setHandler(new SubHandler() {
                public void handle(CallInfo call) throws MondoMessage {
                    call.append(new BasicMessage(Status.SUCCESS, "%s version %s", MondoConstants.APP_NAME, MondoConstants.MONDOCHEST_VERSION));
                }    
		    });
	}

}
