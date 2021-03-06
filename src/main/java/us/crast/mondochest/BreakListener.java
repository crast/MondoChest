package us.crast.mondochest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Sign;

import us.crast.chatmagic.MessageWithStatus;
import us.crast.chatmagic.MondoMessage;
import us.crast.mondochest.util.SignUtils;

public final class BreakListener implements Listener {
	private MondoListener listener;
	
	public BreakListener(MondoListener listener) {
		this.listener = listener;
	}
	
	@EventHandler(ignoreCancelled=true)
    public void blockBroken(BlockBreakEvent event) {
		Block block = event.getBlock();
		MessageWithStatus message = null;
		Player player = event.getPlayer();
		try {
    		switch (block.getType()) {
    		case WALL_SIGN:
    	        Sign sign = SignUtils.signFromBlock(block);
    	        switch (MondoSign.match(sign.getLine(0))) {
    	        case MASTER:
    	            message = this.listener.masterBroken(event, sign, player);
    	            break;
    	        case SLAVE:
    	            message = this.listener.slaveBroken(event, sign, player);
    	            break;
    	        }		    
    		    break;
    		case CHEST:
    		case TRAPPED_CHEST:
    		    message = this.listener.chestBroken(event, block, player);
    		    break;
    		default:
    		    return;
    		}
		} catch (MondoMessage e) {
		    message = e;
    	}
		if (message != null) {
		    player.sendMessage(message.render(true));
		}
	}
	
}
