package fr.galaxyoyo.mobdefense;

import net.cubespace.Yamler.Config.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.Serializable;

public class Configuration extends YamlConfig implements Serializable
{
	private static final transient Location SPAWN_LOC = Bukkit.getWorlds().get(0).getSpawnLocation();

	@Comment("Location of player spawn (x:y:z:yaw:pitch)")
	private Location playerSpawn = SPAWN_LOC;

	@Comment("Location of mob spawn (x:y:z:yaw:pitch)")
	private Location spawn = SPAWN_LOC;

	@Comment("Location of mob objective (x:y:z:yaw:pitch)")
	private Location end = SPAWN_LOC;

	@Comment("The amount of gold nuggets you start with")
	private int startMoney = 150;

	@Comments({"The time (in seconds) between each wave, after the last mob of", "last wave spawned"})
	private int waveTime = 42;

	@Comment("The number of mobs that can reach the end point before you loose")
	private int lives = 10;

	@Comment("Location of towers seller (x:y:z:yaw:pitch)")
	private Location npcTowerLoc = SPAWN_LOC;

	@Comment("Location of tower upgrades seller (x:y:z:yaw:pitch)")
	private Location npcUpgradesLoc = SPAWN_LOC;

	@Comment("Location of exchanger (x:y:z:yaw:pitch)")
	private Location npcExchangeLoc = SPAWN_LOC;

	@Comment("Tower update rate, in ticks")
	private int towerUpdateRate = 20;

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

	public Location getPlayerSpawn()
	{
		return playerSpawn;
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

	public Location getSpawn()
	{
		return spawn;
	}

	public void setSpawn(Location spawn)
	{
		this.spawn = spawn;
		save();
	}

	public Location getEnd()
	{
		return end;
	}

	public void setEnd(Location end)
	{
		this.end = end;
		save();
	}

	public int getStartMoney()
	{
		return startMoney;
	}

	public int getWaveTime()
	{
		return waveTime;
	}

	public int getLives()
	{
		return lives;
	}

	public Location getNpcTowerLoc()
	{
		return npcTowerLoc;
	}

	public void setNpcTowerLoc(Location loc)
	{
		this.npcTowerLoc = loc;
		save();
	}

	public Location getNpcUpgradesLoc()
	{
		return npcUpgradesLoc;
	}

	public void setNpcUpgradesLoc(Location loc)
	{
		this.npcUpgradesLoc = loc;
		save();
	}

	public Location getNpcExchangeLoc()
	{
		return npcExchangeLoc;
	}

	public void setNpcExchangeLoc(Location loc)
	{
		this.npcExchangeLoc = loc;
		save();
	}

	public int getTowerUpdateRate()
	{
		return towerUpdateRate;
	}
}
