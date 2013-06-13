package us.crast.mondochest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mondocommand.CallInfo;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.chatmagic.BasicMessage;
import us.crast.chatmagic.MessageWithStatus;
import us.crast.chatmagic.MondoMessage;
import us.crast.chatmagic.Status;
import us.crast.mondochest.dialogue.AccessConvo;
import us.crast.mondochest.persist.BankManager;
import us.crast.mondochest.persist.PlayerInfoManager;
import us.crast.mondochest.persist.PlayerState;
import us.crast.mondochest.security.MondoSecurity;
import us.crast.mondochest.security.PermissionChecker;
import us.crast.mondochest.util.MaterialSet;
import us.crast.mondochest.util.SignUtils;

public final class MondoListener implements Listener {
	
	private PermissionChecker can_use;
	private PermissionChecker can_create_bank;
	private PermissionChecker can_add_slave;
	private PermissionChecker can_override_break;
	private PermissionChecker can_override_add_slave;
    private PermissionChecker can_override_open;
    private PermissionChecker can_override_master_limit;

	
	private final BankManager bankManager;
	private PlayerInfoManager playerManager = new PlayerInfoManager();
    private AccessConvo accessConvo;
	
	public MondoListener(final MondoChest plugin) {
	    this.accessConvo = new AccessConvo(plugin, this);
		this.bankManager = plugin.getBankManager();
		this.reloadConfig();
	}
	
	@EventHandler(ignoreCancelled=true)
    public void playerInteract(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.getClickedBlock();
        Player player = event.getPlayer();
		Material blockType = block.getType();
		switch (blockType) {
		case WALL_SIGN:
			Sign sign = SignUtils.signFromBlock(block);
			String firstLine = sign.getLine(0);
			if (firstLine.startsWith("[")) {
				MessageWithStatus response = null;
				try {
				    switch (MondoSign.match(firstLine)) {
				    case MASTER:
						response = masterSignClicked(block, sign, player);
						break;
				    case SLAVE:
				        if (can_add_slave.check(player)) {
				            response = slaveSignClicked(block, sign, player);
				        }
				        break;
				    case RELOAD:
				        if (can_add_slave.check(player)) {
				            response = reloadSignClicked(block, sign, player);
				        }
				        break;
					}
				} catch (MondoMessage m) {
					response = m;
				}
				if (response != null) {
					player.sendMessage(response.render(true));
				}
			}
			break;
		case CHEST:
		    MessageWithStatus response = chestClicked(event, block, player);
		    if (response != null) {
		        player.sendMessage(response.render(true));
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
		BankSet bank = getWorldBanks(world).get(vec);
		
		if (bank == null) {
			if (!can_create_bank.check(player)) {
				return new BasicMessage("No permissions to create new bank", Status.WARNING);
			}
			
			// Deal with masters per user limit.
			Limits limits = MondoConfig.getLimits(player);
			if (limits.mastersPerUser != Limits.UNLIMITED && !can_override_master_limit.check(player)) {
			    if (numBanks(player) >= limits.mastersPerUser) {
			        return new BasicMessage(Status.WARNING, "{WARNING}You have passed the limit of {GREEN}%d{WARNING} banks", limits.mastersPerUser);
			    }
			}
			
			bank = bankFromSign(sign, player.getName()); // propagates MondoMessage
			bankManager.addBank(world.getName(), vec, bank);
			bankManager.save(); // propagates MondoMessage
			BasicMessage.send(player, Status.SUCCESS, "Created new MondoChest bank");
			// Store the last clicked bank
		    PlayerState state = playerManager.getState(player);
	        state.setLastClickedMaster(block.getLocation());
			return new BasicMessage("right-click slave signs to add them", Status.INFO);
		} 
	    
		// Store the last clicked bank
        PlayerState state = playerManager.getState(player);
        state.setLastClickedMaster(block.getLocation());
		
        if (!bank.getAccess(player).canShelve()) {
		    return new BasicMessage("You do not have access to this MondoChest", Status.WARNING);
		}
		bank.refreshMaterials(world);
		int num_shelved = bank.shelveItems(world);
		if (MondoConfig.RESTACK_MASTER) {
			bank.restackSpecial(world);
		}

	    if (num_shelved > 0) {
	        return new BasicMessage(Status.SUCCESS, "Shelved %d item%s", num_shelved, pluralize(num_shelved));
	    } else {
	        return new BasicMessage("Nothing to shelve", Status.WARNING);
	    }
	}

    private MessageWithStatus slaveClickedCommon(Block block, Sign sign, Player player, String noun, MaterialSet materials) throws MondoMessage {
	    Location lastClicked = getLastClicked(player);
		if (lastClicked == null) {
			return new BasicMessage("To add slaves to a bank, click a master sign first", Status.USAGE);
		}
		Limits limits = MondoConfig.getLimits(player);
		if (limits.slaveMaxAddRadius != Limits.UNLIMITED) {
    		double distance = block.getLocation().distance(lastClicked);
    		if (distance > limits.slaveMaxAddRadius) {
    			return new BasicMessage("Slave is too far away from the last clicked master, this is not recommended.", Status.WARNING);
    		}
		}
		
		BankSet targetBank = bankManager.getBank(lastClicked);
		if (targetBank == null) {
			return new BasicMessage("Wot. No Target " + lastClicked.toString(), Status.ERROR);
		} else if (!targetBank.getAccess(player).canAddChests() && !can_override_add_slave.check(player)) {
			return new BasicMessage(Status.WARNING, "You do not have permission to add slaves to this MondoChest");
		}
	    if (limits.slavesPerMaster != Limits.UNLIMITED) {
	        if (targetBank.numChests() >= limits.slavesPerMaster) {
	            return new BasicMessage(Status.WARNING, "You cannot add more than %d slaves.", limits.slavesPerMaster);
	        }
	    }
		
		int num_added = addNearbyObjectsToBank(targetBank, sign, materials);
		BasicMessage response = null;
		if (num_added == -1) {
			response = new BasicMessage("No nearby " + noun + "s found", Status.ERROR);
		} else if (num_added == 0) {
			response = new BasicMessage(noun + "s already in bank", Status.ERROR);
		} else {
			bankManager.markChanged(targetBank);
			response = new BasicMessage(Status.SUCCESS,
					"Added %d chest%s to bank",
					num_added,
					pluralize(num_added)
			);
			
		}
		bankManager.save(); // Propagates MondoMessage on error
		return response;
	}
	
	private MessageWithStatus slaveSignClicked(Block block, Sign sign, Player player) throws MondoMessage {
	    return slaveClickedCommon(block, sign, player, "chest", MondoConstants.CHEST_MATERIALS);
	}
	private MessageWithStatus reloadSignClicked(Block block, Sign sign, Player player) throws MondoMessage {
        return slaveClickedCommon(block, sign, player, "dispenser", MondoConstants.DISPENSER_MATERIALS);
    }

    public MessageWithStatus masterBroken(Cancellable event, Sign sign, Player player) {
		BankSet bank = bankManager.getBank(sign.getBlock().getLocation());
		if (bank == null) return null;
		if (!bank.getOwner().equals(player.getName())) {
			if (can_override_break.check(player)) {
				return new BasicMessage("break override allowed", Status.INFO);
			} else {
				event.setCancelled(true);
				return new BasicMessage(Status.WARNING, "Cannot destroy a MondoChest which does not belong to you");
			}
		}
		// If we're here, actually delete the bank
		int num_slaves = bank.numChests();
		bankManager.removeBank(sign.getWorld().getName(), bank);
		playerManager.getState(player).setLastClickedMaster(null);
		return new BasicMessage(Status.SUCCESS,
			"removed bank and %d slave%s",
			num_slaves,
			pluralize(num_slaves)
		);
	}
	
	public MessageWithStatus slaveBroken(Cancellable event, Sign sign, Player player) {
		Map<ChestManager, BankSet> slaves = bankManager.getWorldSlaves(sign.getWorld().getName());
		int removed = 0;
		for (Chest chest: slaveFinder().nearbyChests(sign)) {
			ChestManager info = new ChestManager(chest, false);
			if (slaves.containsKey(info)) {
				BankSet bs = slaves.get(info);
				if (bs.getAccess(player).canRemoveChests() || can_override_break.check(player)) {
					if (bs.removeChest(chest)) {
					    bankManager.markChanged(bs);
					}
					removed++;
				} else {
					event.setCancelled(true);
					return new BasicMessage(Status.WARNING, "No access to remove this slave sign");

				}
			}
		}
		return new BasicMessage(Status.SUCCESS, "Removed %d chests", removed);
	}

    public MessageWithStatus chestBroken(BlockBreakEvent event, Block block, Player player) throws MondoMessage {
        int removed = 0;
        int errors = 0;
        for (BankSet bank : banksFromChest(block)) {
            if (MondoConfig.PROTECTION_CHEST_BREAK) {
                if (!bank.getAccess(player).canRemoveChests() && !can_override_break.check(player)) {
                    event.setCancelled(true);
                    return new BasicMessage(Status.WARNING, "Only the chest's owner, %s, can remove chests.", bank.getOwner());
                }
            }
            if (bank.removeChest((Chest) block.getState())) {
                bankManager.markChanged(bank);
                removed++;
            } else {
                errors++;
                BasicMessage.send(player, Status.ERROR, "Had some problems removing a chest.");
                if (errors >= 3) {
                    return new BasicMessage("Total fail. stopping", Status.ERROR);
                }
            }
        }
        if (removed > 0) {
            bankManager.save();
           return new BasicMessage(Status.SUCCESS, "Removed chest from %d banks", removed);
        }
        return null;
    }

    private MessageWithStatus chestClicked(Cancellable event, Block block, Player player) {
        if (MondoConfig.PROTECTION_CHEST_OPEN) {
            if (can_override_open.check(player)) {
                return null; // Succeed and don't bother creating the banksFromChest mapping
            }
            boolean success = true;
            BlockVector specific_vector = block.getLocation().toVector().toBlockVector();
            for (BankSet bank : banksFromChest(block)) {
                ChestManager master = bank.getMasterChest();
                Role role = bank.getAccess(player);
                if (specific_vector.equals(master.getChest1()) || specific_vector.equals(master.getChest2())) {
                    // We have a master chest
                    success = role.canOpenMasterChest();
                } else {
                    // We have a slave chest
                    success = role.canOpenSlaveChest();
                }
                if (!success) {
                    event.setCancelled(true);
                    return new BasicMessage("You do not have access to this MondoChest", Status.WARNING);
                }
            }
        }
        /*
        PlayerState state = playerManager.getState(player);
        if (state.isManagingChest()) {
            // TODO
        }*/
        return null;
    }

    public void manageAccess(CallInfo call, Player player) throws MondoMessage {
        if (!MondoConfig.ACL_ENABLED) {
            throw new MondoMessage(MondoConstants.ACL_ENABLED_MESSAGE, Status.ERROR);
        }
        accessConvo.begin(player);
    }
	
	private Map<BlockVector, BankSet> getWorldBanks(World world) {
		return bankManager.getWorldBanks(world.getName());
	}

	private BankSet bankFromSign(Sign sign, String owner) throws MondoMessage {
		java.util.Vector<Chest> chestsFound = masterFinder().nearbyChests(sign);
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

	private Set<BankSet> banksFromChest(Block block) {
	    BlockVector vec = block.getLocation().toVector().toBlockVector();
	    Set<BankSet> results = bankManager.getChestLocMap(block.getWorld().getName()).get(vec);
	    if (results == null) {
	        return Collections.emptySet();
	    }
	    return results;
	}
	
	private int addNearbyObjectsToBank(BankSet bank, Sign sign, MaterialSet materials) {
		int objectsAdded = 0;
		boolean allow_restack = sign.getLine(1).trim().equalsIgnoreCase("restack");
		
	    List<BlockState> nearby = slaveFinder().nearbyBlocks(sign, materials);
        if (nearby.isEmpty()) return -1;
        for (BlockState block: nearby) {
            if (bank.add(block, allow_restack)) {
                objectsAdded++;
            }
        }
        
		return objectsAdded;
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
	
	public BankSet getLastClickedBank(Player player, boolean enforce_ownership) throws MondoMessage{
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
	
	public void shutdown() {
		if (playerManager != null) {
			playerManager.shutdown();
			playerManager = null;
		}
		if (accessConvo != null) {
		    accessConvo.shutdown();
		    accessConvo = null;
		}
	}

    public void findItems(CallInfo call, Player player) throws MondoMessage {
        List<String> all_args = call.getArgs();
        String item_name = StringUtils.join(all_args, ' ');
        Material mat = Material.matchMaterial(item_name);
        if (mat == null) {
            call.reply("{ERROR}Unidentified material '%s'", item_name);
            return;
        }
        World world = player.getWorld();
        BankSet bank = getLastClickedBank(player, false);
        if (!bank.getAccess(player).canFind()) {
            call.reply("{WARNING}Not allowed to access this MondoChest");
            return;
        }
        int findMaxRadius = MondoConfig.getLimits(player).findMaxRadius;
        if (findMaxRadius != Limits.UNLIMITED  && player.getLocation().toVector().distance(bank.getMasterSign()) > findMaxRadius) {
            call.reply("{ERROR}Too far away from chest bank");
            return;
        }
        boolean found = false;
        for (ChestManager chest: bank.listSlaves()) {
            int quantity = 0;
            for (ItemStack item: chest.listItems(world)) {
                if (item.getType() == mat) {
                    quantity += item.getAmount();
                }
            }
            if (quantity > 0) {
                BlockVector chest1 = chest.getChest1();
                call.reply(
                    "{INFO}%d in chest at {BLUE}x={RED}%d{BLUE}, y={RED}%d{BLUE}, z={RED}%d",
                    quantity,
                    chest1.getBlockX(),
                    chest1.getBlockY(),
                    chest1.getBlockZ()
                );
                if (!found) player.openInventory(chest.getInventory(world, chest.getChest1()));
                found = true;
            }
        }
        if (!found) {
            call.reply("{ERROR}No items found");
        }
    }
    
    /**
     * Find out how many banks a player has owned by them.
     * @return number of banks, zero if none are owned by this player.
     */
    private int numBanks(final Player player) {
        int myBanks = 0;
        for (BankSet bs: bankManager.listAllBanks()) {
            if (player.getName().equalsIgnoreCase(bs.getOwner())) {
                myBanks++;
            }
        }
        return myBanks;
    }
    
    private SignUtils slaveFinder() {
        return new SignUtils(
            MondoConfig.SLAVE_VERTICAL_TWO, 
            MondoConfig.SLAVE_HORIZONTAL_TWO,
            MondoConfig.SIGN_IN_FRONT
        );
        
    }
    
    private SignUtils masterFinder() {
        return new SignUtils(false, false, MondoConfig.SIGN_IN_FRONT);
    }
    
    public void reloadConfig() {
        this.can_use = MondoSecurity.getChecker("mondochest.use");
        this.can_add_slave = MondoSecurity.getChecker("mondochest.add_slave");
        this.can_create_bank = MondoSecurity.getChecker("mondochest.create_master");
        this.can_override_break = MondoSecurity.getChecker("mondochest.admin.break_any");
        this.can_override_open = MondoSecurity.getChecker("mondochest.admin.open_any");
        this.can_override_add_slave = MondoSecurity.getChecker("mondochest.admin.add_any_slave");
        this.can_override_master_limit = MondoSecurity.getChecker("mondochest.admin.no_master_limit");
    }
}
