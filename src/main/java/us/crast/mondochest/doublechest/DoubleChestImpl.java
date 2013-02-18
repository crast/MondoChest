package us.crast.mondochest.doublechest;

import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.crast.mondochest.ChestManager;

public abstract class DoubleChestImpl {
	public void removeItem(ChestManager manager, World world, ItemStack stack) {
		HashMap<Integer, ItemStack> failures = null;
		for (Inventory inv: validInventories(manager, world)) {
			failures = inv.removeItem(stack);
			if (failures.isEmpty()) break;
		}
	}
	
	public List<ItemStack> listItems(ChestManager manager, World world) {
		java.util.Vector<ItemStack> items = new java.util.Vector<ItemStack>();
		for (Inventory inv: validInventories(manager, world)) {
			//log.info("Inv: " + inv.toString());
			for (ItemStack stack: inv.getContents()) {
				if (stack != null) { 
					items.add(stack);
					//log.info("" + stack.getType().toString());
				}
			}
			//log.info("====");
		}
		return items;
	}
	
	public abstract HashMap<Integer, ItemStack> addItem(ChestManager manager, World world, ItemStack stack);

	protected abstract Inventory[] validInventories(ChestManager manager, World world);
}
