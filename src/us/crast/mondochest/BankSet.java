package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

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
	
	public int shelveItems(World world) {
		int num_shelved = 0;
		for (ItemStack stack: masterChest.listItems(world)) {
			Material m = stack.getType();
			ChestManager dest = materialChests.get(m);
			if (dest != null) {
				//Logger log = Logger.getLogger("Minecraft");
				//log.info("Stack of " + stack.getType().toString() + " starting quantity: " + stack.getAmount());
				HashMap<Integer, ItemStack> failures = dest.addItem(world, stack);
				if (failures.isEmpty() && stack.getAmount() >= 0) {
					masterChest.removeItem(world, stack);
				} else {
					ChestManager.printWeirdStack(failures);
				}
				//log.info("Stack of " + stack.getType().toString() + " ending quantity: " + stack.getAmount());
				num_shelved++;
			}
		}
		return num_shelved;
	}
	
	public int numChests() {
		return chestLocations.size();
	}
}
