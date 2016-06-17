package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;

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

	public static List<String> getLore()
	{
		return Lists.newArrayList("Launches Instant Healing arrows.", "Remember: Instant Healing deals damage to zombies, skeletons and pigmens!");
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
