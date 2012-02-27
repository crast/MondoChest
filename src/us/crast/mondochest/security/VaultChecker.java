package us.crast.mondochest.security;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

public class VaultChecker extends PermissionChecker {
	private Permission pmanager;
	
	public VaultChecker(Permission pmanager, String permission) {
		super(permission);
		this.pmanager = pmanager;
	}

	@Override
	public boolean check(Player player) {
		return pmanager.has(player, this.permission);
	}

}
