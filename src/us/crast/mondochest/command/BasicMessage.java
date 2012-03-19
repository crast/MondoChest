package us.crast.mondochest.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import us.crast.mondochest.MessageWithStatus;
import us.crast.mondochest.Status;

public class BasicMessage implements MessageWithStatus {
	
	private String message;
	private Status status;

	BasicMessage(String message, Status status) {
		this.message = message;
		this.status = status;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	public static String render(MessageWithStatus m, boolean prefix) {
		return render(m.getStatus(), m.getMessage(), prefix);
	}
	
	public static String render(Status status, String format, Object... args) {
		return render(status, String.format(format, args), true);
	}
	
	public static String render(Status status, String message, boolean prefix) {
		ChatColor color = ChatColor.BLACK;
		switch(status) {
		case SUCCESS:
			color = ChatColor.GREEN;
			break;
		case ERROR:
			color = ChatColor.RED;
			break;
		case WARNING:
			color = ChatColor.DARK_RED;
			break;
		case USAGE:
			color = ChatColor.AQUA;
			break;
		}
		return String.format("%sMondoChest: %s%s", ChatColor.GOLD, color, message);
	}
	
	public static void send(CommandSender sender, Status status, String format, Object... args) {
		sender.sendMessage(render(status, format, args));
	}

}
