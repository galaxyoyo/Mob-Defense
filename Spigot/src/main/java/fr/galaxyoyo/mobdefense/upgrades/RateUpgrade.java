package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.entity.Arrow;

import java.util.Map;

public class RateUpgrade extends Upgrade
{
	private double divider;

	protected RateUpgrade(UpgradeRegistration registration, Tower tower)
	{
		super(registration, tower);
	}

	@Override
	public void read(Map<String, Object> parameters)
	{
		divider = ((Number) parameters.getOrDefault("divider", 1.0D)).doubleValue();
	}

	@Override
	public void apply()
	{
		getTower().setUpdateRate(getTower().getUpdateRate() / divider);
	}

	@Override
	public void disapply()
	{
		getTower().setUpdateRate(getTower().getUpdateRate() * divider);
	}

	@Override
	public void onTowerTick()
	{
	}

	@Override
	public void onTowerLaunchArrow(Arrow arrow)
	{
	}
}
