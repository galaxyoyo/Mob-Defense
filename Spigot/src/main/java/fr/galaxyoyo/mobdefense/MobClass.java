package fr.galaxyoyo.mobdefense;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

public class MobClass implements Serializable
{
	private String name;
	private String displayName;
	private int hp;
	private double speed;
	private EntityType type;
	private ItemStack[] inv;
	private int loot;

	@SuppressWarnings("unused")
	private MobClass()
	{
	}

	public MobClass(String name, String displayName, int hp, float speed, EntityType type, ItemStack[] inv, int loot)
	{
		this.name = name;
		this.displayName = displayName;
		this.hp = hp;
		this.speed = speed;
		this.type = type;
		this.inv = inv;
		this.loot = loot;
	}

	public String getName()
	{
		return name;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int getHP()
	{
		return hp;
	}

	public double getSpeed()
	{
		return speed;
	}

	public EntityType getType()
	{
		return type;
	}

	public ItemStack[] getInv()
	{
		return inv;
	}

	public int getLoot()
	{
		return loot;
	}
}
