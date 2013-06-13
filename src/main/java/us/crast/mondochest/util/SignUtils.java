package us.crast.mondochest.util;

import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import us.crast.mondochest.MondoConstants;
import us.crast.utils.GenericUtil;

public final class SignUtils {
    private boolean verticalTwo;
    private boolean horizontalTwo;
    private boolean signInFront;

    public SignUtils(boolean verticalTwo, boolean horizontalTwo, boolean signInFront) {
        this.verticalTwo = verticalTwo;
        this.horizontalTwo = horizontalTwo;
        this.signInFront = signInFront;
        
    }
    
	public Vector<Chest> nearbyChests(Sign sign) {
		return nearbyBlocks(sign, MondoConstants.CHEST_MATERIALS);
	}	
	
	public <T extends BlockState> Vector<T> nearbyBlocks(Sign sign, MaterialSet materials) {
		Block block = sign.getBlock();
		Vector<Block> searchblocks = new Vector<Block>();
		searchblocks.add(block.getRelative(BlockFace.DOWN));
		searchblocks.add(block.getRelative(BlockFace.UP));
		BlockFace facing = signFacing(sign);
		addRelativeToFacing(searchblocks, block, facing, 1);
		if (horizontalTwo) {
		    addRelativeToFacing(searchblocks, block, facing, 2);
		}
		// Add chests directly behind the sign as well
		Block behind = block.getRelative(facing.getOppositeFace());
		searchblocks.add(behind);

		if (verticalTwo) {
            searchblocks.add(block.getRelative(BlockFace.DOWN, 2));
            searchblocks.add(block.getRelative(BlockFace.UP, 2));
        }

        if (signInFront) {
            searchblocks.add(behind.getRelative(BlockFace.DOWN));
            searchblocks.add(behind.getRelative(BlockFace.UP));
            addRelativeToFacing(searchblocks, behind, facing, 1);
        }

		Vector<T> targets = new Vector<T>();
		for (Block b: searchblocks) {
			if (materials.contains(b.getType())) {
				targets.add(GenericUtil.<T>cast(b.getState()));
			}
		}
		return targets;
	}

    /**
     * Add blocks to the search results which are sideways to the provided facing.
     * @param results A vector of search results, which will be modified.
     * @param basis The block which is the basis of where to get relative blocks from.
     * @param signFacing The direction the sign is facing.
     * @param distance How far away on each side.
     */
    private void addRelativeToFacing(Vector<Block> results, Block basis, BlockFace signFacing, int distance) {
        switch (signFacing) {
		case NORTH:
		case SOUTH:
			// North/south faces down the X axis so look for chests along Z
			results.add(basis.getRelative(BlockFace.EAST, distance));
			results.add(basis.getRelative(BlockFace.WEST, distance));
			break;
		case EAST:
		case WEST:
			// East/west faces down the Z axis so look for chests along X
			results.add(basis.getRelative(BlockFace.NORTH, distance));
			results.add(basis.getRelative(BlockFace.SOUTH, distance));
			break;
		default:
		}
    }
	
	public static BlockFace signFacing(Sign sign) {
		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
		return signData.getFacing();
	}
	
	public static Sign signFromBlock(Block block) {
		return (Sign) block.getState();
	}
}
