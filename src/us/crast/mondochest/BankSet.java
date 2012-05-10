package us.crast.mondochest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.datastructures.DefaultDict;
import us.crast.mondochest.util.ChestManagerSet;
import us.crast.utils.StringTools;

@SerializableAs("MondoChestSet")
public final class BankSet implements ConfigurationSerializable {
	private String world;
	private String owner;
	private BlockVector masterSign;
	private ChestManager masterChest;
	private Set<ChestManager> chestLocations = new java.util.HashSet<ChestManager>();
	private DefaultDict<Material, ChestManagerSet> materialChests = new DefaultDict<Material, ChestManagerSet>(ChestManagerSet.getMaker());
	private DefaultDict<MaterialWithData, ChestManagerSet> materialDataChests = new DefaultDict<MaterialWithData, ChestManagerSet>(ChestManagerSet.getMaker());
	private Set<String> acl = null;
	
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
		if (chestLocations.contains(newmanager)) return false;
		chestLocations.add(newmanager);
		return true;
	}
	
	public boolean removeChest(Chest chest) {
		ChestManager other = new ChestManager(chest, false);
		return chestLocations.remove(other);
	}
	
	private void addChestManager(ChestManager manager) {
		// XXX de-serializing from version 0.5 might allow a ChestManager to be
		// added which is equal to the master, so stop it by ignoring its add.
		if (manager.equals(masterChest)) {
			MondoConfig.logDecodeError(
				"Found a slave chest with the same coordinates as a master. " +
				"Removing it to prevent any future issues."
			);
			return;
		}
		chestLocations.add(manager);
	}
	
	public java.util.Set<Material> refreshMaterials(World world) {
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
	
	public Collection<ChestManager> listSlaves() {
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
		d.put("chestLocations", new ArrayList<ChestManager>(chestLocations));
		d.put("owner", owner);
		if (acl != null) {
            d.put("acl", new ArrayList<String>(acl));
        }
		return d;
	}
	
	public static BankSet deserialize(Map<String, Object> d) {
		BankSet bankset = new BankSet(
				(ChestManager) d.get("masterChest"),
				(String) d.get("owner"),
				(BlockVector) d.get("masterSign")
		);
		
		Object locations = d.get("chestLocations");
		if (locations instanceof Collection<?>) {
			for (Object location: (Collection<?>) d.get("chestLocations")) {
				if (location instanceof ChestManager) {
					bankset.addChestManager((ChestManager) location);
				} else {
					MondoConfig.logDecodeError(String.format(
							"when building chestLocations for bankset %s, expected a ChestManager, got a %s",
							bankset.toString(),
							location.getClass().getName()
					));
				}
			}
		} else if (locations == null) {
			MondoConfig.logDecodeError("chestLocations appears to be null");
		} else {
			MondoConfig.logDecodeError("chestLocations is supposed to be a list, wtf mait, got: " + locations.getClass().getName());
		}
		Object acl = d.get("acl");
		if (acl != null) {
		    if (acl instanceof Collection<?>) {
		        for (Object user: (Collection<?>) acl) {
		            if (user instanceof String) {
		                bankset.addAccess((String) user);
		            } else {
		                MondoConfig.logDecodeError("ACL entries should be strings");
		            }
		        }
		    } else {
		        MondoConfig.logDecodeError("ACL is supposed to be a collection");
		    }
		}
		
		return bankset;
	}

	public boolean addAccess(String name) {
		return getAcl().add(name);
	}

	public boolean removeAccess(String name) {
		return getAcl().remove(name);
	}
	
	public boolean hasAccess(String name) {
		if (acl == null || acl.isEmpty()) return true;
		return acl.contains(name);
	}
	
	private Set<String> getAcl() {
		if (acl == null) acl = new HashSet<String>();
		return acl;
	}
	
	public void setAcl(Set<String> acl) {
		this.acl = acl;
	}
}