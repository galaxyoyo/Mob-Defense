package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
