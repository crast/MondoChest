package us.crast.mondochest;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class Restacker {
	private static final Material[] STACKABLE_MATERIALS = {Material.SIGN, Material.SNOW_BALL, Material.EGG};
	
	public static void restackChestManager(World world, ChestManager manager) {
		restack(new RestackerContextChestManager(manager, world));
	}
	
	private static void restack(RestackerContext context) {
		Map<Integer, ItemStack> availMats = new java.util.HashMap<Integer, ItemStack>();
		for (Material mat: STACKABLE_MATERIALS) availMats.put(new Integer(mat.getId()), null);
		
		List<ItemStack> items = context.listItems();
		for (ItemStack stack: items) {
			Integer id = new Integer(stack.getTypeId());
			if (availMats.containsKey(id) && stack.getAmount() < 64) {
				ItemStack avail = availMats.get(id);
				if (avail == null) {
					availMats.put(id, stack);
				} else {
					int new_amount = avail.getAmount() + stack.getAmount(); 
					if (new_amount > 64) {
						stack.setAmount(new_amount - 64);
						avail.setAmount(64);
					} else {
						stack.setAmount(avail.getAmount() + stack.getAmount());
						context.removeItem(avail);
					}
					availMats.put(id, stack);
				}
			}
		}
	}

}

interface RestackerContext {
	public void removeItem(ItemStack stack);
	public List<ItemStack> listItems();
}

final class RestackerContextChestManager implements RestackerContext {
	private ChestManager chestmanager;
	private World world;

	public RestackerContextChestManager(ChestManager cm, World world) {
		this.chestmanager = cm;
		this.world = world;
	}
	
	public void removeItem(ItemStack stack) {
		chestmanager.removeItem(world, stack);
	}
	
	public List<ItemStack> listItems() {
		return chestmanager.listItems(world);
	}
}
