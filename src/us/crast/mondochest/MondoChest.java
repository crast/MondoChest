package us.crast.mondochest;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class MondoChest extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	MondoListener listener = null;

	public void onEnable(){
		log.info("Loaded MondoChest v1");
		if (listener == null) listener = new MondoListener(log);
		getServer().getPluginManager().registerEvents(listener, this);

	}
	public void onDisable(){
		
	}
	
}
