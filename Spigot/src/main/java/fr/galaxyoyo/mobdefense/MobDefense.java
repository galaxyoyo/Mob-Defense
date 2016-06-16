package fr.galaxyoyo.mobdefense;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.galaxyoyo.mobdefense.events.GameStartedEvent;
import fr.galaxyoyo.mobdefense.events.GameStoppedEvent;
import fr.galaxyoyo.mobdefense.towers.Tower;
import fr.galaxyoyo.spigot.nbtapi.ItemStackUtils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftVillager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class MobDefense extends JavaPlugin
{
	private static MobDefense instance;
	private Gson gson;
	private Map<String, MobClass> mobClasses = Maps.newHashMap();
	private Location playerSpawn;
	private Location spawn, end;
	private int startMoney;
	private int waveTime;
	private int baseLives;
	private Location npcTowerLoc;
	private Location npcUpgradesLoc;
	private Location npcExchangeLoc;
	private List<Wave> waves = Lists.newArrayList();
	private Wave currentWave;
	private Objective objective;

	@Override
	public void onDisable()
	{
		stop(null);
	}

	@Override
	public void onEnable()
	{
		instance = this;

		if (getServer().getPluginManager().getPlugin("NBTAPI") == null)
		{
			try
			{
				File file = new File("plugins", "NBTAPI.jar");
				FileUtils.copyURLToFile(new URL("http://arathia.fr/maven/fr/galaxyoyo/spigot/nbtapi/1.0.1/nbtapi-1.0.1.jar"), file);
				getServer().getPluginManager().loadPlugin(file);
			}
			catch (IOException e)
			{
				getLogger().severe("Unable to download NBTAPI library. Make sure you have the latest version of MobDefense and have an Internet connection.");
				getLogger().severe("Plugin will disable now.");
				e.printStackTrace();
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			catch (InvalidPluginException | InvalidDescriptionException e)
			{
				e.printStackTrace();
			}
		}

		getServer().getPluginManager().registerEvents(new MobDefenseListener(), this);

		getCommand("mobdefense").setExecutor(new MobDefenseExecutor());

		gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter()).registerTypeAdapter(Wave.class, new WaveTypeAdapter()).setPrettyPrinting().create();

		try
		{
			if (!getDataFolder().isDirectory())
				//noinspection ResultOfMethodCallIgnored
				getDataFolder().mkdir();
			File configFile = new File(getDataFolder(), "config.yml");
			if (!configFile.exists())
				IOUtils.copy(getClass().getResourceAsStream("/config.yml"), FileUtils.openOutputStream(configFile));

			World world = Bukkit.getWorlds().get(0);
			YamlConfiguration config = (YamlConfiguration) getConfig();
			String playerSpawnStr = config.getString("player-spawn-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			playerSpawn = LocationConverter.instance().fromString(playerSpawnStr);
			String spawnStr = config.getString("spawn-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			spawn = LocationConverter.instance().fromString(spawnStr);
			String endStr = config.getString("end-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			end = LocationConverter.instance().fromString(endStr);
			String towerLoc = config.getString("npc-tower-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			npcTowerLoc = LocationConverter.instance().fromString(towerLoc);
			String upgradesLoc = config.getString("npc-upgrades-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			npcUpgradesLoc = LocationConverter.instance().fromString(upgradesLoc);
			String exchangeLoc = config.getString("npc-exchange-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			npcExchangeLoc = LocationConverter.instance().fromString(exchangeLoc);
			startMoney = config.getInt("start-money", 50);
			waveTime = config.getInt("wave-time", 60);
			baseLives = config.getInt("lives", 10);

			File file = new File(getDataFolder(), "mobs.json");
			if (file.exists())
				//noinspection unchecked
				((List<MobClass>) getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), new TypeToken<ArrayList<MobClass>>() {}.getType())).stream()
						.forEach(mobClass -> mobClasses.put(mobClass.getName(), mobClass));
			else
			{
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();

				ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
				helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 42);
				ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
				chestplate.addUnsafeEnchantment(Enchantment.THORNS, 42);
				ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
				leggings.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 42);
				ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
				boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 42);
				ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
				sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 42);
				ItemStack shield = new ItemStack(Material.SHIELD);
				shield.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 42);
				ItemMeta meta = shield.getItemMeta();
				meta.spigot().setUnbreakable(true);
				shield.setItemMeta(meta);
				MobClass sample = new MobClass("sample", "Sample Zombie", 42, 1.0F, EntityType.ZOMBIE, new ItemStack[]{helmet, chestplate, leggings, boots, sword, shield}, 42);
				mobClasses.put(sample.getName(), sample);
			}
			FileUtils.writeStringToFile(file, getGson().toJson(mobClasses.values()), StandardCharsets.UTF_8);

			file = new File(getDataFolder(), "waves.json");
			if (file.exists())
				waves.addAll(getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), new TypeToken<ArrayList<Wave>>() {}.getType()));
			else
			{
				Wave wave1 = new Wave();
				wave1.setNumber(1);
				//noinspection OptionalGetWithoutIsPresent
				wave1.getSpawns().put(mobClasses.values().stream().findAny().get(), 5);
				waves.add(wave1);

				Wave wave2 = new Wave();
				wave2.setNumber(2);
				//noinspection OptionalGetWithoutIsPresent
				wave2.getSpawns().put(mobClasses.values().stream().findAny().get(), 10);
				waves.add(wave2);
			}
			FileUtils.writeStringToFile(file, getGson().toJson(waves), StandardCharsets.UTF_8);

			world.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);

			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public Gson getGson()
	{
		return gson;
	}

	public void stop(CommandSender sender)
	{
		if (objective == null)
		{
			if (sender != null)
				sender.sendMessage("No game is running!");
			return;
		}

		Bukkit.broadcastMessage("[MobDefense] Game ended.");

		for (Tower tower : Tower.getAllTowers())
			Tower.breakAt(tower.getLocation());
		Bukkit.getWorlds().get(0).getEntities().stream().filter(entity -> entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.VILLAGER).forEach(Entity::remove);
		Bukkit.getOnlinePlayers().forEach(player -> player.getInventory().clear());
		getServer().getPluginManager().callEvent(new GameStoppedEvent(currentWave == null ? 0 : currentWave.getNumber()));
		currentWave = null;
		objective.unregister();
		objective = null;
		Bukkit.getScheduler().cancelTasks(MobDefense.instance());
	}

	public static MobDefense instance()
	{
		return instance;
	}

	public Location getPlayerSpawn()
	{
		return playerSpawn;
	}

	public Location getSpawn()
	{
		return spawn;
	}

	public Location getEnd()
	{
		return end;
	}

	@SuppressWarnings("unused")
	public int getBaseLives()
	{
		return baseLives;
	}

	public MobClass getMobClass(String name)
	{
		return mobClasses.get(name);
	}

	public void startNextWave()
	{
		if (currentWave == null)
			currentWave = waves.get(0);
		else
		{
			int index = waves.indexOf(currentWave);
			if (index != waves.size() - 1)
				currentWave = waves.get(index + 1);
			else
				currentWave.setNumber(currentWave.getNumber() + 1);
		}

		objective.getScore("Wave").setScore(currentWave.getNumber());
		currentWave.start();
	}

	public void start(CommandSender sender)
	{
		if (objective != null)
		{
			sender.sendMessage(ChatColor.RED + "A game is already started!");
			return;
		}

		Player giveTo;
		if (sender instanceof Player)
			giveTo = (Player) sender;
		else
		{
			List<Player> players = Lists.newArrayList(Bukkit.getOnlinePlayers());
			if (players.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "Any player is connected to start the game!");
				return;
			}
			else
				giveTo = players.get(((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().random.nextInt(players.size()));
		}

		int remainingMoney = startMoney;
		while (remainingMoney > 0)
		{
			giveTo.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, Math.min(remainingMoney, 64)));
			remainingMoney -= 64;
		}

		World world = Bukkit.getWorlds().get(0);
		Random random = ((CraftWorld) world).getHandle().random;

		for (int i = 0; i < 3; ++i)
		{
			Location loc = npcTowerLoc.clone().add(random.nextDouble() * 3.0D - 1.5D, 0, random.nextDouble() * 3.0D - 1.5D);
			Villager npcTower = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
			npcTower.setCollidable(false);
			npcTower.setAI(false);
			((CraftVillager) npcTower).getHandle().h(loc.getYaw());
			((CraftVillager) npcTower).getHandle().i(loc.getYaw());
			npcTower.setProfession(Villager.Profession.FARMER);
			List<MerchantRecipe> recipes = Lists.newArrayList();
			for (Class<? extends Tower> clazz : Tower.getTowerClasses())
			{
				ItemStack result = new ItemStack(Material.DISPENSER);
				ItemMeta meta = result.getItemMeta();
				meta.setDisplayName(Tower.getTowerName(clazz));
				result.setItemMeta(meta);
				List<Material> list = Arrays.stream(Material.values()).filter(Material::isSolid).collect(Collectors.toList());
				ItemStackUtils.setCanPlaceOn(result, list.toArray(new Material[list.size()]));
				MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);
				recipe.setIngredients(Lists.newArrayList(Tower.getTowerPrice(clazz)));
				recipes.add(recipe);
			}
			npcTower.setRecipes(recipes);
			npcTower.setCustomName("Towers");
		}

		for (int i = 0; i < 3; ++i)
		{
			Location loc = npcUpgradesLoc.clone().add(random.nextDouble() * 3.0D - 1.5D, 0, random.nextDouble() * 3.0D - 1.5D);
			Villager npcUpgrades = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
			npcUpgrades.setCollidable(false);
			npcUpgrades.setAI(false);
			((CraftVillager) npcUpgrades).getHandle().h(loc.getYaw());
			((CraftVillager) npcUpgrades).getHandle().i(loc.getYaw());
			npcUpgrades.setProfession(Villager.Profession.LIBRARIAN);
			npcUpgrades.setRecipes(Lists.newArrayList());
			npcUpgrades.setCustomName("Upgrades (Soon ...)");
		}

		for (int i = 0; i < 3; ++i)
		{
			Location loc = npcExchangeLoc.clone().add(random.nextDouble() * 4.0D - 1.0D, 0, random.nextDouble() * 3.0D - 1.5D);
			Villager npcExchange = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
			npcExchange.setCollidable(false);
			npcExchange.setAI(false);
			((CraftVillager) npcExchange).getHandle().h(loc.getYaw());
			((CraftVillager) npcExchange).getHandle().i(loc.getYaw());
			npcExchange.setProfession(Villager.Profession.BLACKSMITH);
			MerchantRecipe nuggetToIngot = new MerchantRecipe(new ItemStack(Material.GOLD_INGOT), Integer.MAX_VALUE);
			MerchantRecipe ingotToNugget = new MerchantRecipe(new ItemStack(Material.GOLD_NUGGET, 9), Integer.MAX_VALUE);
			MerchantRecipe ingotToBlock = new MerchantRecipe(new ItemStack(Material.GOLD_BLOCK), Integer.MAX_VALUE);
			MerchantRecipe blockToIngot = new MerchantRecipe(new ItemStack(Material.GOLD_INGOT, 9), Integer.MAX_VALUE);
			MerchantRecipe blockToEmerald = new MerchantRecipe(new ItemStack(Material.EMERALD), Integer.MAX_VALUE);
			MerchantRecipe emeraldToBlock = new MerchantRecipe(new ItemStack(Material.GOLD_BLOCK, 9), Integer.MAX_VALUE);
			MerchantRecipe emeraldToEBlock = new MerchantRecipe(new ItemStack(Material.EMERALD_BLOCK), Integer.MAX_VALUE);
			MerchantRecipe eBlockToEmerald = new MerchantRecipe(new ItemStack(Material.EMERALD, 9), Integer.MAX_VALUE);
			nuggetToIngot.addIngredient(new ItemStack(Material.GOLD_NUGGET, 9));
			ingotToNugget.addIngredient(new ItemStack(Material.GOLD_INGOT));
			ingotToBlock.addIngredient(new ItemStack(Material.GOLD_INGOT, 9));
			blockToIngot.addIngredient(new ItemStack(Material.GOLD_BLOCK));
			blockToEmerald.addIngredient(new ItemStack(Material.GOLD_BLOCK, 9));
			emeraldToBlock.addIngredient(new ItemStack(Material.EMERALD));
			emeraldToEBlock.addIngredient(new ItemStack(Material.EMERALD, 9));
			eBlockToEmerald.addIngredient(new ItemStack(Material.EMERALD_BLOCK));
			npcExchange
					.setRecipes(Lists.newArrayList(nuggetToIngot, ingotToNugget, ingotToBlock, blockToIngot, blockToEmerald, emeraldToBlock, emeraldToEBlock, eBlockToEmerald));
			npcExchange.setCustomName("Exchange");
		}

		objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("mobdefense", "dummy");
		objective.setDisplayName("[MobDefense]");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.getScore(ChatColor.RED.toString()).setScore(999);
		objective.getScore("Lives").setScore(baseLives);
		objective.getScore("Wave").setScore(0);

		getServer().getPluginManager().callEvent(new GameStartedEvent());
		Bukkit.broadcastMessage("[MobDefense] Game started!");
		Bukkit.getScheduler().runTaskTimer(this, this::startNextWave, waveTime * 20L, waveTime * 20L);
	}

	public Wave getCurrentWave()
	{
		return currentWave;
	}
}
