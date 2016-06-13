package fr.galaxyoyo.mobdefense;

import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
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
		if (event.getBlockPlaced().getType() == Material.DISPENSER)
		{
			event.setCancelled(true);
			event.setBuild(true);
			if (Tower.placeAt(event.getBlockPlaced().getLocation(), event.getItemInHand()) == null)
			{
				event.setBuild(false);
				return;
			}
		}

		for (Creature c : Wave.getAllCreatures())
		{
			if (!Wave.recalculate(c))
			{
				event.setCancelled(true);
				break;
			}
		}

		if (event.getBlockPlaced().getType() == Material.DISPENSER)
		{
			if (!event.canBuild())
				Tower.breakAt(event.getBlockPlaced().getLocation());
			else
			{
				event.getItemInHand().setAmount(event.getItemInHand().getAmount() - 1);
				if (event.getItemInHand().getAmount() <= 0)
					event.getItemInHand().setType(Material.AIR);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Tower t = Tower.breakAt(event.getBlock().getLocation());
		if (t != null)
		{
			ItemStack stack = new ItemStack(Material.DISPENSER);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(Tower.getTowerName(t.getClass()));
			stack.setItemMeta(meta);
			t.getLocation().getWorld().dropItem(t.getLocation(), stack);
		}
		Wave.getAllCreatures().forEach(Wave::recalculate);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked().getType() != EntityType.VILLAGER)
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (!(event.getEntity() instanceof Creature))
			return;
		MobClass mobClass = Wave.getClass((Creature) event.getEntity());
		event.setDroppedExp(0);
		if (mobClass == null)
		{
			event.getDrops().clear();
			return;
		}
		int loot = mobClass.getLoot();
		int emeraldBlocks = loot / (9 * 9 * 9 * 9);
		if (emeraldBlocks > 0)
		{
			event.getDrops().add(new ItemStack(Material.EMERALD_BLOCK, emeraldBlocks));
			loot %= 9 * 9 * 9 * 9;
		}
		int emeralds = loot / (9 * 9 * 9);
		if (emeralds > 0)
		{
			event.getDrops().add(new ItemStack(Material.EMERALD, emeralds));
			loot %= 9 * 9 * 9;
		}
		int goldBlocks = loot / (9 * 9);
		if (goldBlocks > 0)
		{
			event.getDrops().add(new ItemStack(Material.GOLD_BLOCK, goldBlocks));
			loot %= 9 * 9;
		}
		int goldIngots = loot / 9;
		if (goldIngots > 0)
		{
			event.getDrops().add(new ItemStack(Material.GOLD_INGOT, goldIngots));
			loot %= 9;
		}
		if (loot > 0)
			event.getDrops().add(new ItemStack(Material.GOLD_NUGGET, loot));
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
		Bukkit.broadcastMessage(event.getEntity().getCustomName() + " a réussi à passer !");
	}

	@EventHandler
	public void onWorldLoaded(WorldLoadEvent event)
	{
		event.getWorld().getEntities().stream().filter(entity -> (entity instanceof Player)).forEach(Entity::remove);
	}
}
