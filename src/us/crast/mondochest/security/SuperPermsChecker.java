package us.crast.mondochest.security;

import org.bukkit.entity.Player;

public class SuperPermsChecker extends PermissionChecker {
	public SuperPermsChecker(String permission) {
		super(permission);
	}

	@Override
	public boolean check(Player player) {
		return player.hasPermission(this.permission);
	}

}
