package fr.galaxyoyo.mobdefense;

import lombok.Getter;
import net.cubespace.Yamler.Config.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class Configuration extends YamlConfig implements Serializable
{
	private static final transient Location SPAWN_LOC = Bukkit.getWorlds().get(0).getSpawnLocation();

	@Getter
	@Comment("Location of player spawn (x:y:z:yaw:pitch)")
	private Location playerSpawn = SPAWN_LOC;

	@Getter
	@Comment("Location of mob spawn (x:y:z:yaw:pitch)")
	private Location spawn = SPAWN_LOC;

	@Getter
	@Comment("Location of mob objective (x:y:z:yaw:pitch)")
	private Location end = SPAWN_LOC;

	@Getter
	@Comment("The amount of gold nuggets you start with")
	private int startMoney = 150;

	@Getter
	@Comments({"The time (in seconds) between each wave, after the last mob of", "last wave spawned"})
	private int waveTime = 42;

	@Getter
	@Comment("The number of mobs that can reach the end point before you loose")
	private int lives = 10;

	@Getter
	@Comment("Location of towers seller (x:y:z:yaw:pitch)")
	private Location npcTowerLoc = SPAWN_LOC;

	@Getter
	@Comment("Location of tower upgrades seller (x:y:z:yaw:pitch)")
	private Location npcUpgradesLoc = SPAWN_LOC;

	@Getter
	@Comment("Location of exchanger (x:y:z:yaw:pitch)")
	private Location npcExchangeLoc = SPAWN_LOC;

	@Getter
	@Comment("Tower update rate, in ticks")
	private int towerUpdateRate = 20;

	@Getter
	@Comment("Preferred language code for messages (default: en; supporteds: en, fr)")
	private String preferredLanguage = Locale.getDefault().getLanguage().toLowerCase();

	@Getter
	@Comment("If true, players will receive messages in the language of the server.\nIf false, players will receive messages in their " +
			"own language (if available).")
	private boolean forcePreferredLanguage = false;

	public Configuration() throws InvalidConfigurationException
	{
		CONFIG_HEADER = new String[]{"##################################################################",
				"###################  Mob Defense configuration ###################",
				"##################################################################"};
		CONFIG_FILE = new File(MobDefense.instance().getDataFolder(), "config.yml");
		try
		{
			addConverter(LocationConverter.class);
		}
		catch (InvalidConverterException e)
		{
			e.printStackTrace();
		}
		init();
	}

	public void setPlayerSpawn(Location spawn)
	{
		this.playerSpawn = spawn;
		save();
	}

	@Override
	public void save()
	{
		try
		{
			super.save();
		}
		catch (InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void setSpawn(Location spawn)
	{
		this.spawn = spawn;
		save();
	}

	public void setEnd(Location end)
	{
		this.end = end;
		save();
	}

	public void setNpcTowerLoc(Location loc)
	{
		this.npcTowerLoc = loc;
		save();
	}

	public void setNpcUpgradesLoc(Location loc)
	{
		this.npcUpgradesLoc = loc;
		save();
	}

	public void setNpcExchangeLoc(Location loc)
	{
		this.npcExchangeLoc = loc;
		save();
	}
}
