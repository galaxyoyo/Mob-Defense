package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.towers.Tower;

import java.util.Map;

public class SpeedUpgrade extends Upgrade
{
	private float multiplier;

	protected SpeedUpgrade(UpgradeRegistration registration, Tower tower)
	{
		super(registration, tower);
	}

	@Override
	protected void read(Map<String, Object> parameters)
	{
		multiplier = ((Number) parameters.getOrDefault("multiplier", 1.0F)).floatValue();
	}

	@Override
	protected void applyTo(Tower t)
	{
		t.setSpeedMultiplier(t.getRangeMultiplier() * multiplier);
	}

	@Override
	protected void disapplyTo(Tower t)
	{
		t.setSpeedMultiplier(t.getRangeMultiplier() / multiplier);
	}

	@Override
	public void onTowerTick(Tower t)
	{
	}
}
