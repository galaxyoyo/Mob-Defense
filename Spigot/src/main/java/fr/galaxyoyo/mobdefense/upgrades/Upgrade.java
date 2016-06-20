package fr.galaxyoyo.mobdefense.upgrades;

import com.google.common.collect.Lists;
import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.entity.Arrow;

import java.util.List;
import java.util.Map;

public abstract class Upgrade
{
	private static final List<UpgradeRegistration> towerRegistrations = Lists.newArrayList();
	private final UpgradeRegistration registration;
	private Tower tower;

	protected Upgrade(UpgradeRegistration registration, Tower tower)
	{
		this.registration = registration;
		this.tower = tower;
	}

	public static void registerUpgrade(UpgradeRegistration registration)
	{
		if (registration.register())
			towerRegistrations.add(registration);
	}

	public static List<UpgradeRegistration> getUpgradeRegistrations()
	{
		return towerRegistrations;
	}

	public UpgradeRegistration getRegistration()
	{
		return registration;
	}

	public Tower getTower()
	{
		return tower;
	}

	public abstract void read(Map<String, Object> parameters);

	public abstract void apply();

	public abstract void disapply();

	public abstract void onTowerTick();

	public abstract void onTowerLaunchArrow(Arrow arrow);
}
