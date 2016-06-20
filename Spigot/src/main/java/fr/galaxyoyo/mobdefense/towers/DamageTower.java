package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;
import org.bukkit.potion.PotionType;

public class DamageTower extends Tower
{
	protected DamageTower(TowerRegistration registration, Location location)
	{
		super(registration, location);
	}

	public void onTick()
	{
		launchArrow(10, PotionType.INSTANT_DAMAGE);
	}
}
