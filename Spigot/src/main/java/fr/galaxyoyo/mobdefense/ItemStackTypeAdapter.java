package fr.galaxyoyo.mobdefense;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ItemStackTypeAdapter extends TypeAdapter<ItemStack>
{
	@Override
	public void write(JsonWriter w, ItemStack stack) throws IOException
	{
		if (stack == null)
		{
			w.nullValue();
			return;
		}
		ItemMeta meta = stack.getItemMeta();
		w.beginObject();
		w.name("type");
		w.value(stack.getType().name().toLowerCase());
		if (stack.getAmount() != 1)
		{
			w.name("amount");
			w.value(stack.getAmount());
		}
		if (stack.getDurability() != 0)
		{
			w.name("durability");
			w.value(stack.getDurability());
		}
		if (meta.getDisplayName() != null)
		{
			w.name("name");
			w.value(meta.getDisplayName());
		}
		if (meta.getLore() != null && !meta.getLore().isEmpty())
		{
			w.name("lore");
			w.beginArray();
			for (String lore : meta.getLore())
				w.value(lore);
			w.endArray();
		}
		if (meta.getEnchants() != null && !meta.getEnchants().isEmpty())
		{
			w.name("enchantments");
			w.beginObject();
			for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet())
			{
				w.name(entry.getKey().getName().toLowerCase());
				w.value(entry.getValue());
			}
			w.endObject();
		}
		if (meta.getItemFlags() != null && !meta.getItemFlags().isEmpty())
		{
			w.name("flags");
			w.beginArray();
			for (ItemFlag flag : meta.getItemFlags())
				w.value(flag.name().toLowerCase());
			w.endArray();
		}
		if (meta.spigot().isUnbreakable())
			w.name("unbreakable").value(true);
		if (meta instanceof PotionMeta)
		{
			PotionMeta potionMeta = (PotionMeta) meta;
			if (!potionMeta.getCustomEffects().isEmpty())
			{
				w.name("potionEffects");
				w.beginArray();
				for (PotionEffect effect : potionMeta.getCustomEffects())
				{
					w.beginObject();
					w.name("id");
					w.value(effect.getType().getName().toLowerCase());
					w.name("duration");
					w.value(effect.getDuration());
					w.name("amplifier");
					w.value(effect.getAmplifier());
					w.name("ambient");
					w.value(effect.isAmbient());
					w.name("particles");
					w.value(effect.hasParticles());
					w.name("color");
					w.value(effect.getColor().asRGB());
					w.endObject();
				}
				w.endArray();
			}
		}
		w.endObject();
	}

	@Override
	public ItemStack read(JsonReader r) throws IOException
	{
		if (r.peek() == JsonToken.NULL)
		{
			r.nextNull();
			return null;
		}

		Material type = null;
		int amount = 1;
		int durability = 0;
		String name = null;
		List<String> lore = Lists.newArrayList();
		Map<Enchantment, Integer> enchantements = Maps.newHashMap();
		List<ItemFlag> flags = Lists.newArrayList();
		boolean unbreakable = false;
		List<PotionEffect> effects = Lists.newArrayList();

		r.beginObject();
		while (r.peek() == JsonToken.NAME)
		{
			String n = r.nextName();
			switch (n)
			{
				case "type":
					type = Material.valueOf(r.nextString().toUpperCase());
					break;
				case "amount":
					amount = r.nextInt();
					break;
				case "durability":
					durability = r.nextInt();
					break;
				case "name":
					name = r.nextString();
					break;
				case "lore":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
						lore.add(ChatColor.RESET + r.nextString());
					r.endArray();
					break;
				case "enchantments":
					r.beginObject();
					while (r.peek() != JsonToken.END_OBJECT)
						enchantements.put(Enchantment.getByName(r.nextName().toUpperCase()), r.nextInt());
					r.endObject();
					break;
				case "flags":
					r.beginArray();
					while (r.peek() != JsonToken.END_ARRAY)
						flags.add(ItemFlag.valueOf(r.nextString().toUpperCase()));
					r.endArray();
					break;
				case "unbreakable":
					unbreakable = r.nextBoolean();
					break;
				case "potionEffects":
					r.beginArray();
					while (r.peek() == JsonToken.BEGIN_OBJECT)
					{
						r.beginObject();
						PotionEffectType effect = null;
						int duration = 0;
						int amplifier = 0;
						boolean ambient = true;
						boolean particles = true;
						Color color = null;
						switch (r.nextName())
						{
							case "id":
								effect = PotionEffectType.getByName(r.nextString().toUpperCase());
								break;
							case "duration":
								duration = r.nextInt();
								break;
							case "amplifier":
								amplifier = r.nextInt();
								break;
							case "ambient":
								ambient = r.nextBoolean();
								break;
							case "particles":
								particles = r.nextBoolean();
								break;
							case "color":
								color = Color.fromRGB(r.nextInt());
								break;
						}
						r.endObject();
						assert effect != null;
						//noinspection ConstantConditions
						effects.add(new PotionEffect(effect, duration, amplifier, ambient, particles, color));
					}
					r.endObject();
				default:
					System.err.println("Unknown property " + n + ".");
					break;
			}
		}
		r.endObject();

		assert type != null : "type is missing!";
		ItemStack stack = new ItemStack(type, amount, (short) durability);
		stack.addUnsafeEnchantments(enchantements);
		ItemMeta meta = stack.getItemMeta();
		//noinspection ConstantConditions
		if (name != null)
			meta.setDisplayName(name);
		meta.setLore(lore);
		meta.addItemFlags(flags.toArray(new ItemFlag[flags.size()]));
		//noinspection ConstantConditions
		meta.spigot().setUnbreakable(unbreakable);
		if (meta instanceof PotionMeta && !effects.isEmpty())
			((PotionMeta) meta).getCustomEffects().addAll(effects);
		stack.setItemMeta(meta);

		return stack;
	}
}
