package us.crast.mondochest.util;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class MaterialSet extends HashSet<Material> {
    private static final long serialVersionUID = -1057358565477726082L;

    public MaterialSet(Material ... materials) {
        super();
        for (Material m: materials) {
            this.add(m);
        }
    }
    
    public boolean match(Material candidate) {
        return this.contains(candidate);
    }
    
    public boolean match(BlockState candidate) {
        return this.contains(candidate.getType());
    }

    public boolean match(Block candidate) {
        return this.contains(candidate.getType());
    }
    
    @Override
    public String toString() {
        ArrayList<String> parts = new ArrayList<String>();
        for (Material m : this) {
            parts.add(m.toString());
        }
        if (this.size() == 1) {
            return parts.get(0);
        } else {
            return "One of: " + StringUtils.join(parts, ", ");
        }
    }
}
