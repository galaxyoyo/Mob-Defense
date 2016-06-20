package fr.galaxyoyo.mobdefense.upgrades;

import com.google.common.collect.Lists;
import fr.galaxyoyo.mobdefense.towers.Tower;

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

	protected abstract void read(Map<String, Object> parameters);

	protected void applyTo0(Tower t)
	{
		t.getUpgrades().add(this);
		applyTo(t);
	}

	protected abstract void applyTo(Tower t);

	protected void disapplyTo0(Tower t)
	{
		t.getUpgrades().remove(this);
		applyTo(t);
	}

	protected abstract void disapplyTo(Tower t);

	public abstract void onTowerTick(Tower t);
}
