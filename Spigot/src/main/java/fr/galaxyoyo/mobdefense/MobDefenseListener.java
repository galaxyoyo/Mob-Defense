package fr.galaxyoyo.mobdefense;

import fr.galaxyoyo.mobdefense.towers.Tower;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
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
	private int goneMobs = 0;

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
			event.setCancelled(true);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		ItemMeta meta = pickaxe.getItemMeta();
		meta.spigot().setUnbreakable(true);
		meta.addItemFlags(ItemFlag.values());
		pickaxe.setItemMeta(meta);
		event.getPlayer().getInventory().clear();
		event.getPlayer().getInventory().addItem(pickaxe);
		//noinspection deprecation
		event.getPlayer().spigot().setCollidesWithEntities(false);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		for (ItemStack stack : event.getPlayer().getInventory().getContents())
		{
			if (stack == null || stack.getType() == Material.DIAMOND_PICKAXE)
				continue;
			event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), stack);
		}

		event.getPlayer().getInventory().clear();
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.getBlockPlaced().getType() == Material.DISPENSER)
		{
			event.setCancelled(true);
			event.setBuild(true);
			if (!Tower.placeAt(event.getBlockPlaced().getLocation(), event.getItemInHand()))
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
				event.setBuild(false);
				break;
			}
		}

		if (event.getBlockPlaced().getType() == Material.DISPENSER)
		{
			if (!event.canBuild())
				Tower.breakAt(event.getBlockPlaced().getLocation());
			else
			{
				ItemStack stack = event.getItemInHand();
				if (stack.getAmount() > 1)
					stack.setAmount(stack.getAmount() - 1);
				else
				{
					ItemStack[] items = event.getPlayer().getInventory().getStorageContents();

					for (int i = 0; i < items.length; ++i)
					{
						if (items[i] != null && items[i].equals(stack))
						{
							event.getPlayer().getInventory().clear(i);
							break;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Tower t = Tower.breakAt(event.getBlock().getLocation());
		if (t != null && event.getPlayer().getGameMode() != GameMode.CREATIVE)
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
		if (event.getDamager().getType() == EntityType.PLAYER)
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntityType() == EntityType.PLAYER)
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
		++goneMobs;
		if (goneMobs < MobDefense.instance().getMaxMobs())
			Bukkit.broadcastMessage("[MobDefense] " + event.getEntity().getCustomName() + " a réussi à passer ! " + (MobDefense.instance().getMaxMobs() - goneMobs) + " restant" +
					(MobDefense.instance().getMaxMobs() - goneMobs > 1 ? "s" : ""));
		else
		{
			Bukkit.broadcastMessage("[MobDefense] " + event.getEntity().getCustomName() + " a réussi à passer ! " + ChatColor.RED + "Partie terminée ! Bravo à vous !");
			for (Tower tower : Tower.getAllTowers())
				Tower.breakAt(tower.getLocation());
			Bukkit.getWorlds().get(0).getEntities().stream().filter(entity -> entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.VILLAGER).forEach(Entity::remove);
			MobDefense.instance().setCurrentWave(null);
			Bukkit.getScheduler().cancelTasks(MobDefense.instance());
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onWorldLoaded(WorldLoadEvent event)
	{
		event.getWorld().getEntities().stream().filter(entity -> (entity instanceof Player)).forEach(Entity::remove);
	}
}
