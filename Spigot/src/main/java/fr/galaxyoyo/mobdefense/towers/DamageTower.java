package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class DamageTower extends Tower
{
	public DamageTower(Location location)
	{
		super(location);
	}

	public static String getName()
	{
		return "Damage Tower";
	}

	public static ItemStack[] getPrice()
	{
		return new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 3)};
	}

	@Override
	public Material getMaterial()
	{
		return Material.NETHER_WART_BLOCK;
	}

	@Override
	public void onTick()
	{
		launchArrow(10, PotionType.INSTANT_DAMAGE);
	}
}
