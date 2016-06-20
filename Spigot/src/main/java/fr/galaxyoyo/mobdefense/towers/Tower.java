package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.galaxyoyo.mobdefense.MobDefense;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.SpectralArrow;
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
	private static final List<TowerRegistration> towerRegistrations = Lists.newArrayList();
	private static final Map<Location, Tower> towersByLocation = Maps.newHashMap();

	private final Location location;
	private final TowerRegistration registration;
	private Dispenser dispenser;
	private BukkitTask loop;

	protected Tower(TowerRegistration registration, Location location)
	{
		this.registration = registration;
		this.location = location;
		towersByLocation.put(location, this);
	}

	public static void registerTower(TowerRegistration registration)
	{
		if (registration.register())
			towerRegistrations.add(registration);
	}

	public static Tower placeAt(Location loc, ItemStack stack)
	{
		Location towerLoc = loc.clone().add(0, 1, 0);
		if (towerLoc.getBlock().getType() != Material.AIR)
			return null;

		TowerRegistration registration = null;
		for (TowerRegistration tr : towerRegistrations)
		{
			if (tr.getDisplayName().equals(stack.getItemMeta().getDisplayName()))
			{
				registration = tr;
				break;
			}
		}

		if (registration == null)
			return null;

		try
		{
			//noinspection deprecation
			byte data = loc.getBlock().getState().getData().getData();
			Tower tower = registration.newInstance(towerLoc);
			towersByLocation.put(towerLoc, tower);
			Bukkit.getScheduler().runTask(MobDefense.instance(), () -> {
				//noinspection deprecation
				towerLoc.getBlock().setTypeIdAndData(Material.DISPENSER.getId(), data, true);
				loc.getBlock().setType(tower.getRegistration().getMaterial());
				tower.dispenser = (Dispenser) tower.getLocation().getBlock().getState().getData();
				tower.loop = Bukkit.getScheduler().runTaskTimer(MobDefense.instance(), tower::onTick, 20L, 20L);
			});
			return tower;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public TowerRegistration getRegistration()
	{
		return registration;
	}

	public Location getLocation()
	{
		return location;
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
		if (t.loop != null)
			t.loop.cancel();
		return t;
	}

	public static List<TowerRegistration> getTowerRegistrations()
	{
		return towerRegistrations;
	}

	public static List<Tower> getAllTowers()
	{
		return Lists.newArrayList(towersByLocation.values());
	}

	public abstract void onTick();

	public TippedArrow launchArrow(int range)
	{
		return launchArrow(range, null);
	}

	public TippedArrow launchArrow(int range, PotionType type)
	{
		return launchArrow(range, type, false, false);
	}

	public TippedArrow launchArrow(int range, PotionType type, boolean extended, boolean upgraded)
	{
		return launchArrow(range, type, extended, upgraded, false);
	}

	@SuppressWarnings("unchecked")
	public <T extends Arrow> T launchArrow(int range, PotionType type, boolean extended, boolean upgraded, boolean spectral)
	{
		BlockFace face = getDispenser().getFacing();
		Class<T> clazz;
		if (type != null)
			clazz = (Class<T>) TippedArrow.class;
		else if (spectral)
			clazz = (Class<T>) SpectralArrow.class;
		else
			clazz = (Class<T>) Arrow.class;
		T arrow = location.getWorld().spawnArrow(location.clone().add(face.getModX() + 0.5D, 0.5D, face.getModZ() + 0.5D), new Vector((range - 1) * face.getModX(), 0,
				(range - 1) * face.getModZ()), 1, -2, clazz);
		if (type != null)
			((TippedArrow) arrow).setBasePotionData(new PotionData(type, extended, upgraded));
		return arrow;
	}

	public Dispenser getDispenser()
	{
		return dispenser;
	}

	public SpectralArrow launchSpectralArrow(int range)
	{
		return launchArrow(range, null, false, false, true);
	}
}
