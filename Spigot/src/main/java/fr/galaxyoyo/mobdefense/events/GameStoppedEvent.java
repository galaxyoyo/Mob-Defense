package fr.galaxyoyo.mobdefense.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStoppedEvent extends Event
{
	private static HandlerList handlers = new HandlerList();
	private final int finalWave;

	public GameStoppedEvent(int finalWave)
	{
		this.finalWave = finalWave;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public int getFinalWave()
	{
		return finalWave;
	}
}
