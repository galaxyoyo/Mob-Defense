package fr.galaxyoyo.mobdefense;

import com.adamki11s.pathing.AStar;
import com.adamki11s.pathing.PathingResult;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MobDefense extends JavaPlugin
{
	private static MobDefense instance;
	private Gson gson;
	private List<MobClass> mobClasses = Lists.newArrayList();
	private Location spawn, end;
	private AStar pathfinder;

	public static MobDefense instance()
	{
		return instance;
	}

	@Override
	public void onEnable()
	{
		instance = this;

		getServer().getPluginManager().registerEvents(new MobDefenseListener(), this);

		gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter()).setPrettyPrinting().create();

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
			saveConfig();

			pathfinder = new AStar(spawn.clone().subtract(0, 1, 0), end.clone().subtract(0, 1, 0), 142);
			pathfinder.iterate();
			if (pathfinder.getPathingResult() == PathingResult.NO_PATH)
			{
				getLogger().severe("****************************************************");
				getLogger().severe("No path were found between the 2 points.");
				getLogger().severe("Please check default map, spawn point and end point.");
				getLogger().severe("The plugin will now be disabled.");
				getLogger().severe("****************************************************");
				getServer().getPluginManager().disablePlugin(this);
			}

			File file = new File(getDataFolder(), "mobs.json");
			if (file.exists())
				mobClasses.addAll(gson.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), new TypeToken<ArrayList<MobClass>>() {}.getType()));
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
				MobClass sample = new MobClass("sample", "Sample Zombie", 42, 42.0F, EntityType.ZOMBIE, new ItemStack[]{helmet, chestplate, leggings, boots, sword, shield});
				mobClasses.add(sample);
				FileUtils.writeStringToFile(file, gson.toJson(mobClasses), StandardCharsets.UTF_8);
			}

			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		catch (AStar.InvalidPathException ex)
		{
			getLogger().severe("*************************************");
			getLogger().severe("An error occurred while path finding:");
			if (ex.isStartNotSolid())
				getLogger().severe("Start is not solid!");
			else if (ex.isEndNotSolid())
				getLogger().severe("End is not solid!");
			else
				ex.printStackTrace();
			getLogger().severe("The plugin will now be disabled.");
			getLogger().severe("*************************************");
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	public Gson getGson()
	{
		return gson;
	}

	public AStar getPathfinder()
	{
		return pathfinder;
	}

	public Location getSpawn()
	{
		return spawn;
	}

	public Location getEnd()
	{
		return end;
	}

	public List<MobClass> getMobClasses()
	{
		return mobClasses;
	}
}
