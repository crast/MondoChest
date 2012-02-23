package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class DirectionalStrings {
	private static Map<String, BlockFace> dirs = new HashMap<String, BlockFace>();
	
	public static Block parseDirectional(Block context, String directioninfo) throws MondoMessage {
		String[] directional = directioninfo.split(" ");
		if ((directional.length % 2) != 0) {
			throw new MondoMessage("Directional info specification is invalid");
		}
		for (int i = 0; i < directional.length; i+=2) {
			BlockFace desired_face = dirs.get(directional[i].toUpperCase());
			if (desired_face == null) {
				throw new MondoMessage(String.format("Direction must be one of up/down/north/south/east/west, got '{0}'", directional[i]));
			}
			int distance = Integer.parseInt(directional[i+1]);
			if (distance < 0) {
				distance = -distance;
				desired_face = desired_face.getOppositeFace();
			}
			context = context.getRelative(desired_face, distance);
		}
		return context;
	}
	
	static {
		dirs.put("UP", BlockFace.UP);
		dirs.put("DOWN", BlockFace.DOWN);
		dirs.put("EAST", BlockFace.EAST);
		dirs.put("WEST", BlockFace.WEST);
		dirs.put("NORTH", BlockFace.NORTH);
		dirs.put("SOUTH", BlockFace.SOUTH);
	}
}
