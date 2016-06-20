package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.entity.Arrow;

import java.util.Map;

public class SpeedUpgrade extends Upgrade
{
	private float multiplier;

	protected SpeedUpgrade(UpgradeRegistration registration, Tower tower)
	{
		super(registration, tower);
	}

	@Override
	public void read(Map<String, Object> parameters)
	{
		multiplier = ((Number) parameters.getOrDefault("multiplier", 1.0F)).floatValue();
	}

	@Override
	public void apply()
	{
		getTower().setSpeedMultiplier(getTower().getSpeedMultiplier() * multiplier);
	}

	@Override
	public void disapply()
	{
		getTower().setSpeedMultiplier(getTower().getRangeMultiplier() / multiplier);
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
