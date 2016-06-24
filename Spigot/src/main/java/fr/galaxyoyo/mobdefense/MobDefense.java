package fr.galaxyoyo.mobdefense;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.galaxyoyo.mobdefense.events.GameStartedEvent;
import fr.galaxyoyo.mobdefense.events.GameStoppedEvent;
import fr.galaxyoyo.mobdefense.towers.Tower;
import fr.galaxyoyo.mobdefense.towers.TowerRegistration;
import fr.galaxyoyo.mobdefense.upgrades.Upgrade;
import fr.galaxyoyo.mobdefense.upgrades.UpgradeRegistration;
import fr.galaxyoyo.spigot.nbtapi.EntityUtils;
import fr.galaxyoyo.spigot.nbtapi.ItemStackUtils;
import fr.galaxyoyo.spigot.nbtapi.ReflectionUtils;
import fr.galaxyoyo.spigot.nbtapi.TagCompound;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.mcstats.Metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class MobDefense extends JavaPlugin
{
	private static MobDefense instance;
	private String latestVersion;
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
	private int towerUpdateRate;
	private List<Wave> waves = Lists.newArrayList();
	private Wave currentWave;
	private Objective objective;

	@Override
	public void onLoad()
	{
		getLogger().info("You're running the MobDefense plugin, by galaxyoyo. Thanks for buying it!");
		JavaPlugin nbtapi = (JavaPlugin) getServer().getPluginManager().getPlugin("NBTAPI");
		String latestNBTAPIVersion = getLatestSpigotVersion(24908);
		boolean needToUpdate = nbtapi == null;
		if (nbtapi != null && new Version(nbtapi.getDescription().getVersion()).compareTo(new Version(latestNBTAPIVersion)) < 0)
		{
			needToUpdate = true;
			getServer().getPluginManager().disablePlugin(nbtapi);
		}
		if (needToUpdate)
		{
			getLogger().info("Downloading version " + latestNBTAPIVersion + " of NBTAPI ...");
			try
			{
				File file = new File("plugins", "NBTAPI.jar");
				URL url = getLatestDownloadURL("nbtapi", 24908);
				HttpURLConnection co = (HttpURLConnection) url.openConnection();
				co.setRequestMethod("GET");
				co.setRequestProperty("User-Agent", "Mozilla/5.0");
				co.setRequestProperty("Connection", "Close");
				co.connect();
				FileUtils.copyInputStreamToFile(co.getInputStream(), file);
				co.disconnect();
				getServer().getPluginManager().loadPlugin(file);
			}
			catch (IOException e)
			{
				getLogger().severe("Unable to download NBTAPI library. Make sure you have the latest version of MobDefense and have an Internet connection.");
				getLogger().severe("Plugin will disable now.");
				e.printStackTrace();
				getServer().getPluginManager().disablePlugin(this);
			}
			catch (InvalidPluginException | InvalidDescriptionException e)
			{
				e.printStackTrace();
			}

			Bukkit.reload();
		}
	}

	private String getLatestSpigotVersion(int resourceId)
	{
		try
		{
			HttpURLConnection con = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.getOutputStream().write(
					("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + resourceId).getBytes("UTF-8"));
			String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			if (version.length() <= 7)
				return version;
		}
		catch (Exception ex)
		{
			getLogger().warning("Failed to check for a update on spigot.");
		}
		return null;
	}

	public URL getLatestDownloadURL(String resourceName, int resourceId)
	{
		try
		{
			String url = "https://www.spigotmc.org/resources/" + (resourceName != null ? resourceName + "." : "") + resourceId + "/";
			URL u = new URL(url);
			HttpURLConnection co = (HttpURLConnection) u.openConnection();
			co.setRequestMethod("GET");
			co.setRequestProperty("User-Agent", "Mozilla/5.0");
			co.setRequestProperty("Connection", "Keep-Alive");
			co.connect();
			String content = IOUtils.toString(co.getInputStream(), StandardCharsets.UTF_8);
			co.disconnect();
			String innerLabel = content.substring(content.indexOf("<label class=\"downloadButton \">"));
			innerLabel = innerLabel.substring(0, innerLabel.indexOf("</label>"));
			String downloadURL = innerLabel.substring(innerLabel.indexOf("<a href=\"") + 9);
			downloadURL = downloadURL.substring(0, downloadURL.indexOf("\" class=\"inner\">"));
			downloadURL = "https://www.spigotmc.org/" + downloadURL;
			return new URL(downloadURL);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public void onDisable()
	{
		stop(null);
	}

	public void stop(CommandSender sender)
	{
		if (objective == null)
		{
			if (sender != null)
				sender.sendMessage("[MobDefense] No game is running!");
			return;
		}

		Bukkit.broadcastMessage("[MobDefense] Game ended.");

		for (Tower tower : Tower.getAllTowers())
			Tower.breakAt(tower.getLocation());
		Bukkit.getWorlds().get(0).getEntities().stream().filter(entity -> entity.getType() != EntityType.PLAYER).forEach(Entity::remove);
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

	@Override
	public void onEnable()
	{
		instance = this;

		getLogger().info("Checking server version ...");
		try
		{
			NMSUtils.ServerVersion version = NMSUtils.getServerVersion();
			getLogger().info("You're running Minecraft server " + version.name() + ", for Minecraft " + version.getServerName() + ".");
			if (version.isBefore1_9())
			{
				getLogger().warning("**************************************************************************************************");
				getLogger().warning("Warning: the 1.8 version of MobDefense contains less features as 1.9 and 1.10, like tipped arrows.");
				getLogger().warning("It mays contain some compatibility issues. I'm able to fix these, so please report  them.");
				getLogger().warning("But please note that in some versions, this compatiblity will be removed.");
				getLogger().warning("If you want a better gameplay, please update your server to 1.9.4 or 1.10 (1.10 recomended).");
				getLogger().warning("There's no plugin update required.");
				getLogger().warning("**************************************************************************************************");
			}
		}
		catch (UnsupportedClassVersionError error)
		{
			getLogger().severe("You're running Minecraft server " + error.getMessage() + ". This version is unsupported by MobDefense now. This plugin will now be disabled.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		getServer().getPluginManager().registerEvents(new MobDefenseListener(), this);

		MobDefenseExecutor executor = new MobDefenseExecutor();
		getCommand("mobdefense").setExecutor(executor);
		getCommand("mobdefense").setTabCompleter(executor);

		gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter()).registerTypeAdapter(Wave.class, new WaveTypeAdapter()).setPrettyPrinting().create();

		try
		{
			String version = getLatestSpigotVersion(25068);
			if (new Version(getDescription().getVersion()).compareTo(new Version(version)) < 0)
			{
				getLogger().warning(
						"This plugin is outdated. The last version is " + version + " and you're running " + getDescription().getVersion() + ". Please update, there're maybe some " +
								"fixes or new features.");
				latestVersion = version;
			}

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
			towerUpdateRate = config.getInt("tower-update-rate", 20);
			startMoney = config.getInt("start-money", 50);
			waveTime = config.getInt("wave-time", 42);
			baseLives = config.getInt("lives", 10);

			File file = new File(getDataFolder(), "mobs.json");
			if (file.exists())
				//noinspection unchecked
				((List<MobClass>) getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), new TypeToken<ArrayList<MobClass>>() {}.getType()))
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
				ItemStack[] stacks;
				if (NMSUtils.getServerVersion().isBefore1_9())
					stacks = new ItemStack[]{helmet, chestplate, leggings, boots, sword};
				else
				{
					ItemStack shield = new ItemStack(Material.SHIELD);
					shield.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 42);
					ItemMeta meta = shield.getItemMeta();
					meta.spigot().setUnbreakable(true);
					shield.setItemMeta(meta);
					stacks = new ItemStack[]{helmet, chestplate, leggings, boots, sword, shield};
				}
				MobClass sample = new MobClass("sample", "Sample Zombie", 42, 1.0F, EntityType.ZOMBIE, stacks, 42);
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
				wave1.getSpawns().put(mobClasses.values().stream().findAny().get(), 5.0D);
				waves.add(wave1);

				Wave wave2 = new Wave();
				wave2.setNumber(2);
				//noinspection OptionalGetWithoutIsPresent
				wave2.getSpawns().put(mobClasses.values().stream().findAny().get(), 10.0D);
				waves.add(wave2);
			}
			FileUtils.writeStringToFile(file, getGson().toJson(waves), StandardCharsets.UTF_8);

			file = new File(getDataFolder(), "towers.json");
			if (file.exists())
				//noinspection unchecked
				((ArrayList<TowerRegistration>) getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), new TypeToken<ArrayList<TowerRegistration>>() {}.getType
						())).forEach(Tower::registerTower);
			else
			{
				TowerRegistration basic = new TowerRegistration("ArrowEffectTower", "Simple Tower", Lists.newArrayList("Launches basic arrows twice every second.", "It is the most " +
						"basic tower you can find, but not the cheapest :)"), new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 5)}, Material.WOOD, Collections.singletonMap("rate",
						10));
				Tower.registerTower(basic);
				TowerRegistration spectral =
						new TowerRegistration("SpectralTower", "Spectral Tower", Lists.newArrayList("Launches basic spectral arrows.", "It's not very useful, but it looks cool ..."),
								new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 7)}, Material.GLOWSTONE, Collections.singletonMap("glowingTicks", 500));
				Tower.registerTower(spectral);
				TowerRegistration healing = new TowerRegistration("ArrowEffectTower", "Healing Tower",
						Lists.newArrayList("Launches Instant Healing arrows.", "Remember: Instant Healing deals damage to zombies, skeletons and pigmens!"),
						new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 1)}, Material.BEACON,
						Collections.singletonMap("basePotionType", PotionType.INSTANT_HEAL.name().toLowerCase()));
				Tower.registerTower(healing);
				TowerRegistration damage =
						new TowerRegistration("ArrowEffectTower", "Damage Tower", Lists.newArrayList("Launches Instant Damage arrows.", "Remember: instant damage heals" +
								" zombies, skeletons and pigmens!"), new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 3)}, NMSUtils.getServerVersion().isAfter1_10() ? Material
								.NETHER_WART_BLOCK : Material.NETHER_BRICK, Collections.singletonMap("basePotionType", PotionType.INSTANT_DAMAGE.name().toLowerCase()));
				Tower.registerTower(damage);
				TowerRegistration poison =
						new TowerRegistration("ArrowEffectTower", "Poison Tower",
								Lists.newArrayList("Launches Poison arrows.", "Remember: poison heals zombies, skeletons and pigmens!"),
								new ItemStack[]{new ItemStack(Material.GOLD_NUGGET, 3)}, Material.SLIME_BLOCK, Collections.singletonMap("basePotionType", PotionType.POISON.name()
								.toLowerCase()));
				Tower.registerTower(poison);
			}
			FileUtils.writeStringToFile(file, getGson().toJson(Tower.getTowerRegistrations()), StandardCharsets.UTF_8);

			file = new File(getDataFolder(), "upgrades.json");
			if (file.exists())
				//noinspection unchecked
				((ArrayList<UpgradeRegistration>) getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), new TypeToken<ArrayList<UpgradeRegistration>>() {}
						.getType())).forEach(Upgrade::registerUpgrade);
			else
			{
				ItemStack stack = new ItemStack(Material.BOW);
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_DAMAGE, 0, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				meta.setDisplayName("Rate upgrade");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Divide tower rate by 2."));
				stack.setItemMeta(meta);
				UpgradeRegistration rate2 =
						new UpgradeRegistration("RateUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_NUGGET, 2)},
								Collections.singletonMap("divider", 2.0D));
				Upgrade.registerUpgrade(rate2);
				meta.setDisplayName("Rate upgrade II");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Divide tower rate by 4."));
				stack.setItemMeta(meta);
				UpgradeRegistration rate4 = new UpgradeRegistration("RateUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 2)}, Collections.singletonMap
						("divider", 4.0D));
				Upgrade.registerUpgrade(rate4);
				meta.setDisplayName("Rate upgrade III");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Divide tower rate by 8."));
				stack.setItemMeta(meta);
				UpgradeRegistration rate8 = new UpgradeRegistration("RateUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material
						.GOLD_NUGGET, 5)}, Collections.singletonMap("divider", 8.0D));
				Upgrade.registerUpgrade(rate8);
				stack.setType(Material.ARROW);
				meta.setDisplayName("Range upgrade");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Multiply tower range by 2."));
				stack.setItemMeta(meta);
				UpgradeRegistration range2 =
						new UpgradeRegistration("RangeUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_NUGGET, 2)},
								Collections.singletonMap("multiplier", 2.0D));
				Upgrade.registerUpgrade(range2);
				meta.setDisplayName("Range upgrade II");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Multiply tower range by 4."));
				stack.setItemMeta(meta);
				UpgradeRegistration range4 = new UpgradeRegistration("RangeUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 2)}, Collections.singletonMap
						("multiplier", 4.0D));
				Upgrade.registerUpgrade(range4);
				meta.setDisplayName("Range upgrade III");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Multiply tower range by 8."));
				stack.setItemMeta(meta);
				UpgradeRegistration range8 = new UpgradeRegistration("RangeUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material
						.GOLD_NUGGET, 5)}, Collections.singletonMap("multiplier", 8.0D));
				Upgrade.registerUpgrade(range8);
				stack.setType(Material.POTION);
				meta = stack.getItemMeta();
				((PotionMeta) meta).setBasePotionData(new PotionData(PotionType.SPEED));
				meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
				meta.setDisplayName("Speed upgrade");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Multiply tower speed by 2."));
				stack.setItemMeta(meta);
				UpgradeRegistration speed2 =
						new UpgradeRegistration("SpeedUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_NUGGET, 2)},
								Collections.singletonMap("multiplier", 2.0D));
				Upgrade.registerUpgrade(speed2);
				meta.setDisplayName("Speed upgrade II");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Multiply tower speed by 4."));
				stack.setItemMeta(meta);
				UpgradeRegistration speed4 = new UpgradeRegistration("SpeedUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 2)}, Collections.singletonMap
						("multiplier", 4.0D));
				Upgrade.registerUpgrade(speed4);
				meta.setDisplayName("Speed upgrade III");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Multiply tower speed by 8."));
				stack.setItemMeta(meta);
				UpgradeRegistration speed8 = new UpgradeRegistration("SpeedUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material
						.GOLD_NUGGET, 5)}, Collections.singletonMap("multiplier", 8.0D));
				Upgrade.registerUpgrade(speed8);
				stack.setType(Material.POWERED_RAIL);
				meta = stack.getItemMeta();
				meta.setDisplayName("Extended Potion Upgrade");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Extend all potion effects", ChatColor.RESET + "of arrows of potion towers"));
				stack.setItemMeta(meta);
				UpgradeRegistration extended = new UpgradeRegistration("ExtendedUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 2)}, Collections.emptyMap
						());
				Upgrade.registerUpgrade(extended);
				stack.setType(Material.FIREWORK);
				meta.setDisplayName("Upgraged Potion Upgrade");
				meta.setLore(Lists.newArrayList(ChatColor.RESET + "Upgrade all potion effects", ChatColor.RESET + "of arrows of potion towers"));
				stack.setItemMeta(meta);
				UpgradeRegistration upgraded = new UpgradeRegistration("UpgradedUpgrade", stack.clone(), new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 2)}, Collections.emptyMap
						());
				Upgrade.registerUpgrade(upgraded);
			}
			FileUtils.writeStringToFile(file, getGson().toJson(Upgrade.getUpgradeRegistrations()), StandardCharsets.UTF_8);

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

	public String getLatestVersion()
	{
		return latestVersion;
	}

	public Location getPlayerSpawn()
	{
		return playerSpawn;
	}

	protected void setPlayerSpawn(Location location)
	{
		this.playerSpawn = location;
		getConfig().set("player-spawn-loc", LocationConverter.instance().toString(location));
	}

	public Location getSpawn()
	{
		return spawn;
	}

	protected void setSpawn(Location location)
	{
		this.spawn = location;
		getConfig().set("spawn-loc", LocationConverter.instance().toString(location));
	}

	public Location getEnd()
	{
		return end;
	}

	protected void setEnd(Location location)
	{
		this.end = location;
		getConfig().set("end-loc", LocationConverter.instance().toString(location));
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
			{
				currentWave.setNumber(currentWave.getNumber() + 1);
				currentWave.getSpawns().entrySet().forEach(entry -> entry.setValue((entry.getValue() + entry.getValue() / (2 * Math.sqrt(currentWave.getNumber() - waves.size() + 1))
				)));
			}
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

		Random random = ReflectionUtils.getNMSField("World", ReflectionUtils.invokeBukkitMethod("getHandle", Bukkit.getWorlds().get(0)), "random");

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
				giveTo = players.get(random.nextInt(players.size()));
		}

		int remainingMoney = startMoney;
		while (remainingMoney > 0)
		{
			giveTo.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, Math.min(remainingMoney, 64)));
			remainingMoney -= 64;
		}

		World world = Bukkit.getWorlds().get(0);

		for (int i = 0; i < 3; ++i)
		{
			Location loc = npcTowerLoc.clone().add(random.nextDouble() * 3.0D - 1.5D, 0, random.nextDouble() * 3.0D - 1.5D);
			Villager npcTower = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
			if (NMSUtils.getServerVersion().isAfter1_9())
			{
				npcTower.setCollidable(false);
				npcTower.setAI(false);
			}
			else
			{
				TagCompound compound = EntityUtils.getTagCompound(npcTower);
				compound.setByte("NoAI", (byte) 1);
				EntityUtils.setTagCompound(npcTower, compound);
			}
			NMSUtils.setEntityYaw(npcTower, loc.getYaw());
			npcTower.setProfession(Villager.Profession.FARMER);
			List recipes = Lists.newArrayList();
			if (NMSUtils.getServerVersion().isBefore1_9())
			{
				Object handle = ReflectionUtils.invokeBukkitMethod("getHandle", npcTower);
				recipes = ReflectionUtils.invokeNMSMethod("getOffers", handle, new Class<?>[]{ReflectionUtils.getNMSClass("EntityHuman")}, (Object) null);
				recipes.clear();
			}
			for (TowerRegistration tr : Tower.getTowerRegistrations())
			{
				ItemStack result = new ItemStack(Material.DISPENSER);
				ItemMeta meta = result.getItemMeta();
				meta.setDisplayName(tr.getDisplayName());
				meta.setLore(tr.getLore());
				meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
				result.setItemMeta(meta);
				List<Material> list = Arrays.stream(Material.values()).filter(Material::isSolid).collect(Collectors.toList());
				ItemStackUtils.setCanPlaceOn(result, list.toArray(new Material[list.size()]));
				if (NMSUtils.getServerVersion().isAfter1_9())
				{
					MerchantRecipe recipe = new MerchantRecipe(result, 0, Integer.MAX_VALUE, true);
					recipe.setIngredients(Lists.newArrayList(tr.getCost()));
					//noinspection unchecked
					recipes.add(recipe);
				}
				else
				{
					TagCompound tag = new TagCompound();
					tag.setCompound("buy", ItemStackUtils.getAllStackCompound(tr.getCost()[0]));
					if (tr.getCost().length >= 2)
						tag.setCompound("buyB", ItemStackUtils.getAllStackCompound(tr.getCost()[1]));
					tag.setCompound("sell", ItemStackUtils.getAllStackCompound(result));
					tag.setInt("maxUses", Integer.MAX_VALUE);
					tag.setByte("rewardExp", (byte) 0);
					Object merchantRecipe = ReflectionUtils.newNMS("MerchantRecipe", new Class<?>[]{ReflectionUtils.getNMSClass("NBTTagCompound")}, tag.convertToNMS());
					//noinspection unchecked
					recipes.add(merchantRecipe);
				}
			}
			if (NMSUtils.getServerVersion().isAfter1_9())
				//noinspection unchecked
				npcTower.setRecipes(recipes);
			npcTower.setCustomName("Towers");
		}

		for (int i = 0; i < 3; ++i)
		{
			Location loc = npcUpgradesLoc.clone().add(random.nextDouble() * 3.0D - 1.5D, 0, random.nextDouble() * 3.0D - 1.5D);
			Villager npcUpgrades = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
			if (NMSUtils.getServerVersion().isAfter1_9())
			{
				npcUpgrades.setCollidable(false);
				npcUpgrades.setAI(false);
			}
			else
			{
				TagCompound compound = EntityUtils.getTagCompound(npcUpgrades);
				compound.setByte("NoAI", (byte) 1);
				EntityUtils.setTagCompound(npcUpgrades, compound);
			}
			NMSUtils.setEntityYaw(npcUpgrades, loc.getYaw());
			npcUpgrades.setProfession(Villager.Profession.LIBRARIAN);
			List recipes = Lists.newArrayList();
			if (NMSUtils.getServerVersion().isBefore1_9())
			{
				Object handle = ReflectionUtils.invokeBukkitMethod("getHandle", npcUpgrades);
				recipes = ReflectionUtils.invokeNMSMethod("getOffers", handle, new Class<?>[]{ReflectionUtils.getNMSClass("EntityHuman")}, (Object) null);
				recipes.clear();
			}
			for (UpgradeRegistration ur : Upgrade.getUpgradeRegistrations())
			{
				ItemStack result = ur.getItem().clone();
				if (NMSUtils.getServerVersion().isAfter1_9())
				{
					MerchantRecipe recipe = new MerchantRecipe(result, 0, Integer.MAX_VALUE, true);
					recipe.setIngredients(Lists.newArrayList(ur.getCost()));
					//noinspection unchecked
					recipes.add(recipe);
				}
				else
				{
					TagCompound tag = new TagCompound();
					tag.setCompound("buy", ItemStackUtils.getAllStackCompound(ur.getCost()[0]));
					if (ur.getCost().length >= 2)
						tag.setCompound("buyB", ItemStackUtils.getAllStackCompound(ur.getCost()[1]));
					tag.setCompound("sell", ItemStackUtils.getAllStackCompound(result));
					tag.setInt("maxUses", Integer.MAX_VALUE);
					tag.setByte("rewardExp", (byte) 0);
					Object merchantRecipe = ReflectionUtils.newNMS("MerchantRecipe", new Class<?>[]{ReflectionUtils.getNMSClass("NBTTagCompound")}, tag.convertToNMS());
					//noinspection unchecked
					recipes.add(merchantRecipe);
				}
			}
			if (NMSUtils.getServerVersion().isAfter1_9())
				//noinspection unchecked
				npcUpgrades.setRecipes(recipes);
			npcUpgrades.setCustomName("Upgrades");
		}

		for (int i = 0; i < 3; ++i)
		{
			Location loc = npcExchangeLoc.clone().add(random.nextDouble() * 4.0D - 1.0D, 0, random.nextDouble() * 3.0D - 1.5D);
			Villager npcExchange = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
			if (NMSUtils.getServerVersion().isAfter1_9())
			{
				npcExchange.setCollidable(false);
				npcExchange.setAI(false);
			}
			else
			{
				TagCompound compound = EntityUtils.getTagCompound(npcExchange);
				compound.setByte("NoAI", (byte) 1);
				EntityUtils.setTagCompound(npcExchange, compound);
			}
			NMSUtils.setEntityYaw(npcExchange, loc.getYaw());
			npcExchange.setProfession(Villager.Profession.BLACKSMITH);
			Material[] mats = new Material[]{Material.GOLD_NUGGET, Material.GOLD_INGOT, Material.GOLD_BLOCK, Material.EMERALD, Material.EMERALD_BLOCK};
			if (NMSUtils.getServerVersion().isAfter1_9())
			{
				List<MerchantRecipe> recipes = Lists.newArrayList();
				for (int j = 0; j < mats.length - 1; j++)
				{
					ItemStack cost = new ItemStack(mats[j], 9);
					ItemStack result = new ItemStack(mats[j + 1]);
					MerchantRecipe recipe1 = new MerchantRecipe(result, 0, Integer.MAX_VALUE, true);
					MerchantRecipe recipe2 = new MerchantRecipe(cost, 0, Integer.MAX_VALUE, true);
					recipe1.addIngredient(cost);
					recipe2.addIngredient(result);
					recipes.add(recipe1);
					recipes.add(recipe2);
				}
				npcExchange.setRecipes(recipes);
			}
			else
			{
				Object handle = ReflectionUtils.invokeBukkitMethod("getHandle", npcExchange);
				List l = ReflectionUtils.invokeNMSMethod("getOffers", handle, new Class<?>[]{ReflectionUtils.getNMSClass("EntityHuman")}, (Object) null);
				l.clear();
				for (int j = 0; j < mats.length - 1; j++)
				{
					ItemStack cost = new ItemStack(mats[j], 9);
					ItemStack result = new ItemStack(mats[j + 1]);
					TagCompound tag1 = new TagCompound();
					tag1.setCompound("buy", ItemStackUtils.getAllStackCompound(cost));
					tag1.setCompound("sell", ItemStackUtils.getAllStackCompound(result));
					tag1.setInt("maxUses", Integer.MAX_VALUE);
					tag1.setByte("rewardExp", (byte) 0);
					TagCompound tag2 = new TagCompound();
					tag2.setCompound("buy", ItemStackUtils.getAllStackCompound(result));
					tag2.setCompound("sell", ItemStackUtils.getAllStackCompound(cost));
					tag2.setInt("maxUses", Integer.MAX_VALUE);
					tag2.setByte("rewardExp", (byte) 0);
					Object merchantRecipe1 = ReflectionUtils.newNMS("MerchantRecipe", new Class<?>[]{ReflectionUtils.getNMSClass("NBTTagCompound")}, tag1.convertToNMS());
					Object merchantRecipe2 = ReflectionUtils.newNMS("MerchantRecipe", new Class<?>[]{ReflectionUtils.getNMSClass("NBTTagCompound")}, tag2.convertToNMS());
					//noinspection unchecked
					l.add(merchantRecipe1);
					//noinspection unchecked
					l.add(merchantRecipe2);

				}
			}
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
		Bukkit.getScheduler().runTaskLater(this, this::startNextWave, waveTime * 20L);
	}

	public Wave getCurrentWave()
	{
		return currentWave;
	}

	public int getWaveTime()
	{
		return waveTime;
	}

	protected void setNpcTowerLoc(Location location)
	{
		this.npcTowerLoc = location;
		getConfig().set("npc-tower-loc", LocationConverter.instance().toString(location));
	}

	protected void setNpcUpgradesLoc(Location location)
	{
		this.npcUpgradesLoc = location;
		getConfig().set("npc-upgrades-loc", LocationConverter.instance().toString(location));
	}

	protected void setNpcExchangeLoc(Location location)
	{
		this.npcExchangeLoc = location;
		getConfig().set("npc-exchange-loc", LocationConverter.instance().toString(location));
	}

	public int getTowerUpdateRate()
	{
		return towerUpdateRate;
	}
}
