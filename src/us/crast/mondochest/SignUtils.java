package us.crast.mondochest;

import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

final class SignUtils {
	private static BlockSearcher searcherZ = new BlockSearcher(0, 1, 2);
	private static BlockSearcher searcherX = new BlockSearcher(2, 1, 0);
	
	public static Vector<Chest> nearbyChests(Sign sign) {
		BlockSearcher searcher;
		switch (signFacing(sign)) {
		case NORTH:
		case SOUTH:
			// North/south faces down the X axis so look for chests along Z
			searcher = searcherZ;
			break;
		case EAST:
		case WEST:
			// East/west faces down the Z axis so look for chests along X
			searcher = searcherX;
			break;
		default:
			return null;
		}
		Vector<Chest> chests = new Vector<Chest>();
		for (Block block: searcher.findBlocks(sign.getBlock(), Material.CHEST)) {
			chests.add((Chest) block.getState());
		}
		return chests;
	}
	public static BlockFace signFacing(Sign sign) {
		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
		return signData.getFacing();
	}
	
	public static Sign signFromBlock(Block block) {
		return (Sign) block.getState();
	}
}
