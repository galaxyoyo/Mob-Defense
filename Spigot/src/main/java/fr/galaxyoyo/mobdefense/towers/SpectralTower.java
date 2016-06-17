package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SpectralTower extends Tower
{
	public SpectralTower(Location location)
	{
		super(location);
	}

	public static String getName()
	{
		return "Spectral Tower";
	}

	public static ItemStack[] getPrice()
	{
		return new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 7)};
	}

	public static List<String> getLore()
	{
		return Lists.newArrayList("Launches basic spectral arrows.", "It's not very useful, but it looks cool ...");
	}

	@Override
	public Material getMaterial()
	{
		return Material.MAGMA;
	}

	@Override
	public void onTick()
	{
		launchSpectralArrow(10);
	}
}
