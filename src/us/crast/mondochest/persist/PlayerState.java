package us.crast.mondochest.persist;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerState {
	private Location lastClickedMaster;
	private State state = State.NORMAL;
	public PlayerState(Player player) {}
	
	/* Getters/Setters */
	public Location getLastClickedMaster() {
		return lastClickedMaster;
	}
	public void setLastClickedMaster(Location lastClickedMaster) {
		this.lastClickedMaster = lastClickedMaster;
	}
	
	public boolean isManagingChest() {
	    return this.state == State.MANAGING_CHEST;
	}
	
	public void setManagingChest() {
	    this.state = State.MANAGING_CHEST;
	}
	
	public void setNormalMode() {
	    this.state = State.NORMAL;
	}
	
	enum State {
	    NORMAL,
	    MANAGING_CHEST;
	}
}
