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
import org.bukkit.util.BlockVector;

import us.crast.mondochest.util.ChestManagerSet;
import us.crast.mondochest.util.DefaultDict;
import us.crast.mondochest.util.StringTools;

@SerializableAs("MondoChestSet")
public class BankSet implements ConfigurationSerializable {
	private String world;
	private String owner;
	private BlockVector masterSign;
	private ChestManager masterChest;
	private List<ChestManager> chestLocations = new java.util.Vector<ChestManager>();
	private DefaultDict<Material, ChestManagerSet> materialChests = new DefaultDict<Material, ChestManagerSet>(ChestManagerSet.getMaker());
	private DefaultDict<MaterialWithData, ChestManagerSet> materialDataChests = new DefaultDict<MaterialWithData, ChestManagerSet>(ChestManagerSet.getMaker());
	
	public BankSet(Chest masterChest, String owner, BlockVector masterSign) {
		this.owner = owner;
		this.masterChest = new ChestManager(masterChest, false);
		this.masterSign = masterSign;
		this.setWorld(masterChest.getWorld().getName());
	}
	
	private BankSet(ChestManager masterChest, String owner, BlockVector masterSign) {
		this.owner = owner;
		this.masterChest = masterChest;
		this.masterSign = masterSign;
	}
	
	public void restackSpecial(World world) {
		masterChest.restackSpecial(world);
	}
	
	public boolean addChest(Chest chest) {
		return addChest(chest, false);
	}
	
	public boolean addChest(Chest chest, boolean allow_restack) {
		ChestManager newmanager = new ChestManager(chest, allow_restack);
		if (newmanager.equals(masterChest)) return false;
		for (ChestManager m: chestLocations) {
			if (m.equals(newmanager)) return false;
		}
		chestLocations.add(newmanager);
		return true;
	}
	
	public boolean removeChest(Chest chest) {
		ChestManager other = new ChestManager(chest, false);
		for (ChestManager m: chestLocations) {
			if (m.equals(other)) {
				chestLocations.remove(m);
				return true;
			}
		}
		return false;
	}
	
	private void addChestManager(ChestManager manager) {
		// XXX de-serializing from version 0.5 might allow a ChestManager to be
		// added which is equal to the master, so stop it by ignoring its add.
		if (manager.equals(masterChest)) {
			MondoConfig.getLog().warning(
				"Found a slave chest with the same coordinates as a master. " +
				"Removing it to prevent any future issues."
			);
			return;
		}
		chestLocations.add(manager);
	}
	
	public java.util.Set<Material> refreshMaterials(World world) {
		//Logger log = Logger.getLogger("Minecraft");
		materialChests.clear();
		materialDataChests.clear();
		for (ChestManager m: chestLocations) {
			for (ItemStack stack: m.listItems(world)) {
				materialDataChests.ensure(new MaterialWithData(stack)).add(m);
				materialChests.ensure(stack.getType()).add(m);
			}
		}
		return materialChests.keySet();
	}
	
	public int shelveItems(World world) {
		int num_shelved = 0;
		Set<ChestManager> to_restack = new java.util.HashSet<ChestManager>();
		//Logger log = Logger.getLogger("Minecraft");
		for (ItemStack stack: masterChest.listItems(world)) {
			//log.info(String.format("Item: %d of %s", stack.getAmount(), stack.getType().toString()));
			MaterialWithData md = new MaterialWithData(stack);
			ChestManagerSet destinations = materialDataChests.get(md);
			if (destinations == null) {
				Material m = stack.getType();
				destinations = materialChests.get(m);
			}
			if (destinations != null) {
				for (ChestManager dest: destinations) {
					//log.info("Stack of " + stack.getType().toString() + " starting quantity: " + stack.getAmount());
					HashMap<Integer, ItemStack> failures = dest.addItem(world, stack);
					if (failures.isEmpty() && stack.getAmount() >= 0) {
						masterChest.removeItem(world, stack);
						if (dest.isRestackAllowed()) {
							to_restack.add(dest);
						}
						num_shelved++;
						break;
					} else {
						//ChestManager.printWeirdStack(failures);
					}
					//log.info("Stack of " + stack.getType().toString() + " ending quantity: " + stack.getAmount());
				}
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
	
	public List<ChestManager> listSlaves() {
		return chestLocations;
	}
	
	public ChestManager getMasterChest() {
		return masterChest;
	}

	public BlockVector getMasterSign() {
		return masterSign;
	}
	
	public String getKey() {
		return StringTools.md5String(this.masterSign.toString());
	}

	public String getOwner() {
		return owner;
	}
	
	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
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
		d.put("masterSign", masterSign);
		d.put("chestLocations", chestLocations);
		d.put("owner", owner);
		return d;
	}
	
	public static BankSet deserialize(Map<String, Object> d) {
		Logger log = MondoConfig.getLog();

		BankSet bankset = new BankSet(
				(ChestManager) d.get("masterChest"),
				(String) d.get("owner"),
				(BlockVector) d.get("masterSign")
		);
		
		Object locations = d.get("chestLocations");
		if (locations instanceof List<?>) {
			for (Object location: (List<?>) d.get("chestLocations")) {
				if (location instanceof ChestManager) {
					bankset.addChestManager((ChestManager) location);
				} else {
					log.warning(String.format(
							"when building chestLocations for bankset %s, expected a ChestManager, got a %s",
							bankset.toString(),
							location.getClass().getName()
					));
				}
			}
		} else {
			log.warning("chestLocations is supposed to be a list, wtf mait");
		}
		
		return bankset;
	}

	public void addAccess(String name) {
		// TODO Auto-generated method stub
		
	}

	public void removeAccess(String name) {
		// TODO Auto-generated method stub
		
	}
}