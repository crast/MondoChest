package us.crast.mondochest;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

public class MondoListener implements Listener {
	private java.util.logging.Logger log;
	private BlockSearcher searcher;
	private HashMap<String, BankSet> banks = new HashMap<String, BankSet>();
	private HashMap<BlockVector, BankSet> banksByCoords = new HashMap<BlockVector, BankSet>();
	
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
			if (firstLine.equals("[MondoChest]")) {
				BlockVector vec = block.getLocation().toVector().toBlockVector();
				BankSet bank = banksByCoords.get(vec);
				if (bank == null) {
					try {
						bank = bankFromSign(sign);
					} catch (MondoMessage m) {
						event.getPlayer().sendMessage(m.getMessage());
					}
					banksByCoords.put(vec, bank);
					initBank(bank, block);
				} else {
					String msg = "Found Materials: ";
					for (Material m: bank.refreshMaterials(block.getWorld())) {
						msg += m.toString() + ", ";
					}
					event.getPlayer().sendMessage(msg);
					bank.shelveItems(block.getWorld());
				}
			} else if (firstLine.equals("[MondoSlave]")) {
				//stuff
			}
			break;
		}
	}

	private BankSet bankFromSign(Sign sign) throws MondoMessage {
		java.util.Vector<Chest> chestsFound = SignUtils.nearbyChests(sign);
		if (chestsFound.isEmpty()) {
			throw new MondoMessage("No chests found near MondoMaster sign");
		} else {
			return new BankSet(chestsFound.elementAt(0));
		}
	}
	
	private void initBank(BankSet bank, Block context) {
		for (Block block: searcher.findBlocks(context, Material.SIGN)) {
			Sign sign = SignUtils.signFromBlock(block);
			for (Chest chest: SignUtils.nearbyChests(sign)) {
				bank.addChest(chest);
			}
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
