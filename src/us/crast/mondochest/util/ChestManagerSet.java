package us.crast.mondochest.util;

import java.util.HashSet;

import us.crast.mondochest.ChestManager;

public class ChestManagerSet extends HashSet<ChestManager> {
	private static final long serialVersionUID = 1156432936713242411L;
	private static ChestManagerSetMaker maker;
	
	public ChestManagerSet() {
		super();
	}
	public ChestManagerSet(ChestManager m) {
		super();
		this.add(m);
	}
	
	public static ObjectMaker<ChestManagerSet> getMaker() {
		if (maker == null) maker = new ChestManagerSetMaker();
		return maker;
	}
}

class ChestManagerSetMaker implements ObjectMaker<ChestManagerSet> {
	@Override
	public ChestManagerSet build(Object key) {
		return new ChestManagerSet();
	}
}