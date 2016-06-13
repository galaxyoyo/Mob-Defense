package fr.galaxyoyo.mobdefense;

import javafx.util.StringConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationConverter extends StringConverter<Location>
{
	private static LocationConverter instance = new LocationConverter();

	private LocationConverter()
	{
	}

	public static LocationConverter instance()
	{
		return instance;
	}

	@Override
	public String toString(Location loc)
	{
		return loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
	}

	@Override
	public Location fromString(String string)
	{
		try
		{
			String[] split = string.split(":");
			if (split.length != 5)
			{
				throw new IllegalArgumentException("CONFIGURATION ERROR : Location must be provided as x:y:z:yaw:pitch, it seems that there are " + split.length + " arguments, " +
						"required 5");
			}

			return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[3]),
					Float.parseFloat(split[4]));
		}
		catch (Throwable t)
		{
			MobDefense.instance().getLogger().severe(t.getMessage());
			Bukkit.shutdown();
			return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		}
	}
}
