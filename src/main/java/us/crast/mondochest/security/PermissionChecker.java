package us.crast.mondochest.security;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PermissionChecker {
	protected final String permission;
	
	PermissionChecker(final String permission) {
		this.permission = permission;
	}
	
	public abstract boolean check(Player player);
	
	public abstract boolean checkSender(CommandSender sender);

	public String getPermission() {
		return permission;
	}
}