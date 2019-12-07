package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * Data-driven key-value storage.
 * @param <T> The type of registered object to store a value for.
 * @param <V> The type of value to store.
 */
public interface Dict<T, V> {
	boolean contains(T entry);
	V get(T entry);
	Collection<T> keys();
	Map<T, V> values();
	T getRandom(Random random);
	Identifier getId();
	Tag<T> toTag();

	/**
	 * Parse a dict from JSON.
	 * @param replace Whether the dict should be cleared before this tag is applied.
	 * @param entries A JsonObject full of entries to deserialize. Keys will be IDs of either registered T or tags of T.
	 * @throws SyntaxError If an entry is malformed, give up loading the dict and throw.
	 */
	void fromJson(boolean replace, JsonObject entries) throws SyntaxError;

	JsonObject toJson();
}
