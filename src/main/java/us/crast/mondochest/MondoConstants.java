package us.crast.mondochest;

import org.bukkit.Material;

import us.crast.mondochest.util.MaterialSet;

import mondocommand.ChatMagic;

public final class MondoConstants {
	public static final String APP_NAME = "MondoChest";
	public static final String MONDOCHEST_VERSION = "0.7.1";

	public static final int DOUBLE_CHEST_SIZE = 54;
	public static final int SINGLE_CHEST_SIZE = 27;

	public static final String[] BAD_CHEST_WARNING = {
		"Detected a chest not one of the known chest sizes.",
		"This suggests that Minecraft has changed something vital, ",
		"(maybe you're running MondoChest against the wrong version?)",
		"make sure that MondoChest supports the game version you are using."
	};
    public static final String ROLE_NONE = "none";
    public static final String ROLE_USER = "user";
    public static final String ACL_ENABLED_MESSAGE = ChatMagic.colorize(
        "MondoChest 0.7 has changed ACL configuration, you must make sure to"
        + " set the {GREEN}acl_fallback_role{ERROR} variable or you will not"
        + " be allowed to use ACL commands until it's set."
    );

    public static final MaterialSet CHEST_MATERIALS = new MaterialSet(Material.CHEST, Material.TRAPPED_CHEST);
    public static final MaterialSet DISPENSER_MATERIALS = new MaterialSet(Material.DISPENSER);
}
