package fr.galaxyoyo.mobdefense;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PathfinderGoalWalkToLoc extends PathfinderGoal
{
	private double speed;

	private EntityInsentient entity;

	private Location loc;

	private NavigationAbstract navigation;

	private PathEntity pathEntity;

	public PathfinderGoalWalkToLoc(EntityInsentient entity, Location loc, double speed)
	{
		this.entity = entity;
		this.loc = loc;
		this.navigation = this.entity.getNavigation();
		this.speed = speed;
	}

	public boolean a()
	{
		pathEntity = this.navigation.a(loc.getX(), loc.getY(), loc.getZ());
		if (pathEntity != null)
		{
			for (int i = 0; i < pathEntity.d(); ++i)
			{
				PathPoint point = pathEntity.a(i);
				System.out.println("#" + i + ": " + point.a + ":" + point.b + ":" + point.c);
			}
		}
		pathEntity = new PathEntity(new PathPoint[]{new PathPoint(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())});
		if (entity.getBukkitEntity().getLocation().distance(MobDefense.instance().getEnd()) <= 1.0D)
		{
			if (entity.isAlive())
				Bukkit.getPluginManager().callEvent(new EntityGoneEvent(entity.getBukkitEntity()));
			return false;
		}
		return pathEntity != null;
	}

	@Override
	public void c()
	{
		if (pathEntity == null)
			System.out.println(navigation);
		this.navigation.a(pathEntity, speed);
	}
}
