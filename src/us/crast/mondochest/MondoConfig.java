package us.crast.mondochest;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class MondoConfig {
	public static boolean RESTACK_MASTER = false;
	public static boolean RESTACK_SLAVES = false;
	public static Material[] RESTACK_MATERIALS = {};
	
	public static void configure(FileConfiguration config, Logger log) {
		RESTACK_MASTER = config.getBoolean("restack_master");
		RESTACK_SLAVES = config.getBoolean("restack_slaves");
		java.util.List<String> matlist = config.getStringList("restack_materials");
		RESTACK_MATERIALS = new Material[matlist.size()];
		for (int i = 0; i < matlist.size(); i++) {
			RESTACK_MATERIALS[i] = Material.matchMaterial(matlist.get(i));
		}
	}
}
