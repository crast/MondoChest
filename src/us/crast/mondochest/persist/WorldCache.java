package us.crast.mondochest.persist;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.util.BlockVector;

import us.crast.datastructures.ObjectMaker;
import us.crast.mondochest.BankSet;
import us.crast.mondochest.ChestManager;

public class WorldCache {
	private final String world;
	private Map<ChestManager, BankSet> slaves = null;
	private Map<BlockVector, BankSet> banksByChest = null;

	public WorldCache(String world) {
		this.world = world;
	}

	public void clear() {
		this.slaves = null;
		this.banksByChest = null;
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
	
	public Map<BlockVector, BankSet> getChestLocMap(BankManager bankManager) {
	    if (this.banksByChest == null) {
	        this.banksByChest = new HashMap<BlockVector, BankSet>();
	        for (BankSet bs: bankManager.getWorldBanks(world).values()) {
	            addLocs(bs.getMasterChest(), bs);
	            for (ChestManager cm: bs.listSlaves()) {
	                addLocs(cm, bs);
	            }
	        }
	    }
	    return this.banksByChest;
	}
	
	private void addLocs(ChestManager cm, BankSet bs) {
	    banksByChest.put(cm.getChest1(), bs);
	    banksByChest.put(cm.getChest2(), bs);
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
