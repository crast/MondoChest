package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class BankSet {
	private ChestManager masterChest;
	private List<ChestManager> chestLocations = new java.util.Vector<ChestManager>();
	private Map<Material, ChestManager> materialChests = new HashMap<Material, ChestManager>();
	private Map<MaterialWithData, ChestManager> materialDataChests = new HashMap<MaterialWithData, ChestManager>();
	
	public BankSet(Chest masterChest) {
		this.masterChest = new ChestManager(masterChest, false);
	}
	
	public void restackSpecial(World world) {
		masterChest.restackSpecial(world);
	}
	
	public boolean addChest(Chest chest) {
		return addChest(chest, false);
	}
	
	public boolean addChest(Chest chest, boolean allow_restack) {
		ChestManager newmanager = new ChestManager(chest, allow_restack);
		for (ChestManager m: chestLocations) {
			if (m.equals(newmanager)) return false;
		}
		chestLocations.add(newmanager);
		return true;
	}
	
	public java.util.Set<Material> refreshMaterials(World world) {
		materialChests.clear();
		materialDataChests.clear();
		for (ChestManager m: chestLocations) {
			for (ItemStack stack: m.listItems(world)) {
				materialDataChests.put(new MaterialWithData(stack), m);
				materialChests.put(stack.getType(), m);
			}
		}
		return materialChests.keySet();
	}
	
	public int shelveItems(World world) {
		int num_shelved = 0;
		Set<ChestManager> to_restack = new java.util.HashSet<ChestManager>();
		for (ItemStack stack: masterChest.listItems(world)) {
			MaterialWithData md = new MaterialWithData(stack);
			ChestManager dest = materialDataChests.get(md);
			if (dest == null) {
				Material m = stack.getType();
				dest = materialChests.get(m);
			}
			if (dest != null) {
				//Logger log = Logger.getLogger("Minecraft");
				//log.info("Stack of " + stack.getType().toString() + " starting quantity: " + stack.getAmount());
				HashMap<Integer, ItemStack> failures = dest.addItem(world, stack);
				if (failures.isEmpty() && stack.getAmount() >= 0) {
					masterChest.removeItem(world, stack);
				} else {
					//ChestManager.printWeirdStack(failures);
				}
				//log.info("Stack of " + stack.getType().toString() + " ending quantity: " + stack.getAmount());
				if (dest.isRestackAllowed()) {
					to_restack.add(dest);
				}
				num_shelved++;
			}
		}
		if (MondoConfig.RESTACK_SLAVES) {
			for (ChestManager chest: to_restack) {
				chest.restackSpecial(world);
			}
		}
		return num_shelved;
	}
	
	public int numChests() {
		return chestLocations.size();
	}
}
