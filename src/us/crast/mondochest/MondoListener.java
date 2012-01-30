package us.crast.mondochest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MondoListener implements Listener {
	private java.util.logging.Logger log;
	private BlockSearcher searcher;
	private BankSet bankset = null;
	
	public MondoListener(java.util.logging.Logger log) {
		this.log = log;
		this.searcher = new BlockSearcher(20, 10, 20);
		log.info("Started MondoListener too");
	}
	
	@EventHandler
    public void playerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Material blockType = block.getType();
		String typestr = block.getType().toString();
		log.info("Got PlayerInteract event: " + typestr + 
				" at x=" + block.getX() +
				" y=" + block.getY() +
				" z=" + block.getZ()
				);
		switch (blockType) {
		case WALL_SIGN:
		case SIGN:
			Sign sign = SignUtils.signFromBlock(block);
			String firstLine = sign.getLine(0);
			if (firstLine.startsWith("=Mondo")) {
				if (firstLine.equals("=MondoMaster")) {
					java.util.Vector<Chest> chestsFound = SignUtils.nearbyChests(sign);
					if (chestsFound.isEmpty()) {
						event.getPlayer().sendMessage("No chests found near MondoMaster sign");
					} else if (this.bankset == null) {
						this.bankset = new BankSet(chestsFound.elementAt(0));
					} else {
						String msg = "Found Materials: ";
						for (Material m: this.bankset.refreshMaterials(block.getWorld())) {
							msg += m.toString() + ", ";
						}
						event.getPlayer().sendMessage(msg);
					}
				} else {
					for (Chest chest: SignUtils.nearbyChests(sign)) {
						this.bankset.addChest(chest);
					}
				}
				for (Chest chest: SignUtils.nearbyChests(sign)) {
					Inventory inv = chest.getInventory();
					listInventory(inv);
				}
			}
			break;
		case CHEST:
			for (Block someBlock: searcher.findBlocks(block)) {
				Chest chest = (Chest) someBlock.getState();
				Inventory inv = chest.getInventory();
				listInventory(inv);
			}
			break;
		}
	}
	
	private void listInventory(Inventory inv) {
		for(ItemStack stack: inv.getContents()) {
			if (stack == null) continue;
			int amount = stack.getAmount();
			String itype = stack.getType().toString();
			log.info("->" + amount + " of " + itype);
		}
	}
}
