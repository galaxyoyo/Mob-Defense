package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.towers.Tower;

import java.util.Map;

public class RangeUpgrade extends Upgrade
{
	private float multiplier;

	protected RangeUpgrade(UpgradeRegistration registration, Tower tower)
	{
		super(registration, tower);
	}

	@Override
	public void read(Map<String, Object> parameters)
	{
		multiplier = ((Number) parameters.getOrDefault("multiplier", 1.0F)).floatValue();
	}

	@Override
	public void applyTo(Tower t)
	{
		t.setRangeMultiplier(t.getRangeMultiplier() * multiplier);
	}

	@Override
	public void disapplyTo(Tower t)
	{
		t.setRangeMultiplier(t.getRangeMultiplier() / multiplier);
	}

	@Override
	public void onTowerTick(Tower t)
	{
	}
}
