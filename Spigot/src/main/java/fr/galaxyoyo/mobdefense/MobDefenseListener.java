package fr.galaxyoyo.mobdefense;

import fr.galaxyoyo.mobdefense.events.EntityGoneEvent;
import fr.galaxyoyo.mobdefense.towers.Tower;
import fr.galaxyoyo.spigot.nbtapi.ItemStackUtils;
import fr.galaxyoyo.spigot.nbtapi.ReflectionUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

import java.util.List;

public class MobDefenseListener implements Listener
{
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
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
		ItemStackUtils.setCanDestroy(pickaxe, Material.DISPENSER);
		event.getPlayer().getInventory().clear();
		event.getPlayer().getInventory().addItem(pickaxe);
		event.getPlayer().setCollidable(true);
		event.getPlayer().teleport(MobDefense.instance().getPlayerSpawn());
		event.getPlayer().setGameMode(GameMode.ADVENTURE);

		if (event.getPlayer().isOp() && MobDefense.instance().getLatestVersion() != null)
			event.getPlayer().sendMessage(ChatColor.RED + "[MobDefense] You're running an outdated version of MobDefense (" + MobDefense.instance().getDescription().getVersion()
					+ "). Please update to " + MobDefense.instance().getLatestVersion() + ".");
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
			Tower t;
			if ((t = Tower.placeAt(event.getBlockPlaced().getLocation(), event.getItemInHand())) == null)
			{
				event.setBuild(false);
				return;
			}
			event.getPlayer().getInventory().all(Material.DIAMOND_PICKAXE).values().stream().forEach(stack -> {
				List<Material> canDestroy = ItemStackUtils.getCanDestroy(stack);
				if (!canDestroy.contains(t.getMaterial()))
				{
					canDestroy.add(t.getMaterial());
					ItemStackUtils.setCanDestroy(stack, canDestroy.toArray(new Material[canDestroy.size()]));
				}
			});
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
			else if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
			{
				ItemStack stack = event.getItemInHand();
				if (stack.getAmount() > 1)
				{
					stack.setAmount(stack.getAmount() - 1);
					if (event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DISPENSER)
						event.getPlayer().getInventory().setItemInMainHand(stack);
					else
						event.getPlayer().getInventory().setItemInOffHand(stack);
				}
				else
				{
					ItemStack[] items = event.getPlayer().getInventory().getContents();

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
		//	if (event.getDamager().getType() == EntityType.PLAYER || event.getEntityType() == EntityType.PLAYER)
		//		event.setCancelled(true);
		//	else
		if (event.getEntity() instanceof Creature)
		{
			if (event.getDamager() instanceof TippedArrow)
			{
				Object arrowHandle = ReflectionUtils.invokeBukkitMethod("getHandle", event.getDamager());
				Object entityHandle = ReflectionUtils.invokeBukkitMethod("getHandle", event.getEntity());
				ReflectionUtils.invokeNMSMethod("a", arrowHandle, new Class<?>[]{ReflectionUtils.getNMSClass("EntityLiving")}, entityHandle);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		//	if (event.getEntityType() == EntityType.PLAYER)
		//		event.setCancelled(true);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event)
	{
		event.getEntity().remove();
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
		Score score = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore("Lives");
		score.setScore(score.getScore() - 1);
		if (score.getScore() > 0)
			Bukkit.broadcastMessage("[MobDefense] " + event.getEntity().getCustomName() + " bypassed the towers! " + score.getScore() + " "
					+ (score.getScore() > 1 ? "lives" : "life") + " left");
		else
		{
			Wave currentWave = MobDefense.instance().getCurrentWave();
			Bukkit.broadcastMessage("[MobDefense] " + event.getEntity().getCustomName() + " bypassed the towers! " + ChatColor.RED + "You survived "
					+ currentWave.getNumber() + " wave" + (currentWave.getNumber() > 1 ? "s." : "."));
			MobDefense.instance().stop(null);
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
