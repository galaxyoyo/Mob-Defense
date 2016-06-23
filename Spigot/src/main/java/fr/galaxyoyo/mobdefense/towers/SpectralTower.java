package fr.galaxyoyo.mobdefense.towers;

import org.bukkit.Location;

import java.util.Map;

public class SpectralTower extends Tower
{
	private float range;
	private int glowingTicks;

	protected SpectralTower(TowerRegistration registration, Location location)
	{
		super(registration, location);
	}

	@Override
	public void onTick()
	{
		launchSpectralArrow(range, glowingTicks);
	}

	@Override
	public void load(Map<String, Object> parameters)
	{
		range = (float) parameters.getOrDefault("range", 10.0F);
		glowingTicks = ((Number) parameters.getOrDefault("glowingTicks", 200)).intValue();
	}
}
