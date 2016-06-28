package fr.galaxyoyo.mobdefense;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import static fr.galaxyoyo.spigot.nbtapi.ReflectionUtils.invokeBukkitMethod;
import static fr.galaxyoyo.spigot.nbtapi.ReflectionUtils.invokeNMSMethod;

public class NMSUtils
{
	private static ServerVersion VERSION;

	static
	{
		String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
		try
		{
			VERSION = ServerVersion.valueOf(version);
		}
		catch (IllegalArgumentException ex)
		{
			throw new UnsupportedClassVersionError(version);
		}
	}

	public static void setEntityYaw(Entity e, float yaw)
	{
		yaw %= 360.0F;
		Object handle = invokeBukkitMethod("getHandle", e);
		if (VERSION.isAfter1_9())
		{
			invokeNMSMethod("EntityLiving", "h", handle, new Class[]{float.class}, yaw);
			invokeNMSMethod("EntityLiving", "i", handle, new Class[]{float.class}, yaw);
		}
		else
			invokeNMSMethod("EntityLiving", "f", handle, new Class[]{float.class}, yaw);
	}

	public static ServerVersion getServerVersion()
	{
		return VERSION;
	}

	public enum ServerVersion
	{
		v1_8_R1("1.8 -> 1.8.2"),
		v1_8_R2("1.8.3 -> 1.8.5"),
		v1_8_R3("1.8.6 -> 1.8.9"),
		v1_9_R1("1.9 -> 1.9.2"),
		v1_9_R2("1.9.4"),
		v1_10_R1("1.10 -> 1.10.2");

		private String name;

		ServerVersion(String name)
		{
			this.name = name;
		}

		public String getServerName()
		{
			return name;
		}

		public boolean isBefore1_9()
		{
			return !isAfter1_9();
		}

		public boolean isAfter1_9()
		{
			return compareTo(v1_9_R1) >= 0;
		}

		public boolean isAfter1_10()
		{
			return compareTo(v1_10_R1) >= 0;
		}
	}
}
