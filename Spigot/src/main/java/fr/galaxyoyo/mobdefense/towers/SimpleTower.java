package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SimpleTower extends Tower
{
	public SimpleTower(Location location)
	{
		super(location);
	}

	public static String getName()
	{
		return "Simple Tower";
	}

	public static ItemStack[] getPrice()
	{
		return new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 5)};
	}

	public static List<String> getLore()
	{
		return Lists.newArrayList("Launches basic arrows once every 1/2 second.", "It is the most basic tower you can find, but not the cheapest :)");
	}

	@Override
	public Material getMaterial()
	{
		return Material.WOOD;
	}

	@Override
	public void onTick()
	{
		launchArrow(10);
	}
}
