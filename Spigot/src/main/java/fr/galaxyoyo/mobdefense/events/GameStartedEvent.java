package fr.galaxyoyo.mobdefense.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartedEvent extends Event
{
	private static HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
}
