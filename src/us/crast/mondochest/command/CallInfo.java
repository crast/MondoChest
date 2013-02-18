package us.crast.mondochest.command;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.crast.chatmagic.BasicMessage;
import us.crast.chatmagic.MessageWithStatus;
import us.crast.chatmagic.Status;

public class CallInfo {
	
	private CommandSender sender;
	private Player player;
	private String[] args;
	private ArrayList<MessageWithStatus> messages;

	public CallInfo(CommandSender sender, Player player, String[] args) {
		this.sender = sender;
		this.player = player;
		this.args = args;
		this.messages = new ArrayList<MessageWithStatus>();
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
	
	public String[] getArgs() {
	    return this.args;
	}
	
	public int maxArgNum() {
	    return this.args.length - 1;
	}

	public void success(String message) {
		append(new BasicMessage(message, Status.SUCCESS));
	}

	public void append(MessageWithStatus m) {
		messages.add(m);
	}

	public java.util.Collection<MessageWithStatus> getMessages() {
		return messages;
	}
}
