package us.crast.mondochest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockSearcher {
	private int radiusX;
	private int radiusY;
	private int radiusZ;
	
	public BlockSearcher(int radiusX, int radiusY, int radiusZ) {
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.radiusZ = radiusZ;
	}
	
	public java.util.Vector<Block> findBlocks(Block contextBlock) {
		java.util.Vector<Block> blocksFound = new java.util.Vector<Block>();
		Material targetMaterial = contextBlock.getType();
		Location loc = contextBlock.getLocation().clone();
		World world = loc.getWorld();
		int maxX = loc.getBlockX() + radiusX;
		int maxY = loc.getBlockY() + radiusY;
		int maxZ = loc.getBlockZ() + radiusZ;
		for (int x = loc.getBlockX() - radiusX; x < maxX; x++) {
			for (int y = loc.getBlockY() - radiusY; y < maxY; y++) {
				for (int z = loc.getBlockZ() - radiusZ; z < maxZ; z++) {
					Block block = world.getBlockAt(x, y, z);
					if (block.getType() == targetMaterial) {
						blocksFound.add(block);
					}
				}
			}
		}
		return blocksFound;
	}
}