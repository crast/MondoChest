package us.crast.mondochest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mondocommand.ChatMagic;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import us.crast.chatmagic.BasicMessage;
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
    public static boolean SIGN_IN_FRONT = false;

	public static Permission VAULT_PERMISSIONS = null;
	public static boolean USE_COMMANDS = true;
	public static String FALLBACK_ROLE = MondoConstants.ROLE_NONE;
    public static boolean ACL_ENABLED = true;
    private static Limits GLOBAL_LIMITS = null;
    private static List<Limits> groupLimits = null;

	private static List<String> decodeErrors = null;

	
	public static void configure(MondoChest plugin, FileConfiguration config, Logger log) {
		MondoConfig.log = log;
		configureChatColors();
		RESTACK_MASTER = config.getBoolean("restack_master");
		RESTACK_SLAVES = config.getBoolean("restack_slaves");
		PROTECTION_SIGNS = config.getBoolean("protection.signs");
		PROTECTION_CHEST_BREAK = config.getBoolean("protection.chest_break");
		PROTECTION_CHEST_OPEN = config.getBoolean("protection.chest_open");
		USE_COMMANDS = config.getBoolean("use_commands", true);
		SLAVE_VERTICAL_TWO = config.getBoolean("special.slave_vertical_two");
		SLAVE_HORIZONTAL_TWO = config.getBoolean("special.slave_horizontal_two");
		SIGN_IN_FRONT = config.getBoolean("special.sign_in_front");
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
	
	private static void configureChatColors() {
	    BasicMessage.setAppTitle(MondoConstants.APP_NAME);
	    ChatMagic.registerAlias("{USAGE}", ChatColor.LIGHT_PURPLE);
        ChatMagic.registerAlias("{WARNING}", ChatColor.DARK_RED);
        ChatMagic.registerAlias("{INFO}", ChatColor.GRAY);
        ChatMagic.registerAlias("{SUCCESS}", ChatColor.GREEN);
        ChatMagic.registerAlias("{ERROR}", ChatColor.RED);
        ChatMagic.registerAlias("{NOUN}", ChatColor.AQUA);
        ChatMagic.registerAlias("{VERB}", ChatColor.GRAY);
    }

    private static void configureGroupLimits(ConfigurationSection s) {
        if (s == null) return;
	    groupLimits = new ArrayList<Limits>();
        for (String name : s.getKeys(false)) {
            if (s.isConfigurationSection(name)) {
                groupLimits.add(
                    new Limits(s.getConfigurationSection(name), GLOBAL_LIMITS)
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
