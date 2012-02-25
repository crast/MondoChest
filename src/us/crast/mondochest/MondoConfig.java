package us.crast.mondochest;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class MondoConfig {
	public static boolean RESTACK_MASTER = false;
	public static boolean RESTACK_SLAVES = false;
	public static Material[] RESTACK_MATERIALS = {};
	
	public static int CONSTRAINTS_Y_MAX = 127;
	public static int CONSTRAINTS_Y_MIN = 0;
	
	public static boolean USE_PERMISSIONS = false;
	
	public static void configure(FileConfiguration config, Logger log) {
		RESTACK_MASTER = config.getBoolean("restack_master");
		RESTACK_SLAVES = config.getBoolean("restack_slaves");
		java.util.List<String> matlist = config.getStringList("restack_materials");
		RESTACK_MATERIALS = new Material[matlist.size()];
		for (int i = 0; i < matlist.size(); i++) {
			RESTACK_MATERIALS[i] = Material.matchMaterial(matlist.get(i));
		}
		
		CONSTRAINTS_Y_MAX = config.getInt("world_constraints.Ymax");
		CONSTRAINTS_Y_MIN = config.getInt("world_constraints.Ymin");
		
		String perms_config = config.getString("permissions").toLowerCase();
		if (perms_config.equals("superperms") || perms_config.equals("true")) {
			USE_PERMISSIONS = true;
		} else if (!perms_config.equals("false") && !perms_config.equals("none")) {
			log.warning(String.format(
				"Do not know permissions scheme '%s', only supported scheme is SuperPerms at the moment.",
				perms_config
			));
		}
	}
}
