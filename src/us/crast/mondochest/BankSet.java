package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

@SerializableAs("MondoChestSet")
public class BankSet implements ConfigurationSerializable {
	private String owner;
	private ChestManager masterChest;
	private List<ChestManager> chestLocations = new java.util.Vector<ChestManager>();
	private Map<Material, ChestManager> materialChests = new HashMap<Material, ChestManager>();
	private Map<MaterialWithData, ChestManager> materialDataChests = new HashMap<MaterialWithData, ChestManager>();
	
	public BankSet(Chest masterChest, String owner) {
		this.owner = owner;
		this.masterChest = new ChestManager(masterChest, false);
	}
	
	private BankSet(ChestManager masterChest, String owner) {
		this.owner = owner;
		this.masterChest = masterChest;
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
	
	private void addChestManager(ChestManager manager) {
		chestLocations.add(manager);
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
	
	/* Getters/setters */

	public String getOwner() {
		return owner;
	}
	
	public String toString() {
		return String.format(
			"<Bankset: master(%s), owner=%s, %d chests>",
			masterChest,
			getOwner(),
			numChests()
		);
	}
	
	/* Serialization */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> d = new HashMap<String, Object>();
		d.put("masterChest", masterChest);
		d.put("chestLocations", chestLocations);
		d.put("owner", owner);
		return d;
	}
	
	public static BankSet deserialize(Map<String, Object> d) {
		Logger log = MondoConfig.getLog();

		BankSet bankset = new BankSet(
				(ChestManager) d.get("masterChest"),
				(String) d.get("owner")
		);
		
		Object locations = d.get("chestLocations");
		if (locations instanceof List<?>) {
			for (Object location: (List<?>) d.get("chestLocations")) {
				if (location instanceof ChestManager) {
					bankset.addChestManager((ChestManager) location);
				} else {
					log.warning(String.format(
							"[MondoChest] when building chestLocations for bankset %s, expected a ChestManager, got a %s",
							bankset.toString(),
							location.getClass().getName()
					));
				}
			}
		} else {
			log.warning("[MondoChest] chestLocations is supposed to be a list, wtf mait");
		}
		
		return bankset;
	}
}
