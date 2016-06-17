package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;

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
		return new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 3)};
	}

	public static List<String> getLore()
	{
		return Lists.newArrayList("Launches Poison arrows.", "Remember: poison heals zombies, skeletons and pigmens!");
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