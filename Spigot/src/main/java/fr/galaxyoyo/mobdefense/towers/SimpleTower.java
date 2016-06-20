package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;

public class SimpleTower extends Tower
{
	protected SimpleTower(TowerRegistration registration, Location location)
	{
		super(registration, location);
	}

	@Override
	public void onTick()
	{
		launchArrow(10);
	}
}
