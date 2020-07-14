package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.Lists;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.function.Supplier;

public class SimpleDict<T, V> implements Dict<T, V> {
	private final Identifier id;
	private final Class<V> type;
	private final Map<T, V> values;
	protected Registry<T> registry;
	protected Supplier<TagContainer<T>> container;

	public SimpleDict(Identifier id, Class<V> type, Registry<T> registry, Supplier<TagContainer<T>> container) {
		this.id = id;
		this.type = type;
		this.registry = registry;
		this.container = container;
		this.values = new HashMap<>();
	}

	@Override
	public void clear() {
		values().clear();
	}

	@Override
	public boolean contains(T entry) {
		return values.containsKey(entry);
	}

	@Override
	public Class<V> getType() {
		return type;
	}

	@Override
	public Collection<T> keys() {
		return values.keySet();
	}

	@Override
	public Map<T, V> values() {
		return values;
	}

	@Override
	public V get(T entry) {
		return values.get(entry);
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
		//Identifier newId = new Identifier(id.getNamespace(), "dict/" + id.getPath());
		return Tag.of(values().keySet());
	}

	//TODO: libcd condition support?
	@Override
	public void fromJson(boolean replace, boolean override, JsonObject entries) throws SyntaxError {
		Map<T, V> vals = values();
		if (replace) vals.clear();
		for (String key : entries.keySet()) {
			V value = entries.get(type, key);
			if (value == null) {
				throw new SyntaxError("Dict value for entry " + key + " could not be parsed into type " + type.getName());
			}
			if (key.indexOf('#') == 0) {
				Tag<T> tag = container.get().get(new Identifier(key.substring(1)));
				if (tag == null) throw new SyntaxError("Dict references tag " + key + " that does not exist");
				for (T t : tag.values()) {
					if (!vals.containsKey(t) || override) vals.put(t, value);
				}
			} else {
				Optional<T> entry = registry.getOrEmpty(new Identifier(key));
				if (!entry.isPresent())
					throw new SyntaxError("Dict references registered object " + key + " that does not exist");
				if (!vals.containsKey(entry.get()) || override) vals.put(entry.get(), value);
			}
		}
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("replace", new JsonPrimitive(false));
		JsonObject vals = new JsonObject();
		for (T t : values().keySet()) {
			vals.putDefault(registry.getId(t).toString(), values.get(t), type, null);
		}
		json.put("values", vals);
		return json;
	}

}
