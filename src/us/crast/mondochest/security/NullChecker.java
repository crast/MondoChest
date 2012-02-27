package us.crast.mondochest.security;

import org.bukkit.entity.Player;

public class NullChecker extends PermissionChecker {
	public NullChecker(String permission) {
		super(permission);
	}

	@Override
	public boolean check(Player player) {
		return true;
	}

}
