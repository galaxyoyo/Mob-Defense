package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class HealingTower extends Tower
{
	public HealingTower(Location location)
	{
		super(location);
	}

	public static String getName()
	{
		return "Healing Tower";
	}

	public static ItemStack[] getPrice()
	{
		return new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 1)};
	}

	@Override
	public Material getMaterial()
	{
		return Material.BEACON;
	}

	@Override
	public void onTick()
	{
		launchArrow(10, PotionType.INSTANT_HEAL);
	}
}
