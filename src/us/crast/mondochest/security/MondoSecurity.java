package us.crast.mondochest.security;

import us.crast.mondochest.MondoConfig;

public class MondoSecurity {
	static String mode = "null";

	public static void setMode(String mode) {
		MondoSecurity.mode = mode;
	}

	public static PermissionChecker getChecker(String permission) {
		if (mode.equals("null")) {
			return new NullChecker(permission);
		} else if (mode.equals("SuperPerms")) {
			return new SuperPermsChecker(permission);
		} else if (mode.equals("Vault")) {
			return new VaultChecker(MondoConfig.VAULT_PERMISSIONS, permission);
		}
		return null; // TODO
	}
}
