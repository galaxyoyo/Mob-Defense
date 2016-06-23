package fr.galaxyoyo.mobdefense.upgrades;

import fr.galaxyoyo.mobdefense.MobDefense;
import fr.galaxyoyo.mobdefense.towers.Tower;
import org.bukkit.event.EventException;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;


public final class UpgradeRegistration implements Serializable
{
	private transient Class<? extends Upgrade> clazz;
	private String className;
	private ItemStack item;
	private ItemStack[] cost;
	private Map<String, Object> parameters;

	@SuppressWarnings("unused")
	private UpgradeRegistration()
	{
	}

	public UpgradeRegistration(String className, ItemStack item, ItemStack[] cost, Map<String, Object> parameters)
	{
		this.className = className;
		this.item = item;
		this.cost = cost;
		this.parameters = parameters;
	}

	public boolean register()
	{
		String className = this.className;
		if (!className.contains("."))
			className = "fr.galaxyoyo.mobdefense.upgrades." + className;
		try
		{
			//noinspection unchecked
			clazz = (Class<? extends Upgrade>) Class.forName(className);
		}
		catch (ClassNotFoundException ex)
		{
			MobDefense.instance().getLogger().severe("Unable to find the upgrade class '" + className + "'. Please update config.");
			return false;
		}
		catch (ClassCastException ex)
		{
			MobDefense.instance().getLogger().severe("The class '" + className + "' was found but isn't an upgrade class. Please update config.");
			return false;
		}

		return true;
	}

	public <T extends Upgrade> T newInstance(Tower tower) throws EventException
	{
		try
		{
			//noinspection unchecked
			Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(UpgradeRegistration.class, Tower.class);
			constructor.setAccessible(true);
			T upgrade = constructor.newInstance(this, tower);
			upgrade.read(getParameters());
			return upgrade;
		}
		catch (Exception ex)
		{
			throw new EventException(ex);
		}
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public ItemStack getItem()
	{
		return item;
	}

	public ItemStack[] getCost()
	{
		return cost;
	}
}