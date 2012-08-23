package us.crast.mondochest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import us.crast.mondochest.command.Executor;
import us.crast.mondochest.persist.BankManager;
import us.crast.mondochest.util.MetricsLite;

public final class MondoChest extends JavaPlugin {
	private Logger log;
	private MondoListener listener = null;
	private BreakListener break_listener = null;
	private int num_reloads = 0;
	private BankManager bankManager;

	public void onEnable() {
		this.log = MondoConfig.setLog(getLogger());
		this.reloadMondoChest();
		if (listener == null) listener = new MondoListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		
		if (MondoConfig.PROTECTION_SIGNS || MondoConfig.PROTECTION_CHEST_BREAK) {
			break_listener = new BreakListener(listener);
			getServer().getPluginManager().registerEvents(break_listener, this);
		}
		
		if (MondoConfig.USE_COMMANDS) {
			getCommand("mondochest").setExecutor(new Executor(this, listener));
		}
		
		log.info(String.format("MondoChest %s ready", MondoConstants.MONDOCHEST_VERSION));
		/*
		if (!setupEconomy()) {
			log.info("No economy found");
		} else {
			log.info("I think I have an economy!");
		}
		*/
		try {
		    MetricsLite metrics = new MetricsLite(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}

	}
	
	public void onDisable() {
		try {
			bankManager.saveIfNeeded();
		} catch (MondoMessage msg) {
			log.warning(msg.getMessage());
		}
		bankManager.shutdown();
		listener.shutdown();
		bankManager = null;
		listener = null;
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
		
		if (listener != null) {
		    listener.reloadConfig();
		}
	}
	
	
	public BankManager getBankManager() {
		return bankManager;
	}
	
	static {
		ConfigurationSerialization.registerClass(ChestManager.class, "ChestManager");
		ConfigurationSerialization.registerClass(BankSet.class, "MondoChestSet");
	}
	
}
