package fr.galaxyoyo.mobdefense;

import com.adamki11s.pathing.AStar;
import com.adamki11s.pathing.Tile;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_10_R1.EntityCreature;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftCreature;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Wave
{
	private int number;
	private Map<MobClass, Integer> spawns = Maps.newHashMap();

	public int getNumber()
	{
		return number;
	}

	public Map<MobClass, Integer> getSpawns()
	{
		return spawns;
	}

	public void start()
	{
		Bukkit.broadcastMessage("Démarrage de la vague #" + number);

		ArmorStand as = (ArmorStand) Bukkit.getWorlds().get(0).spawnEntity(MobDefense.instance().getEnd(), EntityType.ARMOR_STAND);
		as.setVisible(false);
		as.setAI(false);
		as.setGravity(false);

		List<Creature> creatures = Lists.newArrayList();
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
					//	ec.goalSelector.a(1, new PathfinderGoalWalkToLoc(ec, MobDefense.instance().getEnd(), entry.getKey().getSpeed()));
					//	ec.targetSelector.a(1, new PathfinderGoalWalkToLoc(ec, MobDefense.instance().getEnd(), entry.getKey().getSpeed()));
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
				c.setTarget(as);

				Bukkit.getScheduler().runTaskTimer(MobDefense.instance(), () -> {
					try
					{
						AStar pf = new AStar(c.getLocation(), MobDefense.instance().getEnd().clone().subtract(0, 1, 0), 142);
						List<Tile> tiles = pf.iterate();
						Tile next = tiles.get(1);
						ec.getNavigation().a(next.getX(c.getLocation()), next.getY(c.getLocation()) + 1, next.getZ(c.getLocation()), 1.0D);
					}
					catch (AStar.InvalidPathException e)
					{
						e.printStackTrace();
					}
				}, 0, 5L);
			}
		}
	}
}
