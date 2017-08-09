package io.github.bananapuncher714;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LeafLocation implements Comparable<LeafLocation> {
	private Location location;
	private boolean brokenByPlayer = false;
	private ItemStack item;
	private Location origin;
	private Material material;
	private byte data;
	
	public LeafLocation( Location l, ItemStack i, Location o, Material m, byte b ) {
		location = l;
		item = i;
		origin = o;
		material = m;
		data = b;
	}
	
	public LeafLocation ( Location l, ItemStack i, Location o, Material m, byte d, boolean b ) {
		brokenByPlayer = b;
		location = l;
		item = i;
		origin = o;
		material = m;
		data = d;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Location getOrigin() {
		return origin;
	}
	
	public Material getBlock() {
		return material;
	}

	public byte getData() {
		return data;
	}
	
	@Override
	public int compareTo(LeafLocation l) {
		if ( l.getLocation() == location ) {
			return 0;
		} else if ( brokenByPlayer ) {
			return -1;
		} else {
			return 1;
		}
		// TODO Auto-generated method stub
	}

}
