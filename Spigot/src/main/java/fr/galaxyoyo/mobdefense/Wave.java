package fr.galaxyoyo.mobdefense;

import com.adamki11s.pathing.AStar;
import com.adamki11s.pathing.PathingResult;
import com.adamki11s.pathing.Tile;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_10_R1.EntityCreature;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftCreature;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.inventory.ItemStack;

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
				if (c instanceof Ageable)
					((Ageable) c).setAdult();
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

				Bukkit.getScheduler().runTaskTimer(MobDefense.instance(), () -> update(c), 0, 10L);
			}
		}

		waves.put(this, creatures);
	}

	public int getNumber()
	{
		return number;
	}

	public static boolean update(Creature c)
	{
		try
		{
			AStar pf = new AStar(c.getLocation().clone().subtract(0, 1, 0), MobDefense.instance().getEnd().clone().subtract(0, 1, 0), 142);
			if (pf.getPathingResult() == PathingResult.NO_PATH)
				return false;
			List<Tile> tiles = pf.iterate();
			Tile next = tiles.get(2);
			((CraftCreature) c).getHandle().getNavigation().a(next.getX(c.getLocation()), next.getY(c.getLocation()) + 1, next.getZ(c.getLocation()), 1.0D);
		}
		catch (AStar.InvalidPathException ignored)
		{
		}
		return true;
	}
}
