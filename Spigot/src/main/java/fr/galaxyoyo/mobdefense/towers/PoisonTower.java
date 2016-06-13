package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class PoisonTower extends Tower
{
	public PoisonTower(Location location)
	{
		super(location);
	}

	public static String getName()
	{
		return "Poison Tower";
	}

	public static ItemStack[] getPrice()
	{
		return new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 5)};
	}

	@Override
	public Material getMaterial()
	{
		return Material.SLIME_BLOCK;
	}

	@Override
	public void onTick()
	{
		launchArrow(10, PotionType.POISON);
	}
}
