package fr.galaxyoyo.mobdefense.towers;

import fr.galaxyoyo.mobdefense.MobDefense;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventException;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

public final class TowerRegistration implements Serializable
{
	private transient Class<? extends Tower> clazz;
	private String className;
	private String displayName;
	private List<String> lore;
	private ItemStack[] cost;
	private Material material;

	@SuppressWarnings("unused")
	private TowerRegistration()
	{
	}

	public TowerRegistration(String className, String displayName, List<String> lore, ItemStack[] cost, Material material)
	{
		this.className = className;
		this.displayName = displayName;
		this.lore = lore;
		this.cost = cost;
		this.material = material;
	}

	public boolean register()
	{
		String className = this.className;
		if (!className.contains("."))
			className = "fr.galaxyoyo.mobdefense.towers." + className;
		try
		{
			//noinspection unchecked
			clazz = (Class<? extends Tower>) Class.forName(className);
		}
		catch (ClassNotFoundException ex)
		{
			MobDefense.instance().getLogger().severe("Unable to find the tower class '" + className + "'. Please update config.");
			return false;
		}
		catch (ClassCastException ex)
		{
			MobDefense.instance().getLogger().severe("The class '" + className + "' was found but isn't a tower class. Please update config.");
			return false;
		}

		return true;
	}

	protected <T extends Tower> T newInstance(Location loc) throws EventException
	{
		try
		{
			//noinspection unchecked
			Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(TowerRegistration.class, Location.class);
			constructor.setAccessible(true);
			return constructor.newInstance(this, loc);
		}
		catch (Exception ex)
		{
			throw new EventException(ex);
		}
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public List<String> getLore()
	{
		return lore.stream().map(line -> ChatColor.RESET + line).collect(Collectors.toList());
	}

	public ItemStack[] getCost()
	{
		return cost;
	}

	public Material getMaterial()
	{
		return material;
	}
}
