package fr.galaxyoyo.mobdefense;

import com.adamki11s.pathing.AStar;
import com.adamki11s.pathing.PathingResult;
import com.adamki11s.pathing.Tile;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_10_R1.EntityCreature;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftCreature;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Wave
{
	private static Map<Wave, Set<Creature>> waves = Maps.newHashMap();
	private static Map<Creature, Wave> wavesByCreature = Maps.newHashMap();
	private static Map<Integer, Wave> wavesByNumber = Maps.newHashMap();
	private int number;
	private Map<MobClass, Integer> spawns = Maps.newHashMap();
	private Map<Creature, List<Tile>> creatureTiles = Maps.newHashMap();
	private Map<Creature, Integer> creatureCurrentTile = Maps.newHashMap();
	private Map<Creature, Location> starts = Maps.newHashMap();

	public static Set<Creature> getAllCreatures()
	{
		Set<Creature> allCreatures = Sets.newHashSet();
		waves.values().forEach(allCreatures::addAll);
		return allCreatures;
	}

	public Map<MobClass, Integer> getSpawns()
	{
		return spawns;
	}

	public void start()
	{
		number = wavesByNumber.size() + 1;
		wavesByNumber.put(number, this);
		Bukkit.broadcastMessage("Démarrage de la vague #" + getNumber());

		Set<Creature> creatures = Sets.newHashSet();
		for (Map.Entry<MobClass, Integer> entry : spawns.entrySet())
		{
			for (int i = 0; i < entry.getValue(); ++i)
			{
				Creature c = (Creature) Bukkit.getWorlds().get(0).spawnEntity(MobDefense.instance().getSpawn(), entry.getKey().getType());
				c.setCustomName(entry.getKey().getDisplayName());
				c.setCustomNameVisible(true);
				c.setMaxHealth(entry.getKey().getHP());
				c.setHealth(entry.getKey().getHP());
				c.setCollidable(false);
				if (c instanceof Ageable)
					((Ageable) c).setAdult();
				if (c instanceof Zombie)
				{
					((Zombie) c).setBaby(false);
					((Zombie) c).setVillagerProfession(Villager.Profession.NORMAL);
				}
				if (c instanceof Skeleton)
					((Skeleton) c).setSkeletonType(Skeleton.SkeletonType.NORMAL);
				ItemStack[] inv = entry.getKey().getInv();
				c.getEquipment().setHelmet(inv[0]);
				c.getEquipment().setChestplate(inv[1]);
				c.getEquipment().setLeggings(inv[2]);
				c.getEquipment().setBoots(inv[3]);
				c.getEquipment().setItemInMainHand(inv[4]);
				c.getEquipment().setItemInOffHand(inv[5]);
				c.getEquipment().setHelmetDropChance(0.0F);
				c.getEquipment().setChestplateDropChance(0.0F);
				c.getEquipment().setLeggingsDropChance(0.0F);
				c.getEquipment().setBootsDropChance(0.0F);
				c.getEquipment().setItemInMainHandDropChance(0.0F);
				c.getEquipment().setItemInOffHandDropChance(0.0F);
				creatures.add(c);
				wavesByCreature.put(c, this);

				EntityCreature ec = ((CraftCreature) c).getHandle();

				try
				{
					Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
					bField.setAccessible(true);
					Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
					cField.setAccessible(true);
					Set bGoal = (Set) bField.get(ec.goalSelector);
					bGoal.clear();
					Set cGoal = (Set) cField.get(ec.goalSelector);
					cGoal.clear();
					Set bTarget = (Set) bField.get(ec.targetSelector);
					bTarget.clear();
					Set cTarget = (Set) cField.get(ec.targetSelector);
					cTarget.clear();
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}

				starts.put(c, c.getLocation().clone());
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
							cancel();
							return;
						}

						if (c.getLocation().distanceSquared(MobDefense.instance().getEnd()) < 4)
						{
							c.remove();
							cancel();
							return;
						}

						update(c);
					}
				}.runTaskTimer(MobDefense.instance(), 5L, 5L);
			}
		}

		waves.put(this, creatures);
	}

	public int getNumber()
	{
		return number;
	}

	public static boolean recalculate(Creature c)
	{
		try
		{
			AStar pf = new AStar(c.getLocation().clone().subtract(0, 1, 0), MobDefense.instance().getEnd().clone().subtract(0, 1, 0), (int) MobDefense.instance().getEnd()
					.distance(c.getLocation()));
			if (pf.getPathingResult() == PathingResult.NO_PATH)
				return false;
			List<Tile> tiles = pf.iterate();
			if (tiles == null)
				return false;
			wavesByCreature.get(c).starts.put(c, c.getLocation().clone());
			wavesByCreature.get(c).creatureCurrentTile.put(c, 1);
			wavesByCreature.get(c).creatureTiles.put(c, tiles);
		}
		catch (AStar.InvalidPathException ignored)
		{
		}
		return true;
	}

	public void update(Creature c)
	{
		Location start = starts.get(c);
		Tile currentTile = creatureTiles.get(c).get(creatureCurrentTile.get(c));
		Tile next = currentTile;
		if (currentTile.getLocation(starts.get(c)).distanceSquared(c.getLocation()) < 6)
		{
			int tileId = creatureCurrentTile.get(c) + 1;
			creatureCurrentTile.put(c, tileId);
			next = creatureTiles.get(c).get(tileId);
		}
		((CraftCreature) c).getHandle().getNavigation().a(next.getX(start), next.getY(start) + 1, next.getZ(start), 1.0D);
	}
}
