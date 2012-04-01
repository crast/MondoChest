package us.crast.mondochest.persist;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import us.crast.mondochest.BankSet;
import us.crast.mondochest.ChestManager;
import us.crast.mondochest.MondoChest;
import us.crast.mondochest.MondoConfig;
import us.crast.mondochest.MondoMessage;
import us.crast.mondochest.util.DefaultDict;
import us.crast.mondochest.util.FileTools;
import us.crast.mondochest.util.StringTools;

public class BankManager {
	private Map<String, Map<BlockVector, BankSet>> banks = new HashMap<String, Map<BlockVector, BankSet>>();
	private Map<String, String> worldHashes = new HashMap<String, String>();
	private DefaultDict<String, WorldCache> worldCaches = new DefaultDict<String, WorldCache>(WorldCache.getMaker());
	private Set<BankSet> changed = new HashSet<BankSet>();
	private File bankFile;
	private FileConfiguration config;
	
	public BankManager(MondoChest plugin) {
		bankFile = new File(plugin.getDataFolder(), "banks.yml");
		load();
	}
	
	public void load() {
		changed.clear();
		MondoConfig.clearDecodeErrors();
		config = YamlConfiguration.loadConfiguration(bankFile);
		config.options().pathSeparator('/');
		ConfigurationSection worlds = ensureSection(config, "worlds");
		for (String worldHash: worlds.getKeys(false)) {
			Map<BlockVector, BankSet> worldbanks = new HashMap<BlockVector, BankSet>();
			ConfigurationSection worldconfig = worlds.getConfigurationSection(worldHash);
			String worldName = worldconfig.getString("_name");
			banks.put(worldName, worldbanks);
			for (String hash: worldconfig.getKeys(false)) {
				if (hash.startsWith("_")) continue;
				Object b = worldconfig.get(hash);
				if (b instanceof BankSet) {
					BankSet bs = (BankSet) b;
					bs.setWorld(worldName);
					worldbanks.put(bs.getMasterSign(), bs);
				}
			}
		}
		if (MondoConfig.getDecodeErrors() != null && MondoConfig.getDecodeErrors().size() > 0) {
			java.util.logging.Logger log = MondoConfig.getLog();
			File dest = new File(bankFile.getParentFile(), String.format("error-backup-banks-%d.yml", System.currentTimeMillis() / 1000));
			String copyinfo = null;
			if (FileTools.copyFileNoError(bankFile, dest)) {
				copyinfo = "For safety, the original has been backed up as \"%s\".";
			} else {
				copyinfo = "We tried to backup the original as \"%s\", but failed";
			}
			log.warning(String.format(
				"There were decode errors noted when loading the banks.yml file." + copyinfo,
				dest.getAbsolutePath()
			));
		}
	}
	
	public void save() throws MondoMessage {
		for (BankSet bank: changed) {
			worldSection(bank.getWorld()).set(bank.getKey(), bank);
		}
		try {
			config.save(bankFile);
		} catch (IOException e) {
			throw new MondoMessage(String.format("Could not save bank config: %s", e.getMessage()));
		}
		changed.clear();
	}

	public Map<String, Map<BlockVector, BankSet>> getBanks() {
		return banks;
	}
	
	public Map<BlockVector, BankSet> getWorldBanks(String world) {
		Map<BlockVector, BankSet> banksByCoords = banks.get(world);
		if (banksByCoords == null) {
			banksByCoords = new HashMap<BlockVector, BankSet>();
			banks.put(world, banksByCoords);
		}
		return banksByCoords;
	}
	
	public Map<ChestManager, BankSet> getWorldSlaves(String world) {
		return worldCaches.ensure(world).getSlaves(this);
	}
	
	public BankSet getBank(String world, BlockVector vec) {
		return getWorldBanks(world).get(vec);
	}
	
	public BankSet getBank(Location loc) {
		return getBank(loc.getWorld().getName(), loc.toVector().toBlockVector());
	}
	
	public void addBank(String world, BlockVector vec, BankSet bank) {
		getWorldBanks(world).put(vec, bank);
		markChanged(world, bank);
	}
	
	public void removeBank(String world, BankSet bank) {
		getWorldBanks(world).remove(bank.getMasterSign());
		changed.remove(bank);
		worldCaches.ensure(world).clear();
		worldSection(world).set(bank.getKey(), null);
	}
	
	public void markChanged(String world, BankSet bank) {
		worldCaches.ensure(world).clear();
		changed.add(bank);
	}
	
	private ConfigurationSection ensureSection(ConfigurationSection base, String name) {
		ConfigurationSection section = base.getConfigurationSection(name);
		if (section == null) {
			section = base.createSection(name);
		}
		return section;
	}
	
	private ConfigurationSection worldSection(String world) {
		ConfigurationSection section = ensureSection(config.getConfigurationSection("worlds"), worldHash(world));
		if (!section.contains("_name")) {
			section.set("_name", world);
		}
		return section;
	}
	
	private String worldHash(String world) {
		String hash = worldHashes.get(world);
		if (hash == null) {
			hash = StringTools.md5String(world);
			worldHashes.put(world, hash);
		}
		return hash;
	}
	

}
