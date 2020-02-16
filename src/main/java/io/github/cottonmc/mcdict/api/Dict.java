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
 * TODO: Is having this an interface actually worth it? No way for third parties to add their own subclasses...
 */
public interface Dict<T, V> {
	/**
	 * Remove all current key/value pairs.
	 */
	void clear();

	/**
	 * @param entry The entry to check for.
	 * @return Whether this dict contains a value for this entry.
	 */
	boolean contains(T entry);

	/**
	 * @return The class of object used for this dict.
	 */
	Class<V> getType();

	/**
	 * @param entry The entry to get for.
	 * @return The value for this entry.
	 */
	V get(T entry);

	/**
	 * @return All the entries in this dict.
	 */
	Collection<T> keys();

	/**
	 * @return A map of all the entries and values in this dict.
	 */
	Map<T, V> values();

	/**
	 * @param random The Random instance to use when rolling for an entry.
	 * @return The entry picked randomly.
	 */
	T getRandom(Random random);

	/**
	 * @return The ID of this dict.
	 */
	Identifier getId();

	/**
	 * Convert the set of entries to a vanilla Tag. Not yet usable as an actual tag, TODO.
	 * @return A tag of type T containing all the entries in this dict.
	 */
	Tag<T> toTag();

	/**
	 * Parse a dict from JSON.
	 * @param replace Whether the dict should be cleared before this JSON is applied.
	 * @param override Whether this JSON should override existing dict values for the same entry.
	 * @param entries A JsonObject full of entries to deserialize. Keys will be IDs of either registered T or tags of T.
	 * @throws SyntaxError If an entry is malformed, give up loading the dict and throw.
	 */
	void fromJson(boolean replace, boolean override, JsonObject entries) throws SyntaxError;

	/**
	 * Serialize a dict to JSON.
	 * @return A JsonObject with two keys: one boolean "replace" with a value of false, and one object "values" containing ID to serialized V pairs.
	 */
	JsonObject toJson();
}
