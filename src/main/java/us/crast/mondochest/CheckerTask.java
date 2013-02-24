package us.crast.mondochest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import mondocommand.ChatMagic;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BlockVector;

import us.crast.chatmagic.BasicMessage;
import us.crast.chatmagic.MondoMessage;
import us.crast.chatmagic.Status;


public class CheckerTask implements Runnable {
    private static final int DEADLINE_MS = 15;
    private MondoChest plugin;
    private ArrayList<TaskEntry> tasks = new ArrayList<TaskEntry>();
    private int currentIndex;
    private CommandSender sender;

    public CheckerTask(MondoChest plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
        fillTasks();
    }
    
    private void fillTasks() {
        for (Map<BlockVector, BankSet> worldBanks : plugin.getBankManager().getBanks().values()) {
            for (Map.Entry<BlockVector, BankSet> entry: worldBanks.entrySet()) {
                BankSet bank = entry.getValue();
                for (ChestManager cm : bank.allChestManagers()) {
                    tasks.add(new TaskEntry(bank, cm));
                }
            }
        }
        // Sort the tasks by world and block location to keep things in similar chunks together
        Collections.sort(tasks);
        currentIndex = tasks.size();
    }

    @Override
    public void run() {
        long deadline = System.currentTimeMillis() + DEADLINE_MS;
        while (--currentIndex >= 0) {
            TaskEntry task = tasks.get(currentIndex);
            World world = plugin.getServer().getWorld(task.bank.getWorld());
            BlockVector[] locs = task.chest.internalBlockLocations();
            boolean is_master = task.bank.getMasterChest().equals(task.chest);
            ArrayList<Block> successes = checkTargets(world, Material.CHEST, locs);
            if (is_master) {
                if (successes.size() == 0 || checkTargets(world, Material.WALL_SIGN, task.bank.getMasterSign()).size() == 0) {
                    log("Removing whole bank");
                    plugin.getBankManager().removeBank(task.bank.getWorld(), task.bank);
                } else if (successes.size() < locs.length) {
                    log("Fixing master to a single chest");
                    task.bank.setMasterChest(new ChestManager(successes.get(0), task.chest.isRestackAllowed()));
                    plugin.getBankManager().markChanged(task.bank);
                }
            } else {
                if (successes.size() == 0) {
                    log("Removing slave");
                    task.bank.remove(task.chest);
                } else if (successes.size() < locs.length) {
                    log("Changing slave from a doublechest to single");
                    task.bank.add(successes.get(0).getState(), task.chest.isRestackAllowed());
                }
            }
            if (System.currentTimeMillis() > deadline) {
                log("Checker: %d of %d", tasks.size()-currentIndex, tasks.size());
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 50);
                return;
            }
        }
        finish();
    }
    
    private void finish() {
        try {
            plugin.getBankManager().saveIfNeeded();
        } catch (MondoMessage e) {
            log(BasicMessage.render(e, true));
        }
        log("Checker complete (%d chests checked)", tasks.size());
        tasks = null;
        plugin = null;
        sender = null;
    }

    /**
     * @param world
     * @param locs
     */
    private ArrayList<Block> checkTargets(World world, Material target, BlockVector ... locs) {
        ArrayList<Block> successes = new ArrayList<Block>();
        for (BlockVector v: locs) {
            Block block = world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());
            if (block.getType() != target) {
                log("%s{INFO}: expected {NOUN}%s{INFO}, got {NOUN}%s", formatBV(v), target, block.getType());
            } else {
                successes.add(block);
                //log("-> Item at %s is a chest!", v);
            }
        }
        return successes;
    }
    
    private String formatBV(BlockVector v) {
        return ChatMagic.colorize("{INFO}x={RED}%d{INFO},y={RED}%d{INFO},z={RED}%d", v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }
    
    private void log(String template, Object ... args) {
        BasicMessage.send(sender, Status.INFO, template, args);
    }

}

class TaskEntry implements Comparable<TaskEntry> {
    public BankSet bank;
    public ChestManager chest;

    TaskEntry(BankSet bank, ChestManager chest) {
        this.bank = bank;
        this.chest = chest;
    }
    
    public Integer getChunkX() {
        return chest.getChest1().getBlockX() / 16;
    }
    
    public Integer getChunkZ() {
        return chest.getChest1().getBlockZ() / 16;
    }

    @Override
    public int compareTo(TaskEntry other) {
        if (other == null) { 
            return 1; 
        }
        int result = bank.getWorld().compareTo(other.bank.getWorld());
        if (result != 0) return result;
        result = getChunkX().compareTo(other.getChunkX());
        if (result != 0) return result;
        result = getChunkZ().compareTo(other.getChunkZ());
        if (result != 0) return result;
        result = new Integer(chest.getChest1().getBlockY())
                    .compareTo(other.chest.getChest1().getBlockY());
        return result;
    }
}