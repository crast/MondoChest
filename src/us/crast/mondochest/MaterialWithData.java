package us.crast.mondochest;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialWithData {
	private Material material;
	private byte data;
	
	MaterialWithData(ItemStack stack) {
		this.material = stack.getType();
		this.data = stack.getData().getData();
	}
	
	/* Object primitives */
	@Override
	public int hashCode() {
		return material.hashCode() ^ (data * 31);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof MaterialWithData) {
			MaterialWithData o = (MaterialWithData) other;
			return this.material.equals(o.getMaterial()) && (this.data == o.getData());
		} else {
			return false;
		}
	}
	
	/* Getters and setters */
	public Material getMaterial() {
		return material;
	}

	public byte getData() {
		return data;
	}
}
