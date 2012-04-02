package us.crast.mondochest.doublechest;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.ChestManager;

public final class DoubleChestImplMC11 extends DoubleChestImpl {
	@Override
	public HashMap<Integer, ItemStack> addItem(ChestManager manager, World world, ItemStack stack) {
		HashMap<Integer, ItemStack> failures = manager.getInventory(world, manager.getChest1()).addItem(stack);
		//printWeirdStack(failures);
		BlockVector chest2 = manager.getChest2();
		if (!failures.isEmpty() && chest2 != null) {
			failures = manager.getInventory(world, chest2).addItem(failures.values().toArray(new ItemStack[0]));
			//printWeirdStack(failures);
		}
		return failures;
	}
	
	@Override
	protected Inventory[] validInventories(ChestManager manager, World world) {
		Inventory i1 = manager.getInventory(world, manager.getChest1());
		Inventory i2 = null;
		BlockVector chest2 = manager.getChest2();
		if (chest2 != null) {
			i2 = manager.getInventory(world, chest2);
		}
		if (i1 == null) return new Inventory[0];
		if (i2 == null) return new Inventory[] { i1 };
		return new Inventory[] { i1, i2 };
	}

}
