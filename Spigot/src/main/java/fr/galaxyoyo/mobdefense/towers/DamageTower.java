package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;

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

	public static List<String> getLore()
	{
		return Lists.newArrayList("Launches Instant Damage arrows.", "Remember: instant damage heals zombies, skeletons and pigmens!");
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
