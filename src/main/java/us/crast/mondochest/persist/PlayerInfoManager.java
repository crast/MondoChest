package us.crast.mondochest.persist;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.entity.Player;


public class PlayerInfoManager {
	private Map<Integer, PlayerState> playerState = new HashMap<Integer, PlayerState>();

	public PlayerInfoManager() {
		
	}
	
	public PlayerState getState(Player player) {
		PlayerState state = playerState.get(player.getEntityId());
		if (state == null) {
			state = new PlayerState(player);
			playerState.put(player.getEntityId(), state);
		}
		return state;
	}

	public void shutdown() {
		playerState.clear();
	}
}
