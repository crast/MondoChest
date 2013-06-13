package us.crast.mondochest.persist;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;


public class PlayerInfoManager {
	private Map<UUID, PlayerState> playerState = new HashMap<UUID, PlayerState>();

	public PlayerInfoManager() {
		
	}
	
	public PlayerState getState(Player player) {
		PlayerState state = playerState.get(player.getUniqueId());
		if (state == null) {
			state = new PlayerState(player);
			playerState.put(player.getUniqueId(), state);
		}
		return state;
	}

	public void shutdown() {
		playerState.clear();
	}
}
