package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.JsonElement;
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

public abstract class SimpleDict<T, V> implements Dict<T, V> {
	private final Identifier id;
	private final Map<T, V> values;
	private Registry<T> registry;
	private Supplier<TagContainer<T>> container;

	public SimpleDict(Identifier id, Registry<T> registry, Supplier<TagContainer<T>> container) {
		this.id = id;
		this.values = new HashMap<>();
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
		Identifier newId = new Identifier(id.getNamespace(), "dict/" + id.getPath());
		return new Tag<>(newId, Collections.singleton(new Tag.CollectionEntry<>(values.keySet())), false);
	}

	@Override
	public void fromJson(boolean replace, JsonObject entries) throws SyntaxError {
		if (replace) values.clear();
		for (String key : entries.keySet()) {
			JsonElement value = entries.get(key);
			if (key.indexOf('#') == 0) {
				Tag<T> tag = container.get().get(new Identifier(key.substring(1)));
				if (tag == null) throw new SyntaxError("Dict references tag " + key + " that does not exist");
				V val = readValue(value);
				for (T t : tag.values()) {
					values.put(t, val);
				}
			}
			Optional<T> entry = registry.getOrEmpty(new Identifier(key));
			if (!entry.isPresent()) throw new SyntaxError("Dict references registered object " + key + " that does not exist");
			values.put(entry.get(), readValue(value));
		}
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("replace", new JsonPrimitive(false));
		JsonObject vals = new JsonObject();
		for (T t : values.keySet()) {
			vals.put(registry.getId(t).toString(), writeValue(values.get(t)));
		}
		json.put("values", vals);
		return json;
	}

	abstract V readValue(JsonElement json) throws SyntaxError;
	abstract JsonElement writeValue(V v);
}
