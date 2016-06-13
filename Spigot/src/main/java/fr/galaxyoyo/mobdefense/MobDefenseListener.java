package fr.galaxyoyo.mobdefense;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MobDefenseListener implements Listener
{
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
			event.setCancelled(true);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Wave w = new Wave();
		w.getSpawns().put(MobDefense.instance().getMobClasses().get(0), 15);
		w.start();

		ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		ItemMeta meta = pickaxe.getItemMeta();
		meta.spigot().setUnbreakable(true);
		meta.addItemFlags(ItemFlag.values());
		pickaxe.setItemMeta(meta);
		event.getPlayer().getInventory().clear();
		event.getPlayer().getInventory().addItem(pickaxe);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		for (Creature c : Wave.getAllCreatures())
		{
			if (!Wave.recalculate(c))
			{
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Wave.getAllCreatures().forEach(Wave::recalculate);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked().getType() != EntityType.VILLAGER)
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityBurn(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityGone(EntityGoneEvent event)
	{
		event.getEntity().remove();
		Bukkit.broadcastMessage(event.getEntity().getCustomName() + " a réussi à passer !");
	}
}
