package us.crast.mondochest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.doublechest.DoubleChestImpl;
import us.crast.mondochest.doublechest.DoubleChestImplMC12;

@SerializableAs("ChestManager")
public final class ChestManager implements ConfigurationSerializable {
	private static final BlockFace[] cardinals = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
	private BlockVector chest1 = null;
	private BlockVector chest2 = null;
	private boolean restackAllowed;
	@SuppressWarnings("unused")
	private static java.util.logging.Logger log = Logger.getLogger("Minecraft"); 
	private static DoubleChestImpl impl = new DoubleChestImplMC12();
	
	public ChestManager(Chest chest, boolean allow_restack) { 
		this(chest.getBlock(), allow_restack);
	}
	
	public ChestManager(Block block, boolean allow_restack) {
		this.setRestackAllowed(allow_restack);
		chest1 = block.getLocation().toVector().toBlockVector();
		for (BlockFace face: cardinals) {
			Block other = block.getRelative(face);
			if (other.getType() == Material.CHEST) {
				BlockVector othervec = other.getLocation().toVector().toBlockVector();
				if (face == BlockFace.NORTH || face == BlockFace.EAST) {
					chest2 = chest1;
					chest1 = othervec;
				} else {
					chest2 = othervec;
				}
				break;
			}
		}
	}
	
	public ChestManager(BlockVector c1, BlockVector c2, boolean allow_restack) {
		chest1 = c1;
		chest2 = c2;
		restackAllowed = allow_restack;
	}
	
	/* ChestManager methods */
	
	public HashMap<Integer, ItemStack> addItem(World world, ItemStack stack) {
		return impl.addItem(this, world, stack);
	}
	
	public void removeItem(World world, ItemStack stack) {
		impl.removeItem(this, world, stack);
	}
	
	public List<ItemStack> listItems(World world) {
		return impl.listItems(this, world);
	}
	
	public Inventory getInventory(World world, BlockVector vector) {
		Block block = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
		if (block.getType() == Material.CHEST) {
			return ((Chest) block.getState()).getInventory();
		} else {
			return null;
		}
	}
	
	public static void printWeirdStack(HashMap<Integer, ItemStack> entries) {
		if (entries.isEmpty()) return;
		Logger logger = Logger.getLogger("Minecraft");
		logger.info("Printing Weird Hashmap:");
		for (Map.Entry<Integer, ItemStack> entry: entries.entrySet()) {
			logger.info(
				" ::: " + entry.getKey().toString() + " -> " + 
				entry.getValue().getAmount() + " of " +
				entry.getValue().getType().toString()
			);
		}
	}
	
	public void restackSpecial(World world) {
		Restacker.restackChestManager(world, this);
	}
	
	/* Object Primitives */
	public int hashCode() {
		int code = chest1.hashCode();
		if (chest2 != null) code ^= chest2.hashCode();
		return code;
	}
	
	public boolean equals(Object other) {
		if (other instanceof ChestManager) {
			ChestManager cmother = (ChestManager) other;
			if (!cmother.getChest1().equals(chest1)) return false;
			if (cmother.getChest2() == null && chest2 == null) return true;
			return (cmother.getChest2().equals(chest2));
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return String.format(
				"<ChestManager: x=%d,y=%d,z=%d,restackAllowed: %s",
				chest1.getBlockX(),
				chest1.getBlockY(),
				chest1.getBlockZ(),
				Boolean.valueOf(restackAllowed).toString()
		);
	}
	
	/* Getters/Setters */
	public BlockVector getChest1() {
		return chest1;
	}

	public BlockVector getChest2() {
		return chest2;
	}

	public boolean isRestackAllowed() {
		return restackAllowed;
	}

	public void setRestackAllowed(boolean restackAllowed) {
		this.restackAllowed = restackAllowed;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> d = new HashMap<String, Object>();
		d.put("chest1", chest1);
		if (chest2 != null) {
			d.put("chest2", chest2);
		}
		if (restackAllowed) d.put("restackAllowed", restackAllowed);
		return d;
	}
	
	public static ChestManager deserialize(Map<String, Object> d) {
		boolean restackAllowed = false;
		if (d.containsKey("restackAllowed")) {
			restackAllowed = ((Boolean) d.get("restackAllowed")).booleanValue();
		}
		return new ChestManager(
				(BlockVector) d.get("chest1"), 
				d.containsKey("chest2")? ((BlockVector) d.get("chest2")) : null,
				restackAllowed
		);
	}
	
}
