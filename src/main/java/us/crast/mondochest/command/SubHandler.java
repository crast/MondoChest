package us.crast.mondochest.command;

import us.crast.chatmagic.MondoMessage;

public interface SubHandler {

	public void handle(CallInfo call) throws MondoMessage;
}
