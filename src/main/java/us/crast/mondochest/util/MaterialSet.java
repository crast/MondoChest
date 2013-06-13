package us.crast.mondochest.util;

import java.util.HashSet;

import org.bukkit.Material;

public class MaterialSet extends HashSet<Material> {
    private static final long serialVersionUID = -1057358565477726082L;

    public MaterialSet(Material ... materials) {
        super();
        for (Material m: materials) {
            this.add(m);
        }
    }

}
