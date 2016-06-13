package fr.galaxyoyo.mobdefense;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.galaxyoyo.mobdefense.towers.Tower;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import java.util.Random;

public class MobDefense extends JavaPlugin
{
	private static MobDefense instance;
	private Gson gson;
	private List<MobClass> mobClasses = Lists.newArrayList();
	private Location spawn, end;
	private Location npcTowerLoc, npcUpgradesLoc, npcExchangeLoc;

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
			String towerLoc = config.getString("npc-tower-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("npc-tower-loc", towerLoc);
			npcTowerLoc = LocationConverter.instance().fromString(towerLoc);
			String upgradesLoc = config.getString("npc-upgrades-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("npc-upgrades-loc", upgradesLoc);
			npcUpgradesLoc = LocationConverter.instance().fromString(upgradesLoc);
			String exchangeLoc = config.getString("npc-exchange-loc", LocationConverter.instance().toString(world.getSpawnLocation()));
			config.set("npc-exchange-loc", exchangeLoc);
			npcExchangeLoc = LocationConverter.instance().fromString(exchangeLoc);
			saveConfig();

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
				MobClass sample = new MobClass("sample", "Sample Zombie", 42, 42.0F, EntityType.ZOMBIE, new ItemStack[]{helmet, chestplate, leggings, boots, sword, shield}, 42);
				mobClasses.add(sample);
				FileUtils.writeStringToFile(file, gson.toJson(mobClasses), StandardCharsets.UTF_8);
			}

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

	public List<MobClass> getMobClasses()
	{
		return mobClasses;
	}
}
