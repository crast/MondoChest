package us.crast.mondochest;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.command.BasicMessage;
import us.crast.mondochest.command.CallInfo;
import us.crast.mondochest.persist.BankManager;
import us.crast.mondochest.persist.PlayerInfoManager;
import us.crast.mondochest.persist.PlayerState;
import us.crast.mondochest.security.MondoSecurity;
import us.crast.mondochest.security.PermissionChecker;
import us.crast.mondochest.util.DirectionalStrings;

public class MondoListener implements Listener {
	private static final String MASTER_SIGN_NAME = MondoConstants.MASTER_SIGN_NAME;
	private static final String SLAVE_SIGN_NAME = MondoConstants.SLAVE_SIGN_NAME;
	
	private PermissionChecker can_use;
	private PermissionChecker can_create_bank;
	private PermissionChecker can_add_slave;
	private PermissionChecker can_override_break;
	private PermissionChecker can_override_add_slave;
	
	private java.util.logging.Logger log;
	private BlockSearcher searcher;
	private BankManager bankManager;
	private PlayerInfoManager playerManager = new PlayerInfoManager();
	
	public MondoListener(java.util.logging.Logger log, BlockSearcher searcher, MondoChest plugin) {
		this.log = log;
		this.searcher = searcher;
		this.can_use = MondoSecurity.getChecker("mondochest.use");
		this.can_add_slave = MondoSecurity.getChecker("mondochest.add_slave");
		this.can_create_bank = MondoSecurity.getChecker("mondochest.create_master");
		this.can_override_break = MondoSecurity.getChecker("mondochest.admin.break_any");
		this.can_override_add_slave = MondoSecurity.getChecker("mondochest.admin.add_any_slave");
		this.bankManager = plugin.getBankManager();
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
			if (firstLine.startsWith("[")) {
				MessageWithStatus response = null;
				try {
					if (firstLine.equals(MASTER_SIGN_NAME)) {
						response = masterSignClicked(block, sign, event.getPlayer());	
					} else if (firstLine.equals(SLAVE_SIGN_NAME) && can_add_slave.check(event.getPlayer())) {
						response = slaveSignClicked(block, sign, event.getPlayer());
					}
				} catch (MondoMessage m) {
					response = m;
				}
				if (response != null) {
					event.getPlayer().sendMessage(BasicMessage.render(response, true));
				}
			}
			break;
		}
	}

	private MessageWithStatus masterSignClicked(Block block, Sign sign, Player player) throws MondoMessage {
		if (!can_use.check(player)) {
			return new BasicMessage("No permissions to use MondoChest", Status.WARNING);
		}
		World world = block.getWorld();
		BlockVector vec = block.getLocation().toVector().toBlockVector();
		BankSet bank = getWorldBanks(world).get(vec);;
		
		if (bank == null) {
			if (!can_create_bank.check(player)) {
				return new BasicMessage("No permissions to create new bank", Status.WARNING);
			}
			Block bank_context = block;
			
			bank = bankFromSign(sign, player.getName()); // propagates MondoMessage
			if (!sign.getLine(1).isEmpty()) {
				bank_context = DirectionalStrings.parseDirectional(block, sign.getLine(1)); // propagates MondoMessage
			}
			bankManager.addBank(world.getName(), vec, bank);
			initBank(bank, block, bank_context);
			bankManager.save(); // propagates MondoMessage
			BasicMessage.send(player, Status.INFO, "Created bank with %d chest%s", bank.numChests(), pluralize(bank.numChests()));
		}
		BasicMessage response = null;
		bank.refreshMaterials(world);
		int num_shelved = bank.shelveItems(world);
		if (num_shelved > 0) {
			response = new BasicMessage(Status.SUCCESS, "Shelved %d item%s", num_shelved, pluralize(num_shelved));
		}
		if (MondoConfig.RESTACK_MASTER) {
			bank.restackSpecial(world);
		}
		// Store the last clicked bank
		PlayerState state = playerManager.getState(player);
		state.setLastClickedMaster(block.getLocation());
		return response;
	}

	private MessageWithStatus slaveSignClicked(Block block, Sign sign, Player player) throws MondoMessage {

		Location lastClicked = getLastClicked(player);
		if (lastClicked == null) {
			return new BasicMessage("To add slaves to a bank, click a master sign first", Status.USAGE);
		}
		double distance = block.getLocation().distance(lastClicked);
		if (distance > MondoConfig.SLAVE_MAX_ADD_RADIUS) {
			return new BasicMessage("Slave is too far away from the last clicked master, this is not recommended.", Status.WARNING);
		}
		
		BankSet targetBank = bankManager.getBank(lastClicked);
		if (targetBank == null) {
			return new BasicMessage("Wot. No Target " + lastClicked.toString(), Status.ERROR);
		} else if (!targetBank.getOwner().equals(player.getName())) {
			if (can_override_add_slave.check(player)) {
				BasicMessage.send(player, Status.INFO, "admin override allowed");
			} else {
				return new BasicMessage(String.format("Only this bank's owner, %s, can add slaves to the bank", targetBank.getOwner()), Status.WARNING);
			}
		}
		int num_added = addNearbyChestsToBank(targetBank, sign);
		BasicMessage response = null;
		if (num_added > 0) {
			bankManager.markChanged(block.getWorld().getName(), targetBank);
			response = new BasicMessage(Status.SUCCESS,
					"Added %d chest%s to bank",
					num_added,
					pluralize(num_added)
			);
			
		} else {
			response = new BasicMessage("Chests already in bank", Status.ERROR);
		}
		bankManager.save(); // Propagates MondoMessage on error
		return response;
	}

	public void masterBroken(Cancellable event, Sign sign, Player player) {
		BankSet bank = bankManager.getBank(sign.getLocation());
		if (bank == null) return;
		if (!bank.getOwner().equals(player.getName())) {
			if (can_override_break.check(player)) {
				BasicMessage.send(player, Status.INFO, "break override allowed");
			} else {
				event.setCancelled(true);
				BasicMessage.send(player,  Status.WARNING, "Cannot destroy a MondoChest which does not belong to you");
				return;
			}
		}
		// If we're here, actually delete the bank
		int num_slaves = bank.numChests();
		bankManager.removeBank(sign.getWorld().getName(), bank);
		BasicMessage.send(player, Status.SUCCESS,
			"removed bank and %d slave%s",
			num_slaves,
			pluralize(num_slaves)
		);
	}
	
	public void slaveBroken(Cancellable event, Sign sign, Player player) {
		Map<ChestManager, BankSet> slaves = bankManager.getWorldSlaves(sign.getWorld().getName());
		int removed = 0;
		for (Chest chest: SignUtils.nearbyChests(sign)) {
			ChestManager info = new ChestManager(chest, false);
			if (slaves.containsKey(info)) {
				BankSet bs = slaves.get(info);
				if (bs.getOwner().equals(player.getName()) || can_override_break.check(player)) {
					bs.removeChest(chest);
					removed++;
				} else {
					BasicMessage.send(player, Status.WARNING, "No access to remove this slave sign");
					event.setCancelled(true);
					return;
				}
			}
		}
		BasicMessage.send(player, Status.SUCCESS, "Removed %d chests", removed);
	}
	
	public void allowAccess(CallInfo call, String target) throws MondoMessage {
		Player player = call.getPlayer();
		BankSet lastClicked = getLastClickedBank(player, true);
		Player targetPlayer = call.getPlayer().getServer().getPlayer(target);
		if (targetPlayer == null) throw new MondoMessage("Target not found", Status.ERROR);
		if (call.getArg(0).equalsIgnoreCase("allow")) {
			lastClicked.addAccess(targetPlayer.getName());
			call.success(String.format("Player %s allowed", targetPlayer.getName()));
		} else {
			lastClicked.removeAccess(targetPlayer.getName());
			call.success(String.format("Player %s removed", targetPlayer.getName()));
		}
	}
	
	
	private Map<BlockVector, BankSet> getWorldBanks(World world) {
		return bankManager.getWorldBanks(world.getName());
	}

	private BankSet bankFromSign(Sign sign, String owner) throws MondoMessage {
		java.util.Vector<Chest> chestsFound = SignUtils.nearbyChests(sign);
		if (chestsFound.isEmpty()) {
			throw new MondoMessage("No chests found near MondoChest sign", Status.ERROR);
		} else {
			return new BankSet(
				chestsFound.elementAt(0), 
				owner,
				sign.getBlock().getLocation().toVector().toBlockVector()
			);
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
	
	@SuppressWarnings("unused")
	private BlockVector closestVector(BlockVector v, java.util.Collection<BlockVector> candidates) {
		BlockVector other = null;
		double otherdistance = 0;
		for (BlockVector candidate: candidates) {
			if (other == null) {
				other = candidate;
				otherdistance = v.distance(candidate);
			} else {
				double curdistance = v.distance(candidate);
				if (curdistance < otherdistance) {
					otherdistance = curdistance;
					other = candidate;
				}
			}
		}
		return other;
	}
	
	private String pluralize(int number) {
		return (number == 1? "": "s");
	}
	
	private Location getLastClicked(Player player) {
		Location lastClicked = playerManager.getState(player).getLastClickedMaster();
		if (lastClicked == null 
			|| !player.getLocation().getWorld().equals(lastClicked.getWorld())) {
			return null;
		}
		return lastClicked;
	}
	
	private BankSet getLastClickedBank(Player player, boolean enforce_ownership) throws MondoMessage{
		Location lastClicked = getLastClicked(player);
		if (lastClicked == null) throw new MondoMessage("Click a MondoChest sign before performing this action", Status.ERROR);
		
		BankSet targetBank = bankManager.getBank(lastClicked);
		if (targetBank == null) {
			throw new MondoMessage("Wot. No Target " + lastClicked.toString(), Status.ERROR);
		}
		if (enforce_ownership && !targetBank.getOwner().equals(player.getName())) {
			throw new MondoMessage("No messing with other people's banks", Status.WARNING);
		}
		return targetBank;
	}
}
