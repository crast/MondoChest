package us.crast.mondochest.security;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuperPermsChecker extends PermissionChecker {
	public SuperPermsChecker(String permission) {
		super(permission);
	}

	@Override
	public boolean check(Player player) {
		return player.hasPermission(this.permission);
	}

	@Override
	public boolean checkSender(CommandSender sender) {
		return sender.hasPermission(this.permission);
	}

}
