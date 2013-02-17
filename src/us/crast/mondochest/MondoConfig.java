package us.crast.mondochest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import us.crast.mondochest.security.MondoSecurity;

public final class MondoConfig {
	private static Logger log = Logger.getLogger("Minecraft");

	public static boolean RESTACK_MASTER = false;
	public static boolean RESTACK_SLAVES = false;
	public static boolean CASE_INSENSITIVE_SIGNS = false;
	
	public static Material[] RESTACK_MATERIALS = {};
	
	public static boolean PROTECTION_SIGNS = false;
	public static boolean PROTECTION_CHEST_BREAK = false;
    public static boolean PROTECTION_CHEST_OPEN = false;
	public static boolean SLAVE_VERTICAL_TWO = false;
    public static boolean SLAVE_HORIZONTAL_TWO = false;

	public static Permission VAULT_PERMISSIONS = null;
	public static boolean USE_COMMANDS = true;
	public static String FALLBACK_ROLE = MondoConstants.ROLE_NONE;
    public static boolean ACL_ENABLED = true;
    private static Limits GLOBAL_LIMITS = null;
    private static List<Limits> groupLimits = null;

	private static List<String> decodeErrors = null;

	
	public static void configure(MondoChest plugin, FileConfiguration config, Logger log) {
		MondoConfig.log = log;
		RESTACK_MASTER = config.getBoolean("restack_master");
		RESTACK_SLAVES = config.getBoolean("restack_slaves");
		PROTECTION_SIGNS = config.getBoolean("protection.signs");
		PROTECTION_CHEST_BREAK = config.getBoolean("protection.chest_break");
		PROTECTION_CHEST_OPEN = config.getBoolean("protection.chest_open");
		USE_COMMANDS = config.getBoolean("use_commands", true);
		SLAVE_VERTICAL_TWO = config.getBoolean("special.slave_vertical_two");
		SLAVE_HORIZONTAL_TWO = config.getBoolean("special.slave_horizontal_two");
		CASE_INSENSITIVE_SIGNS = config.getBoolean("special.case_insensitive_signs");
		java.util.List<String> matlist = config.getStringList("restack_materials");
		RESTACK_MATERIALS = new Material[matlist.size()];
		for (int i = 0; i < matlist.size(); i++) {
			RESTACK_MATERIALS[i] = Material.matchMaterial(matlist.get(i));
		}

		// Permissions Config
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
	    FALLBACK_ROLE = config.getString("acl_fallback_role").toLowerCase();
	    if (FALLBACK_ROLE == null || "configure_me".equals(FALLBACK_ROLE)) {
	        FALLBACK_ROLE = MondoConstants.ROLE_NONE;
	        ACL_ENABLED = false;
	    }
	    GLOBAL_LIMITS = new Limits(config.getConfigurationSection("limits"), null);
	    configureGroupLimits(config.getConfigurationSection("group_limits"));
	}
	
	private static void configureGroupLimits(ConfigurationSection s) {
	    groupLimits = new ArrayList<Limits>();
        if (s == null) return;
        for (Map.Entry<String, Object> e : s.getValues(false).entrySet()) {
            if (s.isConfigurationSection(e.getKey())) {
                groupLimits.add(
                    new Limits((ConfigurationSection) e.getValue(), GLOBAL_LIMITS)
                );
            }
        }
        
    }

    public static void setupRoles() {
        /** Create roles hard-coded for now*/
        Role.create("none");
        
        Role.create("deposit")
            .grantShelve()
            .grantOpenMasterChest();
        
        Role.create("user")
            .grantShelve()
            .grantOpenAnyChest();
        
        Role.create("manager")
            .grantShelve()
            .grantOpenAnyChest()
            .grantAddChests()
            .grantRemoveChests();

        Role.create("admin")
            .grantShelve()
            .grantOpenAnyChest()
            .grantAddChests()
            .grantRemoveChests()
            .grantManageAccess();        
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
		if (decodeErrors == null) decodeErrors = new ArrayList<String>();
		log.warning(error);
		decodeErrors.add(error);
	}
	
	public static Limits getLimits(final Player player) {
	    for (Limits limits : groupLimits) {
	        if (limits.checker.check(player)) {
	            return limits;
	        }
	    }
	    return GLOBAL_LIMITS;
	}
}
