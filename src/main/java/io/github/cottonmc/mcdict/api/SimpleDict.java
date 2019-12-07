package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.Lists;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.*;

public abstract class SimpleDict<T, V> implements Dict<T, V> {
	private final Identifier id;
	private final Map<T, V> values;

	public SimpleDict(Identifier id) {
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

	abstract V readValue(JsonElement json) throws SyntaxError;
	abstract JsonElement writeValue(V v);
}
