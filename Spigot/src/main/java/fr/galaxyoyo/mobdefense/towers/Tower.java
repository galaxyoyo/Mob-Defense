package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.galaxyoyo.mobdefense.MobDefense;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dispenser;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;

public abstract class Tower
{
	private static final Set<Class<? extends Tower>> towerClasses = Sets.newHashSet();
	private static final Map<Location, Tower> towersByLocation = Maps.newHashMap();

	static
	{
		registerTower(SimpleTower.class);
	}

	private final Location location;
	private final Dispenser dispenser;
	private BukkitTask loop;

	protected Tower(Location location)
	{
		this.location = location;
		dispenser = (Dispenser) location.getBlock().getState().getData();
		towersByLocation.put(location, this);
		loop = Bukkit.getScheduler().runTaskTimer(MobDefense.instance(), this::onTick, 10L, 10L);
	}

	public static void registerTower(Class<? extends Tower> clazz)
	{
		towerClasses.add(clazz);
	}

	public static <T extends Tower> T placeAt(Location loc, ItemStack stack)
	{
		Location towerLoc = loc.clone().add(0, 1, 0);
		if (towerLoc.getBlock().getType() != Material.AIR)
			return null;

		Class<T> clazz = null;
		for (Class<? extends Tower> towerClass : towerClasses)
		{
			if (getTowerName(towerClass).equals(stack.getItemMeta().getDisplayName()))
			{
				//noinspection unchecked
				clazz = (Class<T>) towerClass;
				break;
			}
		}

		if (clazz == null)
			return null;

		try
		{
			towerLoc.getBlock().setType(Material.DISPENSER);
			towerLoc.getBlock().getState().setData(loc.getBlock().getState().getData());
			T tower = clazz.getConstructor(Location.class).newInstance(towerLoc);
			loc.getBlock().setType(tower.getMaterial());
			towersByLocation.put(towerLoc, tower);
			return tower;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static String getTowerName(Class<? extends Tower> clazz)
	{
		try
		{
			return (String) clazz.getDeclaredMethod("getName").invoke(null);
		}
		catch (Exception ex)
		{
			throw new UnsupportedOperationException("Class '" + clazz + "' must contain a public static method named 'getName' with no parameter that returns a String.");
		}
	}

	public abstract Material getMaterial();

	public static ItemStack[] getTowerPrice(Class<? extends Tower> clazz)
	{
		try
		{
			return (ItemStack[]) clazz.getDeclaredMethod("getPrice").invoke(null);
		}
		catch (Exception ex)
		{
			throw new UnsupportedOperationException("Class '" + clazz + "' must contain a public static method named 'getPrice' with no parameter that returns an array of ItemStack.");
		}
	}

	public static void breakAt(Location loc)
	{
		Tower t = towersByLocation.get(loc);
		if (t == null)
			t = towersByLocation.get(loc.clone().add(0, 1, 0));
		if (t == null)
			return;

		t.getLocation().getBlock().setType(Material.AIR);
		t.getLocation().clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);

		ItemStack stack = new ItemStack(Material.DISPENSER);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(getTowerName(t.getClass()));
		stack.setItemMeta(meta);
		loc.getBlock().breakNaturally(stack);
		t.loop.cancel();
	}

	public Location getLocation()
	{
		return location;
	}

	public static Set<Class<? extends Tower>> getTowerClasses()
	{
		return towerClasses;
	}

	public abstract void onTick();

	public void onBreak()
	{
	}

	public void launchArrow(int range)
	{
		launchArrow(range, null);
	}

	public void launchArrow(int range, PotionType type)
	{
		/*((CraftDispenser) getLocation().getBlock().getState()).dispense();
		Vector vec;
		if (getDispenser().getFacing() == BlockFace.NORTH)
			vec = new Vector(range, 0, 0);
		else if (getDispenser().getFacing() == BlockFace.WEST)
			vec = new Vector(0, 0, -range);
		else if (getDispenser().getFacing() == BlockFace.SOUTH)
			vec = new Vector(-range, 0, 0);
		else
			vec = new Vector(0, 0, range);
		Arrow arrow;
		if (type == null)
			arrow = getLocation().getWorld().spawnArrow(getLocation(), vec, 1.0F, 12.0F);
		else
		{
			arrow = getLocation().getWorld().spawnArrow(getLocation().add(new Vector(vec.getX() / range, 0, vec.getZ() / range)), vec, 1.0F, 12.0F, TippedArrow.class);
			((TippedArrow) arrow).setBasePotionData(new PotionData(type));
		}*/

		DispenseBehaviorProjectile projectile = new DispenseBehaviorProjectile()
		{
			@Override
			protected IProjectile a(World world, IPosition pos, net.minecraft.server.v1_10_R1.ItemStack itemStack)
			{
				return new EntityTippedArrow(world);
			}
		};
		projectile.b(new SourceBlock(((CraftWorld) getLocation().getWorld()).getHandle(), new BlockPosition(getLocation().getBlockX(), getLocation().getBlockY(), getLocation()
				.getBlockZ())), new net.minecraft.server.v1_10_R1.ItemStack(Items.ARROW));
	}

	public Dispenser getDispenser()
	{
		return dispenser;
	}
}
