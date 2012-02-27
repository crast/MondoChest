package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.security.MondoSecurity;
import us.crast.mondochest.security.PermissionChecker;

public class MondoListener implements Listener {
	private static final String MASTER_SIGN_NAME = "[MondoChest]";
	private static final String SLAVE_SIGN_NAME = "[MondoSlave]";
	
	private PermissionChecker PERMISSION_USE;
	private PermissionChecker PERMISSION_CREATE_BANK;
	private PermissionChecker PERMISSION_ADD_SLAVE;
	
	private java.util.logging.Logger log;
	private BlockSearcher searcher;
	private Map<String, Map<BlockVector, BankSet>> banks = new HashMap<String, Map<BlockVector, BankSet>>();
	
	public MondoListener(java.util.logging.Logger log, BlockSearcher searcher) {
		this.log = log;
		this.searcher = searcher;
		this.PERMISSION_USE = MondoSecurity.getChecker("mondochest.use");
		this.PERMISSION_ADD_SLAVE = MondoSecurity.getChecker("mondochest.add_slave");
		this.PERMISSION_CREATE_BANK = MondoSecurity.getChecker("mondochest.create_master");
	}
	
	@EventHandler
    public void playerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.getClickedBlock();
		Material blockType = block.getType();
		switch (blockType) {
		case WALL_SIGN:
			Sign sign = SignUtils.signFromBlock(block);
			String firstLine = sign.getLine(0);
			if (firstLine.equals(MASTER_SIGN_NAME)) {
				Player player = event.getPlayer();
				if (!PERMISSION_USE.check(player)) {
					event.getPlayer().sendMessage("No permissions to use MondoChest");
					return;
				}
				World world = block.getWorld();
				BlockVector vec = block.getLocation().toVector().toBlockVector();
				BankSet bank = getWorldBanks(world).get(vec);
				
				if (bank == null) {
					if (!PERMISSION_CREATE_BANK.check(player)) {
						player.sendMessage("No permissions to create new MondoChest bank");
						return;
					}
					Block bank_context = block;
					try {
						bank = bankFromSign(sign);
						if (!sign.getLine(1).isEmpty()) {
							bank_context = DirectionalStrings.parseDirectional(block, sign.getLine(1));
						}
					} catch (MondoMessage m) {
						player.sendMessage(m.getMessage());
						return;
					}
					getWorldBanks(world).put(vec, bank);
					initBank(bank, block, bank_context);
					player.sendMessage("Created bank with " + bank.numChests() + " chests");
				}
				bank.refreshMaterials(world);
				int num_shelved = bank.shelveItems(world);
				if (num_shelved > 0) {
					event.getPlayer().sendMessage(String.format("Shelved %d items", num_shelved));
				}
				if (MondoConfig.RESTACK_MASTER) {
					bank.restackSpecial(world);
				}
			} else if (firstLine.equals(SLAVE_SIGN_NAME) && PERMISSION_ADD_SLAVE.check(event.getPlayer())) {
				BlockVector v = block.getLocation().toVector().toBlockVector();
				BlockVector other = null;
				double otherdistance = 0;
				for (BlockVector candidate: getWorldBanks(block.getWorld()).keySet()) {
					if (other == null) {
						other = candidate;
						otherdistance = v.distance(candidate);
					} else {
						double curdistance = v.distance(candidate);
						if (curdistance < otherdistance) {
							other = candidate;
						}
					}
				}
				if (other != null) {
					int num_added = addNearbyChestsToBank(getWorldBanks(block.getWorld()).get(other), sign);
					if (num_added > 0) {
						event.getPlayer().sendMessage("Added " + num_added + " chest" + (num_added == 1? "": "s") +" to bank");
					} else {
						event.getPlayer().sendMessage("Chests probably already in bank");
					}
				}
			}
			break;
		}
	}
	
	private Map<BlockVector, BankSet> getWorldBanks(World world) {
		Map<BlockVector, BankSet> banksByCoords = banks.get(world.getName());
		if (banksByCoords == null) {
			banksByCoords = new HashMap<BlockVector, BankSet>();
			banks.put(world.getName(), banksByCoords);
		}
		return banksByCoords;
	}

	private BankSet bankFromSign(Sign sign) throws MondoMessage {
		java.util.Vector<Chest> chestsFound = SignUtils.nearbyChests(sign);
		if (chestsFound.isEmpty()) {
			throw new MondoMessage("No chests found near MondoChest sign");
		} else {
			return new BankSet(chestsFound.elementAt(0));
		}
	}
	
	private int addNearbyChestsToBank(BankSet bank, Sign sign) {
		int chestsAdded = 0;
		boolean allow_restack = sign.getLine(1).equals("restack");
		for (Chest chest: SignUtils.nearbyChests(sign)) {
			if (bank.addChest(chest, allow_restack)) {
				chestsAdded++;
			}
		}
		return chestsAdded;
	}
	
	private int initBank(BankSet bank, Block orig, Block search_context) {
		int chestsAdded = 0;
		for (Block block: searcher.findBlocks(search_context, orig.getType())) {
			Sign sign = SignUtils.signFromBlock(block);
			if (sign.getLine(0).equals(SLAVE_SIGN_NAME)) {
				chestsAdded += addNearbyChestsToBank(bank, sign);
			}
		}
		return chestsAdded;
	}
	
	@SuppressWarnings("unused")
	private void listInventory(Inventory inv) {
		for(ItemStack stack: inv.getContents()) {
			if (stack == null) continue;
			int amount = stack.getAmount();
			String itype = stack.getType().toString();
			log.info("->" + amount + " of " + itype);
		}
	}
}
