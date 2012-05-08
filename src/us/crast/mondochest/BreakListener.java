package us.crast.mondochest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Sign;

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
		if (block.getType() != Material.WALL_SIGN) return;
		Sign sign = SignUtils.signFromBlock(block);
		String firstLine = sign.getLine(0);
		if (firstLine.equals(MASTER_SIGN_NAME)) {
			this.listener.masterBroken(event, sign, event.getPlayer());
		} else if (firstLine.equals(SLAVE_SIGN_NAME)) {
			this.listener.slaveBroken(event, sign, event.getPlayer());
			
		}
	}
	
}
