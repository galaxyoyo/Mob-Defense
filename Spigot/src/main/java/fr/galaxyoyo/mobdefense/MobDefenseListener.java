package fr.galaxyoyo.mobdefense;

import com.adamki11s.pathing.PathingResult;
import com.adamki11s.pathing.Tile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MobDefenseListener implements Listener
{
	private List<Tile> path;

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
		w.getSpawns().put(MobDefense.instance().getMobClasses().get(0), 1);
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
		List<Tile> oldPath = path;
		path = MobDefense.instance().getPathfinder().iterate();
		if (MobDefense.instance().getPathfinder().getPathingResult() == PathingResult.NO_PATH)
		{
			event.setCancelled(true);
			path = oldPath;
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.getBlock().getType() == Material.DISPENSER)
			path = MobDefense.instance().getPathfinder().iterate();
		else
			event.setCancelled(true);
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
	public void onEntityGone(EntityGoneEvent event)
	{
		event.getEntity().remove();
		Bukkit.broadcastMessage(event.getEntity().getCustomName() + " a réussi à passer !");
	}
}
