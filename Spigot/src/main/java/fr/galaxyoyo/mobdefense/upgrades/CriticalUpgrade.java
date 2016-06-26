package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.MobDefense;
import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.entity.Arrow;

import java.util.Map;
import java.util.Random;

public class CriticalUpgrade extends Upgrade
{
	private static final Random RANDOM = MobDefense.instance().getRandomInstance();
	private double percentage;

	protected CriticalUpgrade(UpgradeRegistration registration, Tower tower)
	{
		super(registration, tower);
	}

	@Override
	public void read(Map<String, Object> parameters)
	{
		percentage = ((Number) parameters.getOrDefault("percentage", 1.0D)).doubleValue();
		if (percentage > 1.0D)
			percentage /= 100.0D;
		if (percentage > 1.0D || percentage <= 0.0D)
		{
			MobDefense.instance().getLogger().warning("Warning: percentage " + percentage + " must be included between 0 and 1. Considering 1 (100 %). Please check " +
					"CriticalUpgrade config.");
			percentage = 1.0D;
		}
	}

	@Override
	public void apply()
	{
	}

	@Override
	public void disapply()
	{
	}

	@Override
	public void onTowerTick()
	{
	}

	@Override
	public void onTowerLaunchArrow(Arrow arrow)
	{
		if (percentage == 1.0D || RANDOM.nextDouble() <= percentage)
			arrow.setCritical(true);
	}
}
