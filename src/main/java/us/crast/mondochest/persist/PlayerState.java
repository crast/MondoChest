package us.crast.mondochest.persist;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerState {
	private String lastClickedWorld = null;
	private Vector lastClickedVector = null;
	
	private State state = State.NORMAL;
	
	public PlayerState(Player player) {}
	
	/* Getters/Setters */
	public Location getLastClickedMaster() {
	    if (lastClickedWorld == null) return null;
	    World world = Bukkit.getWorld(lastClickedWorld);
	    if (world == null) return null;
		return new Location(
		    world,
		    lastClickedVector.getX(),
		    lastClickedVector.getY(),
		    lastClickedVector.getZ()
		);
	}
	
	public void setLastClickedMaster(Location lastClickedMaster) {
	    if (lastClickedMaster == null) {
	        this.lastClickedWorld = null;
	    } else {
    		this.lastClickedWorld = lastClickedMaster.getWorld().toString();
    		this.lastClickedVector = lastClickedMaster.toVector();
	    }
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
