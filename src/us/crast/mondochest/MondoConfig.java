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
    public static boolean PROTECTION_CHEST_OPEN = false;
	public static boolean SLAVE_VERTICAL_TWO = false;
    public static boolean SLAVE_HORIZONTAL_TWO = false;

	
	public static int SLAVE_MAX_ADD_RADIUS = 150;
	public static int FIND_MAX_RADIUS = 300;
	public static int SLAVES_PER_MASTER = -1;

	public static Permission VAULT_PERMISSIONS = null;
	public static boolean USE_COMMANDS = true;

	private static List<String> decodeErrors = null;

	
	public static void configure(MondoChest plugin, FileConfiguration config, Logger log) {
		MondoConfig.log = log;
		SLAVE_MAX_ADD_RADIUS = getLimit(config, "limits.slaves.max_add_radius");
		FIND_MAX_RADIUS = getLimit(config, "limits.find_max_radius");
		SLAVES_PER_MASTER = getLimit(config, "limits.slaves.per_master");
		RESTACK_MASTER = config.getBoolean("restack_master");
		RESTACK_SLAVES = config.getBoolean("restack_slaves");
		PROTECTION_SIGNS = config.getBoolean("protection.signs");
		PROTECTION_CHEST_BREAK = config.getBoolean("protection.chest_break");
		PROTECTION_CHEST_OPEN = config.getBoolean("protection.chest_open");
		USE_COMMANDS = config.getBoolean("use_commands", true);
		SLAVE_VERTICAL_TWO = config.getBoolean("special.slave_vertical_two");
		SLAVE_HORIZONTAL_TWO = config.getBoolean("special.slave_horizontal_two");
		java.util.List<String> matlist = config.getStringList("restack_materials");
		RESTACK_MATERIALS = new Material[matlist.size()];
		for (int i = 0; i < matlist.size(); i++) {
			RESTACK_MATERIALS[i] = Material.matchMaterial(matlist.get(i));
		}

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
	
	private static int getLimit(FileConfiguration config, String path) {
	    String val = config.getString(path);
	    if (val == null) {
	        return -1;
	    } else if (val.equals("unlimited") || val.equals("inf")) {
	        return -1;
	    } else {
	        return Integer.parseInt(val);
	    }
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
