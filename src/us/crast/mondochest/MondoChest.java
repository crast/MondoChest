package us.crast.mondochest;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MondoChest extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	private MondoListener listener = null;

	public void onEnable() {
		if (!(new File(this.getDataFolder(), "config.yml").exists())) { 
			saveDefaultConfig();
		}
		MondoConfig.configure(getConfig(), log);
		if (listener == null) listener = new MondoListener(log, getSearcherFromConfig());
		getServer().getPluginManager().registerEvents(listener, this);
		log.info("[MondoChest] MondoChest v0.4.1 ready");

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
	
}
