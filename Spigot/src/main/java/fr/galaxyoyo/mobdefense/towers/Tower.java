package fr.galaxyoyo.mobdefense.towers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.galaxyoyo.mobdefense.MobDefense;
import fr.galaxyoyo.mobdefense.NMSUtils;
import fr.galaxyoyo.mobdefense.Wave;
import fr.galaxyoyo.mobdefense.upgrades.Upgrade;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
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
	private int currentTick = 0;
	private double updateRate = 20;
	private float rangeMultiplier = 1.0F;
	private float speedMultiplier = 1.0F;
	private List<Upgrade> upgrades = Lists.newArrayList(null, null, null, null, null, null, null, null, null);

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
				tower.loop = Bukkit.getScheduler().runTaskTimer(MobDefense.instance(), tower::onTick0, 1L, 1L);

				boolean build = true;
				if (!Wave.checkForPath())
					build = false;
				else
				{
					for (Creature c : Wave.getAllCreatures())
					{
						if (!Wave.recalculate(c))
						{
							build = false;
							break;
						}
					}

					if (!build)
					{
						breakAt(tower.location);
						Bukkit.getScheduler().runTask(MobDefense.instance(), () -> Wave.getAllCreatures().forEach(Wave::recalculate));
					}
				}
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

	public static Tower getTowerAt(Location location)
	{
		return towersByLocation.get(location);
	}

	public double getUpdateRate()
	{
		return updateRate;
	}

	public void setUpdateRate(double ticks)
	{
		this.updateRate = Math.floor(ticks * 100000.0D) / 100000.0D;
	}

	public float getRangeMultiplier()
	{
		return rangeMultiplier;
	}

	public void setRangeMultiplier(float multiplier)
	{
		this.rangeMultiplier = (float) (Math.floor(multiplier * 100000.0D) / 100000.0D);
	}

	public float getSpeedMultiplier()
	{
		return speedMultiplier;
	}

	public void setSpeedMultiplier(float multiplier)
	{
		this.speedMultiplier = (float) (Math.floor(multiplier * 100000.0D) / 100000.0D);
	}

	private void onTick0()
	{
		if (++currentTick >= updateRate)
		{
			getUpgrades().stream().filter(upgrade -> upgrade != null).forEach(Upgrade::onTowerTick);
			onTick();
			currentTick = 0;
		}
	}

	public List<Upgrade> getUpgrades()
	{
		return upgrades;
	}

	public abstract void onTick();

	public abstract void load(Map<String, Object> parameters);

	public Upgrade getUpgrade(int slot)
	{
		return upgrades.get(slot);
	}

	public void setUpgrade(int slot, Upgrade upgrade)
	{
		upgrades.set(slot, upgrade);
	}

	@SuppressWarnings("unused")
	public <T extends Arrow> T launchArrow(float range)
	{
		return launchArrow(range, null);
	}

	public <T extends Arrow> T launchArrow(float range, PotionType type)
	{
		return launchArrow(range, type, false, false);
	}

	public <T extends Arrow> T launchArrow(float range, PotionType type, boolean extended, boolean upgraded)
	{
		return launchArrow(range, type, extended, upgraded, -1);
	}

	@SuppressWarnings("unchecked")
	public <T extends Arrow> T launchArrow(float range, PotionType type, boolean extended, boolean upgraded, int spectralGlowingTicks)
	{
		if (NMSUtils.getServerVersion().isBefore1_9())
		{
			type = null;
			spectralGlowingTicks = -1;
		}
		BlockFace face = getDispenser().getFacing();
		Class<T> clazz;
		if (type != null)
			clazz = (Class<T>) TippedArrow.class;
		else if (spectralGlowingTicks > 0)
			clazz = (Class<T>) SpectralArrow.class;
		else
			clazz = (Class<T>) Arrow.class;
		range *= rangeMultiplier / speedMultiplier;
		Location loc = location.clone().add(face.getModX() + 0.5D, 0.5D, face.getModZ() + 0.5D);
		Vector vec = new Vector((range - 1) * face.getModX(), 0, (range - 1) * face.getModZ());
		T arrow;
		if (clazz == Arrow.class)
			arrow = (T) location.getWorld().spawnArrow(loc, vec, speedMultiplier, -2);
		else
			arrow = location.getWorld().spawnArrow(loc, vec, speedMultiplier, -2, clazz);
		if (type != null)
			((TippedArrow) arrow).setBasePotionData(new PotionData(type, type.isExtendable() && extended, type.isUpgradeable() && upgraded));
		else if (spectralGlowingTicks > 0)
			((SpectralArrow) arrow).setGlowingTicks((int) (spectralGlowingTicks * (extended ? 2.5D : 1.0D)));
		getUpgrades().stream().filter(upgrade -> upgrade != null).forEach(upgrade -> upgrade.onTowerLaunchArrow(arrow));
		return arrow;
	}

	public Dispenser getDispenser()
	{
		return dispenser;
	}

	@SuppressWarnings("unused")
	public SpectralArrow launchSpectralArrow(float range)
	{
		return launchSpectralArrow(range, 200);
	}

	public SpectralArrow launchSpectralArrow(float range, int glowingTicks)
	{
		return launchArrow(range, null, false, false, glowingTicks);
	}
}
