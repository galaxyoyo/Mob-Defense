package fr.galaxyoyo.mobdefense;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.ItemStack;

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

	@Override
	public Material getMaterial()
	{
		return Material.WOOD;
	}

	@Override
	public void onTick()
	{
		Dispenser d = (Dispenser) getLocation().getBlock().getState();
		d.getInventory().addItem(new ItemStack(Material.ARROW));
		d.dispense();
	}
}
