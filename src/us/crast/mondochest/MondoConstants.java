package us.crast.mondochest;

public final class MondoConstants {
	public static final String APP_NAME = "MondoChest";
	public static final String MONDOCHEST_VERSION = "0.5.6-pre1";
	
	public static final String MASTER_SIGN_NAME = "[MondoChest]";
	public static final String SLAVE_SIGN_NAME = "[MondoSlave]";
	public static final String RELOAD_SIGN_NAME = "[MondoReload]";
	
	public static final int DOUBLE_CHEST_SIZE = 54;
	public static final int SINGLE_CHEST_SIZE = 27;

	public static final String[] BAD_CHEST_WARNING = {
		"Detected a chest not one of the known chest sizes.",
		"This suggests that Minecraft has changed something vital, ",
		"(maybe you're running MondoChest against the wrong version?)",
		"make sure that MondoChest supports the game version you are using."
	};
}
