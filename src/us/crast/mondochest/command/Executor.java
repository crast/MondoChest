package us.crast.mondochest.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.crast.mondochest.MondoChest;
import us.crast.mondochest.MondoConstants;
import us.crast.mondochest.MondoListener;
import us.crast.mondochest.MondoMessage;
import us.crast.mondochest.security.MondoSecurity;
import us.crast.mondochest.security.PermissionChecker;

public class Executor implements CommandExecutor {
	private PermissionChecker can_reload;
	
	private MondoListener listener;
	private MondoChest mondoChest;
	
	public java.util.Map<String, SubCommand> subcommands = new HashMap<String, SubCommand>();


	public Executor(MondoChest mondoChest, MondoListener listener) {
		this.mondoChest = mondoChest;
		this.listener = listener;
		this.can_reload = MondoSecurity.getChecker("mondochest.admin.reload");
		setupCommands();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (args.length == 0) {
			if (player == null) {
				return sendVersion(sender);
			} else {
				for (SubCommand sub: availableCommands(sender, player)) {
					player.sendMessage(String.format(" %s: %s", sub.getName(), sub.getDescription()));
				}
				return false;
			}
		}
		String subcommandName = args[0].toLowerCase();
		SubCommand sub = subcommands.get(subcommandName);
		if (sub == null) {
			// TODO return usage
			return false;
		}
		CallInfo call = new CallInfo(sender, player, args);
		try {
			sub.getHandler().handle(call);
		} catch (MondoMessage m) {
			player.sendMessage(m.getMessage());
		}
		return false;
	}
	
	private boolean sendVersion(CommandSender sender) {
		sender.sendMessage(String.format("%s version %s", MondoConstants.APP_NAME, MondoConstants.MONDOCHEST_VERSION));
		return true;
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
		
		addSub("allow", "mondochest.use")
			.setMinArgs(1)
			.setUsage("<username>")
			.setDescription("Allow other users to access a MondoChest")
			.setHandler(new SubHandler() {
				public void handle(CallInfo call) throws MondoMessage {
					listener.allowAccess(call, call.getArg(1));
				}
			});
		
		addSub("deny", "mondochest.use")
			.setMinArgs(1)
			.setUsage("<username>")
			.setDescription("Remove users from access")
			.setHandler(subcommands.get("allow").getHandler());
		
		addSub("reload", "mondochest.admin.reload")
			.allowConsole()
			.setDescription("Reload MondoChest Config")
			.setHandler(new SubHandler() {
				public void handle(CallInfo call) {
					mondoChest.reloadMondoChest();
				}	
			});
	}

}
