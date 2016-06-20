package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;
import org.bukkit.potion.PotionType;

public class HealingTower extends Tower
{
	protected HealingTower(TowerRegistration registration, Location location)
	{
		super(registration, location);
	}

	@Override
	public void onTick()
	{
		launchArrow(10, PotionType.INSTANT_HEAL);
	}
}
