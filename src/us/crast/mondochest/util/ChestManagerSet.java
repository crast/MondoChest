package us.crast.mondochest.util;

import java.util.HashSet;

import us.crast.mondochest.ChestManager;

public class ChestManagerSet extends HashSet<ChestManager> {
	public ChestManagerSet() {
		super();
	}
	public ChestManagerSet(ChestManager m) {
		super();
		this.add(m);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1156432936713242411L;
}
