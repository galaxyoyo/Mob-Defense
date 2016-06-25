package fr.galaxyoyo.mobdefense;

import com.adamki11s.pathing.AStar;
import com.adamki11s.pathing.PathingResult;
import com.adamki11s.pathing.Tile;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.galaxyoyo.mobdefense.events.EntityGoneEvent;
import fr.galaxyoyo.spigot.nbtapi.EntityUtils;
import fr.galaxyoyo.spigot.nbtapi.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Wave implements Serializable
{
	private static Map<Creature, MobClass> creatureClasses = Maps.newHashMap();
	private static Map<Wave, Set<Creature>> waves = Maps.newHashMap();
	private static Map<Creature, Wave> wavesByCreature = Maps.newHashMap();
	private int number = -1;
	private Map<MobClass, Double> spawns = Maps.newHashMap();
	private Map<Creature, List<Tile>> creatureTiles = Maps.newHashMap();
	private Map<Creature, Integer> creatureCurrentTile = Maps.newHashMap();
	private Map<Creature, Location> starts = Maps.newHashMap();

	public static Set<Creature> getAllCreatures()
	{
		Set<Creature> allCreatures = Sets.newHashSet();
		waves.values().forEach(allCreatures::addAll);
		return allCreatures;
	}

	public static MobClass getClass(Creature c)
	{
		return creatureClasses.get(c);
	}

	public static boolean checkForPath()
	{
		try
		{
			AStar as = new AStar(MobDefense.instance().getSpawn().clone().subtract(0, 1, 0), MobDefense.instance().getEnd().clone().subtract(0, 1, 0), 100);
			List<Tile> list = as.iterate();
			return list != null && !list.isEmpty() && as.getPathingResult() == PathingResult.SUCCESS;
		}
		catch (AStar.InvalidPathException e)
		{
			return false;
		}
	}

	public Map<MobClass, Double> getSpawns()
	{
		return spawns;
	}

	public void start()
	{
		AtomicInteger totalMobs = new AtomicInteger(0);
		spawns.values().forEach(delta -> totalMobs.addAndGet(delta.intValue()));
		Bukkit.broadcastMessage("Starting wave #" + number + " (" + totalMobs.get() + (totalMobs.get() > 1 ? " mobs)" : " mob)"));

		Set<Creature> creatures = Sets.newHashSet();
		Iterator<Map.Entry<MobClass, Double>> entries = spawns.entrySet().iterator();
		AtomicReference<Map.Entry<MobClass, Double>> entry = new AtomicReference<>(null);
		AtomicInteger current = new AtomicInteger(0);
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (entry.get() == null || current.incrementAndGet() >= entry.get().getValue().intValue())
				{
					if (!entries.hasNext())
					{
						MobDefense.instance().setNextWaveTask(Bukkit.getScheduler().runTaskLater(MobDefense.instance(), () -> MobDefense.instance().startNextWave(), MobDefense
								.instance().getWaveTime() * 20L));
						cancel();
						return;
					}
					entry.set(entries.next());
				}
				Creature c = (Creature) Bukkit.getWorlds().get(0).spawnEntity(MobDefense.instance().getSpawn().clone(), entry.get().getKey().getType());
				c.setCustomName(entry.get().getKey().getDisplayName());
				c.setCustomNameVisible(true);
				c.setMaxHealth(entry.get().getKey().getHP());
				c.setHealth(entry.get().getKey().getHP());
				c.setCanPickupItems(false);
				if (c instanceof Ageable)
					((Ageable) c).setAdult();
				if (c instanceof Zombie)
				{
					((Zombie) c).setBaby(false);
					if (NMSUtils.getServerVersion().isAfter1_9())
						((Zombie) c).setVillagerProfession(NMSUtils.getServerVersion().isAfter1_10() ? Villager.Profession.NORMAL : null);
					else
						//noinspection deprecation
						((Zombie) c).setVillager(false);
				}
				if (c instanceof Skeleton)
					((Skeleton) c).setSkeletonType(Skeleton.SkeletonType.NORMAL);
				ItemStack[] inv = entry.get().getKey().getInv();
				c.getEquipment().setHelmet(inv[0]);
				c.getEquipment().setChestplate(inv[1]);
				c.getEquipment().setLeggings(inv[2]);
				c.getEquipment().setBoots(inv[3]);
				if (NMSUtils.getServerVersion().isAfter1_9())
				{
					c.getEquipment().setItemInMainHand(inv[4]);
					c.getEquipment().setItemInOffHand(inv[5]);
					c.getEquipment().setItemInMainHandDropChance(0.0F);
					c.getEquipment().setItemInOffHandDropChance(0.0F);
				}
				else
				{
					//noinspection deprecation
					c.getEquipment().setItemInHand(inv[4]);
					//noinspection deprecation
					c.getEquipment().setItemInHandDropChance(0.0F);
				}
				c.getEquipment().setHelmetDropChance(0.0F);
				c.getEquipment().setChestplateDropChance(0.0F);
				c.getEquipment().setLeggingsDropChance(0.0F);
				c.getEquipment().setBootsDropChance(0.0F);
				creatures.add(c);
				wavesByCreature.put(c, Wave.this);

				EntityUtils.setAttribute(c, EntityUtils.EntityAttributes.KNOCKBACK_RESISTANCE, 1.0D);
				Object handle = ReflectionUtils.invokeBukkitMethod("getHandle", c);
				Object goalSelector = ReflectionUtils.getNMSField("EntityInsentient", handle, "goalSelector");
				Object targetSelector = ReflectionUtils.getNMSField("EntityInsentient", handle, "targetSelector");
				Collection pathfinders = ReflectionUtils.getNMSField(goalSelector, "b");
				pathfinders.clear();
				pathfinders = ReflectionUtils.getNMSField(goalSelector, "c");
				pathfinders.clear();
				pathfinders = ReflectionUtils.getNMSField(targetSelector, "b");
				pathfinders.clear();
				pathfinders = ReflectionUtils.getNMSField(targetSelector, "c");
				pathfinders.clear();

				creatureClasses.put(c, entry.get().getKey());
				starts.put(c, MobDefense.instance().getSpawn().clone());
				creatureCurrentTile.put(c, 1);
				creatureTiles.put(c, Lists.newArrayList());
				recalculate(c);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						if (c.isDead())
						{
							Wave w = Wave.this;
							creatureClasses.remove(c);
							wavesByCreature.remove(c);
							waves.get(w).remove(c);
							if (waves.get(w).isEmpty())
								waves.remove(w);
							w.creatureTiles.remove(c);
							w.creatureCurrentTile.remove(c);
							w.starts.remove(c);
							cancel();
							return;
						}

						if (c.getLocation().distanceSquared(MobDefense.instance().getEnd()) < 2)
						{
							c.remove();
							Wave w = Wave.this;
							creatureClasses.remove(c);
							wavesByCreature.remove(c);
							waves.get(w).remove(c);
							if (waves.get(w).isEmpty())
								waves.remove(w);
							w.creatureTiles.remove(c);
							w.creatureCurrentTile.remove(c);
							w.starts.remove(c);
							cancel();
							Bukkit.getPluginManager().callEvent(new EntityGoneEvent(c));
							return;
						}

						update(c);
					}
				}.runTaskTimer(MobDefense.instance(), 0L, 5L);
			}
		}.runTaskTimer(MobDefense.instance(), 0, 20L);

		waves.put(this, creatures);
	}

	public static boolean recalculate(Creature c)
	{
		try
		{
			AStar pf = new AStar(c.getLocation().clone().subtract(0, 1, 0), MobDefense.instance().getEnd().clone().subtract(0, 1, 0), 100);
			if (pf.getPathingResult() == PathingResult.NO_PATH)
				return false;
			List<Tile> tiles = pf.iterate();
			if (tiles == null || tiles.isEmpty())
				return false;
			wavesByCreature.get(c).starts.put(c, c.getLocation().clone());
			wavesByCreature.get(c).creatureCurrentTile.put(c, 1);
			wavesByCreature.get(c).creatureTiles.put(c, tiles);
		}
		catch (AStar.InvalidPathException ignored)
		{
			return false;
		}
		return true;
	}

	public void update(Creature c)
	{
		Location start = starts.get(c);
		Tile currentTile = creatureTiles.get(c).get(creatureCurrentTile.get(c));
		Tile next = currentTile;
		int tileId = (int) (creatureCurrentTile.get(c) + creatureClasses.get(c).getSpeed());
		if (tileId < creatureTiles.get(c).size())
		{
			next = creatureTiles.get(c).get(tileId);
			Location currentLoc = currentTile.getLocation(start);
			Location nextLoc = next.getLocation(start);
			if (nextLoc.distanceSquared(c.getLocation()) > currentLoc.distanceSquared(c.getLocation()))
			{
				int xDif = nextLoc.getBlockX() - currentLoc.getBlockX();
				int zDif = nextLoc.getBlockZ() - currentLoc.getBlockZ();
				if (xDif != 0)
				{
					if (xDif < 0 && c.getLocation().getX() > currentLoc.getX()
							|| xDif > 0 && c.getLocation().getX() < currentLoc.getX())
					{
						next = currentTile;
						tileId = creatureCurrentTile.get(c);
					}
				}
				if (zDif != 0)
				{
					if (zDif < 0 && c.getLocation().getZ() > currentLoc.getZ()
							|| zDif > 0 && c.getLocation().getZ() < currentLoc.getZ())
					{
						next = currentTile;
						tileId = creatureCurrentTile.get(c);
					}
				}
			}
			creatureCurrentTile.put(c, tileId);
		}
		Object handle = ReflectionUtils.invokeBukkitMethod("getHandle", c);
		Object navigation = ReflectionUtils.invokeNMSMethod("EntityInsentient", "getNavigation", handle, new Class<?>[0]);
		ReflectionUtils.invokeNMSMethod("NavigationAbstract", "a", navigation, new Class<?>[]{double.class, double.class, double.class, double.class}, next.getX(start), next.getY
				(start) + 1, next.getZ(start), creatureClasses.get(c).getSpeed());
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}
}
