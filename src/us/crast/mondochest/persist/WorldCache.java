package us.crast.mondochest.persist;

import java.util.HashMap;
import java.util.Map;

import us.crast.mondochest.BankSet;
import us.crast.mondochest.ChestManager;
import us.crast.mondochest.util.ObjectMaker;

public class WorldCache {
	private String world;
	
	private Map<ChestManager, BankSet> slaves = null;


	public WorldCache(String world) {
		this.world = world;
	}

	public void clear() {
		this.slaves = null;
	}

	public Map<ChestManager, BankSet> getSlaves(BankManager bankManager) {
		if (this.slaves == null) {
			this.slaves = new HashMap<ChestManager, BankSet>();
			for (BankSet bs: bankManager.getWorldBanks(world).values()) {
				for (ChestManager cm: bs.listSlaves()) {
					slaves.put(cm, bs);
				}
			}
		}
		return this.slaves;
		
	}
	
	public static ObjectMaker<WorldCache> getMaker() {
		return new Maker();
	}
	
}

/* Maker */
class Maker implements ObjectMaker<WorldCache> {
	@Override
	public WorldCache build(Object key) {
		String world = "";
		if (key instanceof String) {
			world = (String) key;
		}
		return new WorldCache(world);
	}
	
}
