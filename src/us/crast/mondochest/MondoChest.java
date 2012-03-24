package us.crast.mondochest;

import java.io.File;
import java.util.logging.Logger;

//import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
//import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import us.crast.mondochest.command.Executor;
import us.crast.mondochest.persist.BankManager;

public class MondoChest extends JavaPlugin {
	Logger log;
	private MondoListener listener = null;
	private BreakListener break_listener = null;
	private int num_reloads = 0;
	private BankManager bankManager;

	//private Economy econ;

	public void onEnable() {
		this.log = getLogger();
		this.reloadMondoChest();
		if (listener == null) listener = new MondoListener(log, getSearcherFromConfig(), this);
		getServer().getPluginManager().registerEvents(listener, this);
		
		if (MondoConfig.PROTECTION_SIGNS || MondoConfig.PROTECTION_CHEST_BREAK) {
			break_listener = new BreakListener(listener);
			getServer().getPluginManager().registerEvents(break_listener, this);
		}
		
		if (MondoConfig.USE_COMMANDS) {
			getCommand("mondochest").setExecutor(new Executor(this, listener));
		}
		
		log.info(String.format("[MondoChest] MondoChest %s ready", MondoConstants.MONDOCHEST_VERSION));
		/*
		if (!setupEconomy()) {
			log.info("No economy found");
		} else {
			log.info("I think I have an economy!");
		}
		*/

	}
	
	public void onDisable() {
		try {
			bankManager.save();
		} catch (MondoMessage msg) {
			log.warning(msg.getMessage());
		}
	}
	
	public void reloadMondoChest() {
		if (bankManager == null) {
			bankManager = new BankManager(this);
		} else {
			bankManager.load();
		}
		if (!(new File(this.getDataFolder(), "config.yml").exists())) { 
			saveDefaultConfig();
		}
		if (++num_reloads > 1) {
			reloadConfig();
		}
		MondoConfig.configure(this, getConfig(), log);
	}

	private BlockSearcher getSearcherFromConfig() {
		FileConfiguration config = getConfig();
		int x = config.getInt("chest_search.radiusX");
		int y = config.getInt("chest_search.radiusY");
		int z = config.getInt("chest_search.radiusZ");
		return new BlockSearcher(x, y, z);
	}
	
	/*
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    */
	
	public BankManager getBankManager() {
		return bankManager;
	}
	
	static {
		ConfigurationSerialization.registerClass(ChestManager.class, "ChestManager");
		ConfigurationSerialization.registerClass(BankSet.class, "MondoChestSet");
	}
	
}
