package fr.galaxyoyo.mobdefense.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Locale;

public class LanguageChangedEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final Locale locale;

	public LanguageChangedEvent(Player who, Locale locale)
	{
		super(who);
		this.locale = locale;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	@SuppressWarnings("unused")
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
