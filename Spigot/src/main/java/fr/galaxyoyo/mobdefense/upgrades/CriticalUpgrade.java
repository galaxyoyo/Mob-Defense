package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.Messages;
import fr.galaxyoyo.mobdefense.MobDefense;
import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.entity.Arrow;

import java.util.Map;

public class CriticalUpgrade extends Upgrade
{
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
			MobDefense.instance().getLogger().warning(String.format(Messages.getMessages().getPercentageUpgradeWarning(), percentage, "CriticalUpgrade"));
			percentage = 1.0D;
		}
	}

	@Override
	public void apply()
	{
		getTower().setCriticalPercentage(getTower().getCriticalPercentage() + percentage);
	}

	@Override
	public void disapply()
	{
		getTower().setCriticalPercentage(getTower().getCriticalPercentage() - percentage);
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
