package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;

public class SpectralTower extends Tower
{
	protected SpectralTower(TowerRegistration registration, Location location)
	{
		super(registration, location);
	}

	@Override
	public void onTick()
	{
		launchSpectralArrow(10);
	}
}
