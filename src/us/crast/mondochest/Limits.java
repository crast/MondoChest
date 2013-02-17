package us.crast.mondochest;

import org.bukkit.configuration.ConfigurationSection;

import us.crast.mondochest.security.MondoSecurity;
import us.crast.mondochest.security.PermissionChecker;

public final class Limits {
    public static final int UNLIMITED = -1;
    public final int findMaxRadius;
    public final int mastersPerUser;
    public final int slaveMaxAddRadius;
    public final int slavesPerMaster;
    public final PermissionChecker checker;

    public Limits(final ConfigurationSection config, Limits base) {
        findMaxRadius = parseLimit(config, "find_max_radius", (base == null)? 300 : base.findMaxRadius);
        mastersPerUser = parseLimitWithFallback(config, "mondochests_per_user", "mondochests.per_user", (base == null)? UNLIMITED : base.mastersPerUser);
        slaveMaxAddRadius = parseLimitWithFallback(config, "slave_max_add_radius", "slaves.max_add_radius", (base == null)? 150 : base.slaveMaxAddRadius);
        slavesPerMaster = parseLimitWithFallback(config, "slaves_per_master", "slaves.per_master", (base == null)? UNLIMITED : base.slavesPerMaster);
        checker = MondoSecurity.getChecker("mondochest.limits." + config.getName());
    }

    private static int parseLimit(ConfigurationSection config, String path, int defaultValue) {
        String val = config.getString(path);
        if (val == null) {
            return defaultValue;
        } else if (val.equals("unlimited") || val.equals("inf")) {
            return -1;
        } else {
            return Integer.parseInt(val);
        }
    }
    
    private static int parseLimitWithFallback(ConfigurationSection c, String path, String altpath, int defaultValue) {
        int val = parseLimit(c, path, -2);
        if (val == -2) {
            val = parseLimit(c, altpath, defaultValue);
        }
        return val;
    }
}
