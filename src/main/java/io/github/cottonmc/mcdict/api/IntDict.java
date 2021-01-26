package io.github.cottonmc.mcdict.api;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.function.Supplier;

public class IntDict<T> extends SimpleDict<T, Integer> {
	private final Object2IntMap<T> values;

	public IntDict(Identifier id, Registry<T> registry, Supplier<TagGroup<T>> group) {
		super(id, Integer.class, registry, group);
		this.values = new Object2IntArrayMap<>();
	}
	
	@Override
	protected void loadPendingTags() {
		if (this.pendingTags != null && !this.pendingTags.isEmpty()) {
			List<Map<Identifier, Integer>> list = new ArrayList<>();
			
			this.pendingTags.forEach((tags, override) -> {
				if (tags != null) {
					tags.forEach((tagId, value) -> {
						Tag<T> tag = group.get().getTag(tagId);
						if (tag != null) {
							for (T t : tag.values()) {
								if (!values.containsKey(t) || override) values.put(t, value.intValue());
							}
							list.add(tags);
						}
					});
				}
			});
			
			list.forEach(entry -> {
				this.pendingTags.remove(entry);
			});
		}
	}

	@Override
	public boolean contains(T entry) {
		this.loadPendingTags();
		return values.containsKey(entry);
	}

	@Override
	public Collection<T> keys() {
		this.loadPendingTags();
		return values.keySet();
	}

	@Override
	public Map<T, Integer> values() {
		this.loadPendingTags();
		return values;
	}

	@Override
	@Deprecated
	public Integer get(T entry) {
		return values.getInt(entry);
	}

	public int getInt(T entry) {
		this.loadPendingTags();
		return values.getInt(entry);
	}
}
