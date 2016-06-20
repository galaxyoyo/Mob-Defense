package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.potion.PotionData;

import java.util.Map;

public class ExtendedUpgrade extends Upgrade
{
	protected ExtendedUpgrade(UpgradeRegistration registration, Tower tower)
	{
		super(registration, tower);
	}

	@Override
	public void read(Map<String, Object> parameters)
	{
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
		if (arrow instanceof TippedArrow)
		{
			PotionData data = ((TippedArrow) arrow).getBasePotionData();
			if (data.getType().isExtendable())
			{
				data = new PotionData(data.getType(), true, data.isUpgraded());
				((TippedArrow) arrow).setBasePotionData(data);
			}
		}
	}
}
