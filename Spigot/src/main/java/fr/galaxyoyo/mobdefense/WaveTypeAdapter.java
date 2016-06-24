package fr.galaxyoyo.mobdefense;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

public class WaveTypeAdapter extends TypeAdapter<Wave>
{
	@Override
	public void write(JsonWriter w, Wave wave) throws IOException
	{
		if (wave == null)
		{
			w.nullValue();
			return;
		}

		w.beginObject();
		w.name("number").value(wave.getNumber());
		w.name("spawns").beginArray();
		for (Map.Entry<MobClass, Double> spawn : wave.getSpawns().entrySet())
		{
			w.beginObject();
			w.name("class").value(spawn.getKey().getName());
			w.name("number").value(spawn.getValue().intValue());
			w.endObject();
		}
		w.endArray();
		w.endObject();
	}

	@Override
	public Wave read(JsonReader r) throws IOException
	{
		if (r.peek() == JsonToken.NULL)
			return null;
		r.beginObject();
		Wave wave = new Wave();
		while (r.peek() != JsonToken.END_OBJECT)
		{
			String name = r.nextName();
			if (name.equalsIgnoreCase("number"))
				wave.setNumber(r.nextInt());
			else if (name.equalsIgnoreCase("spawns"))
			{
				r.beginArray();
				while (r.peek() != JsonToken.END_ARRAY)
				{
					MobClass mobClass = null;
					int number = -1;
					r.beginObject();
					while (r.peek() != JsonToken.END_OBJECT)
					{
						name = r.nextName();
						if (name.equalsIgnoreCase("class"))
							mobClass = MobDefense.instance().getMobClass(r.nextString());
						else if (name.equalsIgnoreCase("number"))
							number = r.nextInt();
						else
						{
							MobDefense.instance().getLogger().warning("Unrecognized name: '" + name + "'");
							r.skipValue();
						}
					}
					r.endObject();
					wave.getSpawns().put(mobClass, (double) number);
				}
				r.endArray();
			}
		}
		r.endObject();
		return wave;
	}
}
