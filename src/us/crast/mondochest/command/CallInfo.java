package us.crast.mondochest.command;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.crast.mondochest.MondoMessage;
import us.crast.mondochest.Status;

public class CallInfo {
	
	private CommandSender sender;
	private Player player;
	private String[] args;
	private ArrayList<MondoMessage> messages;

	public CallInfo(CommandSender sender, Player player, String[] args) {
		this.sender = sender;
		this.player = player;
		this.args = args;
		this.messages = new ArrayList<MondoMessage>();
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

	public void success(String message) {
		append(new MondoMessage(message, Status.SUCCESS));
	}

	public void append(MondoMessage m) {
		messages.add(m);
	}

	public java.util.Collection<MondoMessage> getMessages() {
		return messages;
	}
}
