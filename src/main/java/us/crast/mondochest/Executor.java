package us.crast.mondochest;


import mondocommand.CallInfo;
import mondocommand.FormatConfig;
import mondocommand.MondoCommand;
import mondocommand.SubHandler;

import us.crast.chatmagic.BasicMessage;
import us.crast.chatmagic.MondoMessage;
import us.crast.chatmagic.Status;

public class Executor extends MondoCommand {
	private MondoListener listener;
	private MondoChest mondoChest;
	private static FormatConfig formatter = new FormatConfig()
		.setReplyPrefix("{GOLD}MondoChest: ");

	public Executor(MondoChest mondoChest, MondoListener listener) {
		super(formatter);
		this.mondoChest = mondoChest;
		this.listener = listener;
		setupCommands();
	}

	private void setupCommands() {
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
					call.reply("MondoChest config reloaded");
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
        if (MondoConfig.SIGNLESS_SLAVES) {
            addSub("add_slave", "mondochest.add_signless_slave")
                .setDescription("Add a slave without a sign")
                .setHandler(new SubHandler() {
                    public void handle(CallInfo call) throws MondoMessage {
                        listener.addSignlessSlave(call, call.getPlayer());
                    }
                });
        }
		addSub("version", "mondochest.admin.console")
			.allowConsole()
			.setDescription("Version Info")
			.setHandler(new SubHandler() {
				public void handle(CallInfo call) {
					call.reply("{SUCCESS}%s version %s", MondoConstants.APP_NAME, MondoConstants.MONDOCHEST_VERSION);
				}
			});

		addSub("check", "mondochest.admin.check")
		  .allowConsole()
		  .setDescription("Check/cleanup missing/broken chest data.")
		  .setHandler(new SubHandler() {
			  public void handle(CallInfo call) {
				  new CheckerTask(mondoChest, call.getSender()).run();
			  }
		  });
	}

}
