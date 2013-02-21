package us.crast.mondochest.util;

import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import us.crast.utils.GenericUtil;

public final class SignUtils {
    private boolean verticalTwo;
    private boolean horizontalTwo;

    public SignUtils(boolean verticalTwo, boolean horizontalTwo) {
        this.verticalTwo = verticalTwo;
        this.horizontalTwo = horizontalTwo;
        
    }
    
	public Vector<Chest> nearbyChests(Sign sign) {
		return nearbyBlocks(sign, Material.CHEST);
	}	
	
	public <T extends BlockState> Vector<T> nearbyBlocks(Sign sign, Material target) {
		Block block = sign.getBlock();
		Vector<Block> searchblocks = new Vector<Block>();
		searchblocks.add(block.getRelative(BlockFace.DOWN));
		searchblocks.add(block.getRelative(BlockFace.UP));
		BlockFace facing = signFacing(sign);
		switch (facing) {
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
		
		// Add chests behind the sign as well
		searchblocks.add(block.getRelative(facing.getOppositeFace()));

		if (verticalTwo) {
            searchblocks.add(block.getRelative(BlockFace.DOWN, 2));
            searchblocks.add(block.getRelative(BlockFace.UP, 2));
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
