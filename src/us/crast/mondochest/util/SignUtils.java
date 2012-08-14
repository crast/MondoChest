package us.crast.mondochest.util;

import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

public final class SignUtils {
    public static Vector<Chest> nearbyChests(Sign sign) {
        return nearbyChests(sign, false, false);
    }
    
	public static Vector<Chest> nearbyChests(Sign sign, boolean verticalTwo, boolean horizontalTwo) {
		return nearbyBlocks(sign, Material.CHEST, verticalTwo, horizontalTwo);
	}
	
	public static <T extends BlockState> Vector<T> nearbyBlocks(Sign sign, Material target) {
	    return nearbyBlocks(sign, target, false, false);
	}
	
	public static <T extends BlockState> Vector<T> nearbyBlocks(Sign sign, Material target, boolean verticalTwo, boolean horizontalTwo) {
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
			if (horizontalTwo) {
			    searchblocks.add(block.getRelative(BlockFace.EAST, 2));
			    searchblocks.add(block.getRelative(BlockFace.WEST, 2));
			}
			break;
		case EAST:
		case WEST:
			// East/west faces down the Z axis so look for chests along X
			searchblocks.add(block.getRelative(BlockFace.NORTH));
			searchblocks.add(block.getRelative(BlockFace.SOUTH));
			if (horizontalTwo) {
			    searchblocks.add(block.getRelative(BlockFace.NORTH, 2));
			    searchblocks.add(block.getRelative(BlockFace.SOUTH, 2));
			}
			break;
		default:
			return null;
		}

		if (verticalTwo) {
            searchblocks.add(block.getRelative(BlockFace.UP, 2));
            searchblocks.add(block.getRelative(BlockFace.DOWN, 2));
        }

		Vector<T> targets = new Vector<T>();
		for (Block b: searchblocks) {
			if (b.getType() == target) {
				targets.add(GenericUtil.<T>cast(b.getState()));
			}
		}
		return targets;
	}
	public static BlockFace signFacing(Sign sign) {
		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
		return signData.getFacing();
	}
	
	public static Sign signFromBlock(Block block) {
		return (Sign) block.getState();
	}
}
