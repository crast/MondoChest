package us.crast.mondochest.command;

import us.crast.mondochest.MondoMessage;

public interface SubHandler {

	public void handle(CallInfo call) throws MondoMessage;
}
