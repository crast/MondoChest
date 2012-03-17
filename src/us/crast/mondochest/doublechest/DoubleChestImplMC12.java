package us.crast.mondochest.doublechest;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.ChestManager;

public class DoubleChestImplMC12 extends DoubleChestImpl {
	private static final int DOUBLE_CHEST_SIZE = 54;
	
	@Override
	public HashMap<Integer, ItemStack> addItem(ChestManager manager, World world, ItemStack stack) {
		HashMap<Integer, ItemStack> failures = null;
		for (Inventory inv: validInventories(manager, world)) {
			if (failures == null) {
				failures = inv.addItem(stack);
			} else {
				failures = inv.addItem(failures.values().toArray(new ItemStack[0]));
			}
			if (failures.isEmpty()) break;
		}
		return failures;
	}
	
	@Override
	protected Inventory[] validInventories(ChestManager manager, World world) {
		Inventory i1 = manager.getInventory(world, manager.getChest1());
		if (i1 == null) return new Inventory[0];
		if (i1.getSize() == DOUBLE_CHEST_SIZE) {
			return new Inventory[] { i1 };
		}
		Inventory i2 = null;
		BlockVector chest2 = manager.getChest2();
		if (chest2 != null) {
			i2 = manager.getInventory(world, chest2);
		}
		if (i2 == null) return new Inventory[] { i1 };
		return new Inventory[] { i1, i2 };
	}

}
