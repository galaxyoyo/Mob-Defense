package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.galaxyoyo.mobdefense.MobDefense;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

public abstract class Tower
{
	private static final List<Class<? extends Tower>> towerClasses = Lists.newArrayList();
	private static final Map<Location, Tower> towersByLocation = Maps.newHashMap();

	static
	{
		registerTower(SimpleTower.class);
		registerTower(DamageTower.class);
		registerTower(PoisonTower.class);
	}

	private final Location location;
	private final Dispenser dispenser;
	private BukkitTask loop;

	protected Tower(Location location)
	{
		this.location = location;
		dispenser = (Dispenser) location.getBlock().getState().getData();
		towersByLocation.put(location, this);
		loop = Bukkit.getScheduler().runTaskTimer(MobDefense.instance(), this::onTick, 20L, 20L);
	}

	public static void registerTower(Class<? extends Tower> clazz)
	{
		towerClasses.add(clazz);
	}

	public static boolean placeAt(Location loc, ItemStack stack)
	{
		Location towerLoc = loc.clone().add(0, 1, 0);
		if (towerLoc.getBlock().getType() != Material.AIR)
			return false;

		Class<? extends Tower> clazz = null;
		for (Class<? extends Tower> towerClass : towerClasses)
		{
			if (getTowerName(towerClass).equals(stack.getItemMeta().getDisplayName()))
			{
				//noinspection unchecked
				clazz = towerClass;
				break;
			}
		}

		if (clazz == null)
			return false;

		try
		{
			//noinspection deprecation
			byte data = loc.getBlock().getState().getData().getData();
			Class<? extends Tower> finalClazz = clazz;
			Bukkit.getScheduler().runTask(MobDefense.instance(), () -> {
				//noinspection deprecation
				towerLoc.getBlock().setTypeIdAndData(Material.DISPENSER.getId(), data, true);
				Tower tower = null;
				try
				{
					tower = finalClazz.getConstructor(Location.class).newInstance(towerLoc);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return;
				}
				loc.getBlock().setType(tower.getMaterial());
				towersByLocation.put(towerLoc, tower);
			});
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
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

	public static Tower breakAt(Location loc)
	{
		Tower t = towersByLocation.get(loc);
		if (t == null)
			t = towersByLocation.get(loc.clone().add(0, 1, 0));
		if (t == null)
			return null;

		t.getLocation().getBlock().setType(Material.AIR);
		t.getLocation().clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);
		t.loop.cancel();
		return t;
	}

	public Location getLocation()
	{
		return location;
	}

	public static List<Class<? extends Tower>> getTowerClasses()
	{
		return towerClasses;
	}

	public abstract void onTick();

	public Arrow launchArrow(int range)
	{
		return launchArrow(range, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends Arrow> T launchArrow(int range, PotionType type)
	{
		BlockFace face = getDispenser().getFacing();
		Class<T> clazz;
		if (type == null)
			clazz = (Class<T>) Arrow.class;
		else
			clazz = (Class<T>) TippedArrow.class;
		T arrow = location.getWorld().spawnArrow(location.clone().add(face.getModX() + 0.5D, 0.5D, face.getModZ() + 0.5D), new Vector((range - 1) * face.getModX(), 0,
				(range - 1) * face.getModZ()), 1, -2, clazz);
		if (type != null)
			((TippedArrow) arrow).setBasePotionData(new PotionData(type));
		arrow.setTicksLived(5900);
		return arrow;
	}

	public Dispenser getDispenser()
	{
		return dispenser;
	}
}