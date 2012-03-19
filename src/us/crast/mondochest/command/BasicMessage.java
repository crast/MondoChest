package us.crast.mondochest.command;

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

}
