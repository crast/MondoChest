package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.persist.BankManager;
import us.crast.mondochest.persist.WorldCache;

public class RedstoneListener implements Listener, WorldCache.ClearWatcher {
    private BankManager bankManager;
    private Map<String, Map<BlockVector, BankSet>> bankInfo = new HashMap<String, Map<BlockVector, BankSet>>();
    
    public RedstoneListener(final MondoChest plugin) {
        this.bankManager = plugin.getBankManager();
    }
    
    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (event.getOldCurrent() != 0 || event.getNewCurrent() == 0) {
            return;
        }
        Block block = event.getBlock();
        World world = block.getWorld();
        String worldName = world.getName();
        Map<BlockVector, BankSet> worldInteresting = bankInfo.get(worldName);
        if (worldInteresting == null) {
            worldInteresting = buildWorldInteresting(worldName);
            bankInfo.put(worldName, worldInteresting);
        }
        BlockVector target = new BlockVector(block.getX(), block.getY(), block.getZ());
        BankSet targetBank = worldInteresting.get(target);
        if (targetBank != null) {
            targetBank.shelveItems(world);
        }
    }

    private Map<BlockVector, BankSet> buildWorldInteresting(String worldName) {
        Map<BlockVector, BankSet> interesting = new HashMap<BlockVector, BankSet>();
        for (BankSet bank: bankManager.getWorldBanks(worldName).values()) {
            interesting.put(bank.getMasterSign(), bank);
        }
        bankManager.getWorldCache(worldName).addClearWatcher(this);
        return interesting;
    }

    @Override
    public void clear(String world) {
        bankInfo.remove(world);
    }
}
