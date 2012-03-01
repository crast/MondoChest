package us.crast.mondochest;

import java.io.File;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.persist.BankManager;

public class MondoChest extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	private MondoListener listener = null;
	private BankManager bankManager;

	//private Economy econ;

	public void onEnable() {
		bankManager = new BankManager(this);
		if (!(new File(this.getDataFolder(), "config.yml").exists())) { 
			saveDefaultConfig();
		}
		MondoConfig.configure(this, getConfig(), log);
		if (listener == null) listener = new MondoListener(log, getSearcherFromConfig(), this);
		getServer().getPluginManager().registerEvents(listener, this);
		
		Object co = getConfig().get("footest");
		if (co instanceof ChestManager) {
			log.info("got chest manager");
			ChestManager cm = (ChestManager) co;
		} else {
			getConfig().set("footest", new ChestManager(new BlockVector(11, 75, 22), new BlockVector(12, 24, 98), false));
			saveConfig();
		}
		log.info("[MondoChest] MondoChest v0.5 ready");
		/*
		if (!setupEconomy()) {
			log.info("No economy found");
		} else {
			log.info("I think I have an economy!");
		}
		*/

	}
	
	private BlockSearcher getSearcherFromConfig() {
		FileConfiguration config = getConfig();
		int x = config.getInt("chest_search.radiusX");
		int y = config.getInt("chest_search.radiusY");
		int z = config.getInt("chest_search.radiusZ");
		return new BlockSearcher(x, y, z);
	}
	
	public void onDisable(){
		
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
