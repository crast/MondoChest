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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

public class ChestManager {
	private static final BlockFace[] cardinals = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
	private BlockVector chest1 = null;
	private BlockVector chest2 = null;
	@SuppressWarnings("unused")
	private java.util.logging.Logger log = Logger.getLogger("Minecraft");
	
	public ChestManager(Chest chest) { 
		this(chest.getBlock());
	}
	
	public ChestManager(Block block) {
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
	
	/* ChestManager methods */
	
	public HashMap<Integer, ItemStack> addItem(World world, ItemStack stack) {
		HashMap<Integer, ItemStack> failures = getInventory(world, chest1).addItem(stack);
		printWeirdStack(failures);
		if (!failures.isEmpty() && chest2 != null) {
			failures = getInventory(world, chest2).addItem(failures.values().toArray(new ItemStack[0]));
			printWeirdStack(failures);
		}
		return failures;
	}
	
	public void removeItem(World world, ItemStack stack) {
		HashMap<Integer, ItemStack> failures = null;
		for (Inventory inv: validInventories(world)) {
			failures = inv.removeItem(stack);
			if (failures.isEmpty()) break;
		}
	}
	
	public List<ItemStack> listItems(World world) {
		java.util.Vector<ItemStack> items = new java.util.Vector<ItemStack>();
		for (Inventory inv: validInventories(world)) {
			for (ItemStack stack: inv.getContents()) {
				if (stack != null) items.add(stack);
			}
		}
		return items;
	}
	
	private Inventory getInventory(World world, BlockVector vector) {
		Block block = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
		if (block.getType() == Material.CHEST) {
			return ((Chest) block.getState()).getInventory();
		} else {
			return null;
		}
	}
	
	private Inventory[] validInventories(World world) {
		Inventory i1 = getInventory(world, chest1);
		Inventory i2 = null;
		if (chest2 != null) {
			i2 = getInventory(world, chest2);
		}
		if (i1 == null) return new Inventory[0];
		if (i2 == null) return new Inventory[] { i1 };
		return new Inventory[] { i1, i2 };
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
	
	/* Getters/Setters */
	public BlockVector getChest1() {
		return chest1;
	}

	public BlockVector getChest2() {
		return chest2;
	}
}
