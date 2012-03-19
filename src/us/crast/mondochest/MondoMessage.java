package us.crast.mondochest;

public class MondoMessage extends Exception {
	private static final long serialVersionUID = 8977396906672876450L;
	private Status status;

	public MondoMessage(String message, Status status) {
		super(message);
		this.status = status;
	}
	
	public MondoMessage(String message) {
		this(message, Status.SUCCESS);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
