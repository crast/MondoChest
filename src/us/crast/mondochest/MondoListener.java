package us.crast.mondochest;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MondoListener implements Listener {
	private java.util.logging.Logger log;
	public MondoListener(java.util.logging.Logger log) {
		this.log = log;
		log.info("Started MondoListener too");
	}
	@EventHandler
    public void playerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		String typestr = block.getType().toString();
		log.info("Got PlayerInteract event: " + typestr + 
				" at x=" + block.getX() +
				" y=" + block.getY() +
				" z=" + block.getZ()
				);
		if (typestr.equals("CHEST")) {
			Chest chest = (Chest) block.getState();
			Inventory inv = chest.getInventory();
			for(ItemStack stack: inv.getContents()) {
				if (stack == null) continue;
				int amount = stack.getAmount();
				String itype = stack.getType().toString();
				log.info("->" + amount + " of " + itype);
			}
		}
	}
}
