package io.github.cottonmc.mcdict.api;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.function.Supplier;

public class IntDict<T> extends SimpleDict<T, Integer> {
	private final Object2IntMap<T> values;

	public IntDict(Identifier id, Registry<T> registry, Supplier<TagContainer<T>> container) {
		super(id, Integer.class, registry, container);
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
	@Deprecated
	public Integer get(T entry) {
		return values.getInt(entry);
	}

	public int getInt(T entry) {
		return values.getInt(entry);
	}
}
