package us.crast.mondochest.security;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NullChecker extends PermissionChecker {
	public NullChecker(String permission) {
		super(permission);
	}

	@Override
	public boolean check(Player player) {
		return true;
	}
	
	@Override
	public boolean checkSender(CommandSender sender) {
		return true;
	}

}
