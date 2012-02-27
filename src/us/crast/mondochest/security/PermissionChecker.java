package us.crast.mondochest.security;

import org.bukkit.entity.Player;

public abstract class PermissionChecker {
	protected String permission;
	
	PermissionChecker(String permission) {
		this.permission = permission;
	}
	
	public abstract boolean check(Player player);

	public String getPermission() {
		return permission;
	}
}