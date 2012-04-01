package us.crast.mondochest.util;

import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

public final class SignUtils {
	
	public static Vector<Chest> nearbyChests(Sign sign) {
		return nearbyBlocks(sign, Material.CHEST);
	}
	
	public static Vector<Chest> nearbyBlocks(Sign sign, Material target) {
		Block block = sign.getBlock();
		Vector<Block> searchblocks = new Vector<Block>();
		searchblocks.add(block.getRelative(BlockFace.UP));
		searchblocks.add(block.getRelative(BlockFace.DOWN));
		switch (signFacing(sign)) {
		case NORTH:
		case SOUTH:
			// North/south faces down the X axis so look for chests along Z
			searchblocks.add(block.getRelative(BlockFace.EAST));
			searchblocks.add(block.getRelative(BlockFace.WEST));
			break;
		case EAST:
		case WEST:
			// East/west faces down the Z axis so look for chests along X
			searchblocks.add(block.getRelative(BlockFace.NORTH));
			searchblocks.add(block.getRelative(BlockFace.SOUTH));
			break;
		default:
			return null;
		}
		Vector<Chest> chests = new Vector<Chest>();
		for (Block b: searchblocks) {
			if (b.getType() == target) {
				chests.add((Chest) b.getState());
			}
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
