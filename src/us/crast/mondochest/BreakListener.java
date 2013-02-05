package us.crast.mondochest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Sign;

import us.crast.mondochest.command.BasicMessage;
import us.crast.mondochest.util.SignUtils;

public final class BreakListener implements Listener {
	private static final String MASTER_SIGN_NAME = MondoConstants.MASTER_SIGN_NAME;
	private static final String SLAVE_SIGN_NAME = MondoConstants.SLAVE_SIGN_NAME;
	
	private MondoListener listener;
	
	public BreakListener(MondoListener listener) {
		this.listener = listener;
	}
	
	@EventHandler(ignoreCancelled=true)
    public void blockBroken(BlockBreakEvent event) {
		Block block = event.getBlock();
		MessageWithStatus message = null;
		Player player = event.getPlayer();
		switch (block.getType()) {
		case WALL_SIGN:
	        Sign sign = SignUtils.signFromBlock(block);
	        String firstLine = sign.getLine(0);
	        if (firstLine.equals(MASTER_SIGN_NAME)) {
	            message = this.listener.masterBroken(event, sign, player);
	        } else if (firstLine.equals(SLAVE_SIGN_NAME)) {
	            message = this.listener.slaveBroken(event, sign, player);
	        }		    
		    break;
		case CHEST:
		    message = this.listener.chestBroken(event, block, player);
		    break;
		default:
		    return;
		}
		if (message != null) {
		    player.sendMessage(BasicMessage.render(message, true));
		}
	}
	
}
