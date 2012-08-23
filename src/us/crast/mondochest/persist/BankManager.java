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

import us.crast.datastructures.DefaultDict;
import us.crast.mondochest.BankSet;
import us.crast.mondochest.ChestManager;
import us.crast.mondochest.MondoChest;
import us.crast.mondochest.MondoConfig;
import us.crast.mondochest.MondoMessage;
import us.crast.utils.FileTools;
import us.crast.utils.StringTools;

public final class BankManager {
	private final Map<String, Map<BlockVector, BankSet>> banks = new HashMap<String, Map<BlockVector, BankSet>>();
	private final Map<String, String> worldHashes = new HashMap<String, String>();
	private final DefaultDict<String, WorldCache> worldCaches = new DefaultDict<String, WorldCache>(WorldCache.getMaker());
	private final Set<BankSet> changed = new HashSet<BankSet>();
	private boolean should_save = false;
	private final File bankFile;
	private FileConfiguration config;
	
	public BankManager(final MondoChest plugin) {
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
		if (MondoConfig.getDecodeErrors() != null && !MondoConfig.getDecodeErrors().isEmpty()) {
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
			should_save = true;
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
		should_save = false;
	}

	public void saveIfNeeded() throws MondoMessage {
		if (needsSave()) save();
	}

	public Map<String, Map<BlockVector, BankSet>> getBanks() {
		return banks;
	}
	
	public Map<BlockVector, BankSet> getWorldBanks(final String world) {
		Map<BlockVector, BankSet> banksByCoords = banks.get(world);
		if (banksByCoords == null) {
			banksByCoords = new HashMap<BlockVector, BankSet>();
			banks.put(world, banksByCoords);
		}
		return banksByCoords;
	}
	
	public Map<ChestManager, BankSet> getWorldSlaves(final String world) {
		return worldCaches.ensure(world).getSlaves(this);
	}
	
	public Map<BlockVector, BankSet> getChestLocMap(final String world) {
	    return worldCaches.ensure(world).getChestLocMap(this);
	}
	
	public BankSet getBank(final String world, final BlockVector vec) {
		return getWorldBanks(world).get(vec);
	}
	
	public BankSet getBank(final Location loc) {
		return getBank(loc.getWorld().getName(), loc.toVector().toBlockVector());
	}
	
	public void addBank(final String world, final BlockVector vec, final BankSet bank) {
		getWorldBanks(world).put(vec, bank);
		bank.setWorld(world);
		markChanged(world, bank);
	}
	
	public void removeBank(final String world, final BankSet bank) {
		getWorldBanks(world).remove(bank.getMasterSign());
		changed.remove(bank);
		worldCaches.ensure(world).clear();
		worldSection(world).set(bank.getKey(), null);
	}
	
	public void markChanged(final String world, final BankSet bank) {
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
	
	private ConfigurationSection worldSection(final String world) {
		ConfigurationSection section = ensureSection(config.getConfigurationSection("worlds"), worldHash(world));
		if (!section.contains("_name")) {
			section.set("_name", world);
		}
		return section;
	}
	
	private String worldHash(final String world) {
		String hash = worldHashes.get(world);
		if (hash == null) {
			hash = StringTools.md5String(world);
			worldHashes.put(world, hash);
		}
		return hash;
	}
	
	public void shutdown() {
		banks.clear();
		worldHashes.clear();
		worldCaches.clear();
		changed.clear();
		config = null;
	}
	
	private boolean needsSave() {
		return (should_save || !changed.isEmpty());
	}

}
