package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

public class BankSet {
	private ChestManager masterChest;
	private java.util.Vector<ChestManager> chestLocations = new java.util.Vector<ChestManager>();
	private Map<Material, ChestManager> materialChests = new HashMap<Material, ChestManager>();
	
	public BankSet(Chest masterChest) {
		this.masterChest = new ChestManager(masterChest);
	}
	
	public boolean addChest(Chest chest) {
		ChestManager newmanager = new ChestManager(chest);
		for (ChestManager m: chestLocations) {
			if (m.equals(newmanager)) return false;
		}
		chestLocations.add(newmanager);
		return true;
	}
	
	public java.util.Set<Material> refreshMaterials(World world) {
		materialChests.clear();
		for (ChestManager m: chestLocations) {
			for (ItemStack stack: m.listItems(world)) {
				materialChests.put(stack.getType(), m);
			}
		}
		return materialChests.keySet();
	}
	
	public void shelveItems(World world) {
		for (ItemStack stack: masterChest.listItems(world)) {
			Material m = stack.getType();
			ChestManager dest = materialChests.get(m);
			if (dest != null) {
				HashMap<Integer, ItemStack> x = dest.addItem(world, stack);
				masterChest.removeItem(world, stack);
				ChestManager.printWeirdStack(x);
			}
		}
	}
}
