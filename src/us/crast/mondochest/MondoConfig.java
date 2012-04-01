package us.crast.mondochest;

import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

import us.crast.mondochest.security.MondoSecurity;

public final class MondoConfig {
	private static Logger log = Logger.getLogger("Minecraft");

	public static boolean RESTACK_MASTER = false;
	public static boolean RESTACK_SLAVES = false;
	
	public static Material[] RESTACK_MATERIALS = {};
	
	public static boolean PROTECTION_SIGNS = false;
	public static boolean PROTECTION_CHEST_BREAK = false;
	
	public static int CONSTRAINTS_Y_MAX = 127;
	public static int CONSTRAINTS_Y_MIN = 0;
	public static int SLAVE_MAX_ADD_RADIUS = 150;

	public static Permission VAULT_PERMISSIONS = null;
	public static boolean USE_COMMANDS = false;

	private static List<String> decodeErrors = null;
	
	public static void configure(MondoChest plugin, FileConfiguration config, Logger log) {
		MondoConfig.log = log;
		RESTACK_MASTER = config.getBoolean("restack_master");
		RESTACK_SLAVES = config.getBoolean("restack_slaves");
		PROTECTION_SIGNS = config.getBoolean("protection.signs");
		PROTECTION_CHEST_BREAK = config.getBoolean("protection.chest_break");
		USE_COMMANDS = config.getBoolean("use_commands", true);
		java.util.List<String> matlist = config.getStringList("restack_materials");
		RESTACK_MATERIALS = new Material[matlist.size()];
		for (int i = 0; i < matlist.size(); i++) {
			RESTACK_MATERIALS[i] = Material.matchMaterial(matlist.get(i));
		}
		
		CONSTRAINTS_Y_MAX = config.getInt("world_constraints.Ymax");
		CONSTRAINTS_Y_MIN = config.getInt("world_constraints.Ymin");
		MondoSecurity.setMode("null");
		String perms_config = config.getString("permissions").toLowerCase();
		if (perms_config.equals("superperms") || perms_config.equals("true")) {
			MondoSecurity.setMode("SuperPerms");
		} else if (perms_config.equals("vault")) {
			if (loadVaultPermissions(plugin) == null) {
				log.warning("Permission system Vault requested, but vault permission system not found.");
			} else {
				MondoSecurity.setMode("Vault");
			}
		} else if (perms_config.equals("false") || perms_config.equals("none")) {
			MondoSecurity.setMode("null");
		} else {
			log.warning(String.format(
				"Do not know permissions scheme '%s', must be one of "
				+ "none, SuperPerms or Vault.",
				perms_config
			));
		}
	}
	
	private static Permission loadVaultPermissions(MondoChest plugin) {
    	if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
        	VAULT_PERMISSIONS = permissionProvider.getProvider();
            return VAULT_PERMISSIONS;
        }
        return null;
    }

	public static Logger getLog() {
		return log;
	}
	
	public static Logger setLog(Logger newLogger) {
		log = newLogger;
		return newLogger;
	}

	public static List<String> getDecodeErrors() {
		return decodeErrors;
	}
	
	public static void clearDecodeErrors() {
		decodeErrors = null;
	}

	public static void logDecodeError(String error) {
		if (decodeErrors == null) decodeErrors = new java.util.ArrayList<String>();
		log.warning(error);
		decodeErrors.add(error);
	}
}
