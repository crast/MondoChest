package us.crast.mondochest.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CallInfo {
	
	private CommandSender sender;
	private Player player;
	private String[] args;

	public CallInfo(CommandSender sender, Player player, String[] args) {
		this.sender = sender;
		this.player = player;
		this.args = args;
	}

	public Player getPlayer() {
		return player;
	}

	public CommandSender getSender() {
		return sender;
	}
	
	public String getArg(int n) {
		return this.args[n];
	}
}
