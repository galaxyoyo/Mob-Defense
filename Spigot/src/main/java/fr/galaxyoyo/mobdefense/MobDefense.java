package fr.galaxyoyo.mobdefense;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.galaxyoyo.mobdefense.towers.Tower;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobDefense extends JavaPlugin
{
	private static MobDefense instance;
	private Gson gson;
	private Map<String, MobClass> mobClasses = Maps.newHashMap();
	private Location spawn, end;
	private int startMoney;
	private int waveTime;
	private int maxMobs;
	private List<Wave> waves = Lists.newArrayList();
	private Wave currentWave;

	public static MobDefense instance()
	{
		return instance;
	}

	@Override
	public void onEnable()
	{
		instance = this;

		getServer().getPluginManager().registerEvents(new MobDefenseListener(), this);

		getCommand("mobdefense").setExecutor(new MobDefenseExecutor());

		gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter()).registerTypeAdapter(Wave.class, new WaveTypeAdapter()).setPrettyPrinting().create();

		try
		{
			World world = Bukkit.getWorlds().get(0);
			YamlConfiguration config = (YamlConfiguration) getConfig();
			String spawnStr = config.getString("spawn-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("spawn-loc", spawnStr);
			spawn = LocationConverter.instance().fromString(spawnStr);
			String endStr = config.getString("end-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("end-loc", endStr);
			end = LocationConverter.instance().fromString(endStr);
			String towerLoc = config.getString("npc-tower-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("npc-tower-loc", towerLoc);
			Location npcTowerLoc = LocationConverter.instance().fromString(towerLoc);
			String upgradesLoc = config.getString("npc-upgrades-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("npc-upgrades-loc", upgradesLoc);
			Location npcUpgradesLoc = LocationConverter.instance().fromString(upgradesLoc);
			String exchangeLoc = config.getString("npc-exchange-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("npc-exchange-loc", exchangeLoc);
			Location npcExchangeLoc = LocationConverter.instance().fromString(exchangeLoc);
			startMoney = config.getInt("start-money", 50);
			config.set("start-money", startMoney);
			waveTime = config.getInt("wave-time", 30);
			config.set("wave-time", waveTime);
			maxMobs = config.getInt("max-mobs", 10);
			config.set("max-mobs", maxMobs);
			saveConfig();

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

			Random random = ((CraftWorld) world).getHandle().random;

			for (int i = 0; i < 3; ++i)
			{
				Villager npcTower = (Villager) world.spawnEntity(npcTowerLoc.clone().add(random.nextDouble() * 2.0D - 1.0D, 0, random.nextDouble() * 2.0D - 1.0D),
						EntityType.VILLAGER);
				npcTower.setCollidable(false);
				npcTower.setAI(false);
				npcTower.setProfession(Villager.Profession.FARMER);
				List<MerchantRecipe> recipes = Lists.newArrayList();
				for (Class<? extends Tower> clazz : Tower.getTowerClasses())
				{
					ItemStack result = new ItemStack(Material.DISPENSER);
					ItemMeta meta = result.getItemMeta();
					meta.setDisplayName(Tower.getTowerName(clazz));
					result.setItemMeta(meta);
					MerchantRecipe recipe = new MerchantRecipe(result, Integer.MAX_VALUE);
					recipe.setIngredients(Lists.newArrayList(Tower.getTowerPrice(clazz)));
					recipes.add(recipe);
				}
				npcTower.setRecipes(recipes);
				npcTower.setCustomName("Towers");
			}

			for (int i = 0; i < 3; ++i)
			{
				Villager npcUpgrades = (Villager) world.spawnEntity(npcUpgradesLoc.clone().add(random.nextDouble() * 2.0D - 1.0D, 0, random.nextDouble() * 2.0D - 1.0D),
						EntityType.VILLAGER);
				npcUpgrades.setCollidable(false);
				npcUpgrades.setAI(false);
				npcUpgrades.setProfession(Villager.Profession.LIBRARIAN);
				npcUpgrades.setRecipes(Lists.newArrayList());
				npcUpgrades.setCustomName("Upgrades (Soon ...)");
			}

			for (int i = 0; i < 3; ++i)
			{
				Villager npcExchange = (Villager) world.spawnEntity(npcExchangeLoc.clone().add(random.nextDouble() * 2.0D - 1.0D, 0, random.nextDouble() * 2.0D - 1.0D),
						EntityType.VILLAGER);
				npcExchange.setCollidable(false);
				npcExchange.setAI(false);
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
				npcExchange.setRecipes(Lists.newArrayList(nuggetToIngot, ingotToNugget, ingotToBlock, blockToIngot, blockToEmerald, emeraldToBlock, emeraldToEBlock, eBlockToEmerald));
				npcExchange.setCustomName("Exchange");
			}

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

	public Location getSpawn()
	{
		return spawn;
	}

	public Location getEnd()
	{
		return end;
	}

	public int getMaxMobs()
	{
		return maxMobs;
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
				currentWave.setNumber(index + 1);
		}

		currentWave.start();
	}

	public void start(CommandSender sender)
	{
		if (currentWave != null)
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

		Bukkit.broadcastMessage("[MobDefense] Game started!");

		Bukkit.getScheduler().runTaskTimer(this, this::startNextWave, waveTime * 20L, waveTime * 20L);
	}
}
