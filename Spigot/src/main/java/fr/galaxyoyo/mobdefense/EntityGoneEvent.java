package fr.galaxyoyo.mobdefense;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class EntityGoneEvent extends EntityEvent
{
	private static HandlerList handlers = new HandlerList();

	public EntityGoneEvent(Entity what)
	{
		super(what);
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}