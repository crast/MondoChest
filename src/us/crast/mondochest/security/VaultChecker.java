package us.crast.mondochest.security;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultChecker extends PermissionChecker {
	private final Permission pmanager;
	
	public VaultChecker(Permission pmanager, String permission) {
		super(permission);
		this.pmanager = pmanager;
	}

	@Override
	public boolean check(Player player) {
		return pmanager.has(player, this.permission);
	}

	@Override
	public boolean checkSender(CommandSender sender) {
		return pmanager.has(sender, this.permission);
	}

}
