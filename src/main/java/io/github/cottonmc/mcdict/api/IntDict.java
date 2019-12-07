package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class IntDict<T> implements Dict<T, Integer> {
	private final Identifier id;
	private Registry<T> registry;
	private final Object2IntMap<T> values;

	public IntDict(Identifier id, Registry<T> registry) {
		this.id = id;
		this.registry = registry;
		this.values = new Object2IntArrayMap<>();
	}

	@Override
	public boolean contains(T entry) {
		return values.containsKey(entry);
	}

	@Override
	public Collection<T> keys() {
		return values.keySet();
	}

	@Override
	public Map<T, Integer> values() {
		return values;
	}

	@Override
	public Integer get(T entry) {
		return values.getInt(entry);
	}

	@Override
	public T getRandom(Random random) {
		List<T> list = Lists.newArrayList(this.keys());
		return list.get(random.nextInt(list.size()));
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public Tag<T> toTag() {
		Identifier newId = new Identifier(id.getNamespace(), "dict/" + id.getPath());
		return new Tag<>(newId, Collections.singleton(new Tag.CollectionEntry<>(values.keySet())), false);
	}

	@Override
	public void fromJson(boolean replace, JsonObject entries) throws SyntaxError {
		if (replace) values.clear();
		for (String key : entries.keySet()) {
			T entry = registry.get(new Identifier(key));
			JsonElement value = entries.get(key);
			if (value instanceof JsonPrimitive) {
				Object val = ((JsonPrimitive)value).getValue();
				if (val instanceof Integer) values.put(entry, (int)val);
				else throw new SyntaxError("Int dict may only have values that are integer primitives!");
			}
			throw new SyntaxError("Int dict may only have values that are integer primitives!");
		}
	}

	@Override
	public JsonObject toJson() {
		return null;
	}
}
