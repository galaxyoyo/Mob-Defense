package fr.galaxyoyo.mobdefense.towers;

import fr.galaxyoyo.mobdefense.MobDefense;
import org.bukkit.Location;
import org.bukkit.potion.PotionType;

import java.util.Map;

public class ArrowEffectTower extends Tower
{
	private float range;
	private PotionType basePotionType;
	private boolean basePotionExtended;
	private boolean basePotionUpgraded;

	protected ArrowEffectTower(TowerRegistration registration, Location location)
	{
		super(registration, location);
	}

	@Override
	public void onTick()
	{
		launchArrow(range, basePotionType, basePotionExtended, basePotionUpgraded);
	}

	@Override
	public void load(Map<String, Object> parameters)
	{
		range = (float) parameters.getOrDefault("range", 10.0F);
		try
		{
			basePotionType = PotionType.valueOf(parameters.getOrDefault("basePotionType", null).toString().toUpperCase());
		}
		catch (IllegalArgumentException ex)
		{
			basePotionType = null;
			if (parameters.get("basePotionType") != null)
				MobDefense.instance().getLogger().warning("Unknown potion effect: " + parameters.get("basePotionType").toString().toUpperCase());
		}
		basePotionExtended = (boolean) parameters.getOrDefault("basePotionExtended", false);
		basePotionUpgraded = (boolean) parameters.getOrDefault("basePotionUpgraded", false);
	}
}
