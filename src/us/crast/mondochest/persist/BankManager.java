package us.crast.mondochest.persist;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.BankSet;
import us.crast.mondochest.MondoChest;

public class BankManager {
	private Map<String, Map<BlockVector, BankSet>> banks = new HashMap<String, Map<BlockVector, BankSet>>();
	
	public BankManager(MondoChest plugin) {
		FileConfiguration f = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "banks.yml"));
		f.getKeys(false);
	}

	public Map<String, Map<BlockVector, BankSet>> getBanks() {
		return banks;
	}
	
}
