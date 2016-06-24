package fr.galaxyoyo.mobdefense;

import fr.galaxyoyo.mobdefense.events.EntityGoneEvent;
import fr.galaxyoyo.mobdefense.towers.Tower;
import fr.galaxyoyo.mobdefense.upgrades.Upgrade;
import fr.galaxyoyo.mobdefense.upgrades.UpgradeRegistration;
import fr.galaxyoyo.spigot.nbtapi.ItemStackUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

import java.util.List;
import java.util.Optional;

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
		if (NMSUtils.getServerVersion().isAfter1_9())
			event.getPlayer().setCollidable(false);
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
				event.setBuild(event.getPlayer().getGameMode() == GameMode.CREATIVE);
				return;
			}
			event.getPlayer().getInventory().all(Material.DIAMOND_PICKAXE).values().forEach(stack ->
			{
				List<Material> canDestroy = ItemStackUtils.getCanDestroy(stack);
				if (!canDestroy.contains(t.getRegistration().getMaterial()))
				{
					canDestroy.add(t.getRegistration().getMaterial());
					ItemStackUtils.setCanDestroy(stack, canDestroy.toArray(new Material[canDestroy.size()]));
				}
			});
		}

		if (!Wave.checkForPath())
		{
			event.setCancelled(true);
			event.setBuild(false);
		}
		else
		{
			for (Creature c : Wave.getAllCreatures())
			{
				if (!Wave.recalculate(c))
				{
					event.setCancelled(true);
					event.setBuild(false);
					break;
				}
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
					if (NMSUtils.getServerVersion().isAfter1_9())
					{
						if (event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DISPENSER)
							event.getPlayer().getInventory().setItemInMainHand(stack);
						else
							event.getPlayer().getInventory().setItemInOffHand(stack);
					}
					else
						//noinspection deprecation
						event.getPlayer().getInventory().setItemInHand(stack);
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
			meta.setDisplayName(t.getRegistration().getDisplayName());
			meta.setLore(t.getRegistration().getLore());
			stack.setItemMeta(meta);
			t.getLocation().getWorld().dropItem(t.getLocation(), stack);
		}
		Wave.getAllCreatures().forEach(Wave::recalculate);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		event.setCancelled(event.getAction() == Action.PHYSICAL && event.getPlayer().getGameMode() != GameMode.CREATIVE);
	}

	@EventHandler
	public void onInventoryOpened(InventoryOpenEvent event)
	{
		Inventory inv = event.getInventory();
		if (inv.getTitle().equals("container.dispenser"))
			inv.setMaxStackSize(1);
	}

	@EventHandler
	public void onInventoryClicked(InventoryClickEvent event)
	{
		Inventory inv = event.getInventory();
		if (!inv.getTitle().equals("container.dispenser"))
			return;

		Tower t = Tower.getTowerAt(event.getInventory().getLocation());

		switch (event.getAction())
		{
			case NOTHING:
				break;
			case PICKUP_ALL:
			case PICKUP_SOME:
			case PICKUP_HALF:
			case PICKUP_ONE:
				if (event.getRawSlot() < 9)
				{
					Upgrade upgrade = t.getUpgrade(event.getRawSlot());
					upgrade.disapply();
					t.getUpgrades().set(event.getRawSlot(), null);
				}
				break;
			case PLACE_ALL:
			case PLACE_SOME:
			case PLACE_ONE:
				if (event.getRawSlot() < 9)
				{
					ItemStack stack = event.getCursor().clone();
					stack.setAmount(1);
					Optional<UpgradeRegistration> optional = Upgrade.getUpgradeRegistrations().stream().filter(upgradeRegistration -> upgradeRegistration.getItem().equals(stack))
							.findAny();
					event.setCancelled(!optional.isPresent());
					optional.ifPresent(upgradeRegistration -> {
						try
						{
							Upgrade upgrade = upgradeRegistration.newInstance(t);
							upgrade.apply();
							t.setUpgrade(event.getRawSlot(), upgrade);
						}
						catch (EventException e)
						{
							e.printStackTrace();
						}
					});
				}
				break;
			case SWAP_WITH_CURSOR:
				event.getWhoClicked().sendMessage("[MobDefense] Warning: this method to add an upgrade is unsupported. Please tell me how you have done this, I couldn't do this.");
				break;
			case DROP_ALL_CURSOR:
			case DROP_ONE_CURSOR:
			case DROP_ALL_SLOT:
			case DROP_ONE_SLOT:
				if (event.getRawSlot() < 9)
				{
					Upgrade upgrade = t.getUpgrade(event.getRawSlot());
					upgrade.disapply();
					t.getUpgrades().set(event.getRawSlot(), null);
				}
				break;
			case MOVE_TO_OTHER_INVENTORY:
				if (event.getRawSlot() < 9)
				{
					Upgrade upgrade = t.getUpgrade(event.getRawSlot());
					upgrade.disapply();
					t.getUpgrades().set(event.getRawSlot(), null);
				}
				else if (inv.firstEmpty() >= 0)
				{
					ItemStack stack = event.getCurrentItem().clone();
					stack.setAmount(1);
					Optional<UpgradeRegistration> optional = Upgrade.getUpgradeRegistrations().stream().filter(upgradeRegistration -> upgradeRegistration.getItem().equals(stack))
							.findAny();
					event.setCancelled(!optional.isPresent());
					optional.ifPresent(upgradeRegistration -> {
						try
						{
							Upgrade upgrade = upgradeRegistration.newInstance(t);
							upgrade.apply();
							t.setUpgrade(inv.firstEmpty(), upgrade);
						}
						catch (EventException e)
						{
							e.printStackTrace();
						}
					});
				}
				break;
			case HOTBAR_MOVE_AND_READD:
				break;
			case HOTBAR_SWAP:
				break;
			case CLONE_STACK:
				break;
			case COLLECT_TO_CURSOR:
				event.setCancelled(true);
				break;
			case UNKNOWN:
				break;
		}
	}

	@EventHandler
	public void onExperienceReceived(PlayerExpChangeEvent event)
	{
		event.setAmount(0);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		event.setCancelled(event.getRightClicked().getType() != EntityType.VILLAGER);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		event.setCancelled(event.getDamager().getType() == EntityType.PLAYER || event.getEntityType() == EntityType.PLAYER || event.getEntityType() == EntityType.VILLAGER);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		event.setCancelled(event.getEntityType() == EntityType.PLAYER || event.getEntityType() == EntityType.VILLAGER);
		if (event.isCancelled())
			event.getEntity().setFireTicks(0);
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
	public void onEntityCombust(EntityCombustEvent event)
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
		World w = event.getWorld();
		w.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
		w.setGameRuleValue("doDaylightCycle", "false");
		w.setGameRuleValue("doFireTick", "false");
		w.setGameRuleValue("doMobSpawning", "false");
		w.setGameRuleValue("doMobLoot", "false");
		w.setFullTime(6000L);
	}
}
