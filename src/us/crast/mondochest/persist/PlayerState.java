package us.crast.mondochest.persist;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerState {
	private Location lastClickedMaster;
	public PlayerState(Player player) {}
	
	/* Getters/Setters */
	public Location getLastClickedMaster() {
		return lastClickedMaster;
	}
	public void setLastClickedMaster(Location lastClickedMaster) {
		this.lastClickedMaster = lastClickedMaster;
	}
	
}
