package us.crast.mondochest.persist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.util.BlockVector;

import us.crast.datastructures.DefaultDict;
import us.crast.datastructures.ObjectMaker;
import us.crast.mondochest.BankSet;
import us.crast.mondochest.ChestManager;

public final class WorldCache {
	private final String world;
	private Map<ChestManager, BankSet> slaves = null;
	private DefaultDict<BlockVector, Set<BankSet>> banksByChest = null;
	private HashSet<ClearWatcher> clears = new HashSet<ClearWatcher>();

	public WorldCache(String world) {
		this.world = world;
	}

	public void clear() {
		this.slaves = null;
		this.banksByChest = null;
		for (ClearWatcher watcher : clears) {
		    watcher.clear(world);
		}
	}
	
	public void addClearWatcher(ClearWatcher watcher) {
	    clears.add(watcher);
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
	
	public Map<BlockVector, Set<BankSet>> getChestLocMap(BankManager bankManager) {
	    if (this.banksByChest == null) {
	        this.banksByChest = new DefaultDict<BlockVector, Set<BankSet>>(new BankSetMaker());
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
	    banksByChest.ensure(cm.getChest1()).add(bs);
	    if (cm.getChest2() != null) {
	        banksByChest.ensure(cm.getChest2()).add(bs);
	    }
	}
	
	public static ObjectMaker<WorldCache> getMaker() {
		return new Maker();
	}
	
	public static interface ClearWatcher {
	    public void clear(String world);
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

class BankSetMaker implements ObjectMaker<Set<BankSet>> {

    @Override
    public Set<BankSet> build(Object key) {
        return new HashSet<BankSet>();
    }
}
