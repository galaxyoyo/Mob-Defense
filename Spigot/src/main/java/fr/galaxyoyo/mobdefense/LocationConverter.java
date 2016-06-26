package fr.galaxyoyo.mobdefense;

import net.cubespace.Yamler.Config.Converter.Converter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.ParameterizedType;

public class LocationConverter implements Converter
{
	@Override
	public Object toConfig(Class<?> clazz, Object obj, ParameterizedType type) throws Exception
	{
		Location loc = (Location) obj;
		return loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
	}

	@Override
	public Object fromConfig(Class<?> clazz, Object obj, ParameterizedType type) throws Exception
	{
		try
		{
			String[] split = obj.toString().split(":");
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
			throw new InvalidConfigurationException(t);
		}
	}

	@Override
	public boolean supports(Class<?> type)
	{
		return Location.class.isAssignableFrom(type);
	}
}
