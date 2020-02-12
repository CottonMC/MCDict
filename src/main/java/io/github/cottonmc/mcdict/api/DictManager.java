package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.Jankson;
import io.github.cottonmc.mcdict.MCDict;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DictManager {
	public static final DictManager INSTANCE = new DictManager();

	public Map<Class<?>, Map<Identifier, Dict<?, ?>>> DICTS = new HashMap<>();
	public Map<Class<?>, DictInfo<?>> DICT_TYPES = new HashMap<>();
	public List<Function<Jankson.Builder, Jankson.Builder>> JKSON_FACTORY = new ArrayList<>();

	/**
	 * Register a new type of dict. based on a class of registered object.
	 * @param type The class entries in this dict type will be.
	 * @param registry The registry which entries in this dict type are registered to.
	 * @param tagContainer The tag container which tags for entries in this dict type are stored in.
	 * @param <T> The type of registered object this dict will support.
	 */
	public <T> void registerDictType(Class<T> type, Registry<T> registry, Supplier<TagContainer<T>> tagContainer) {
		DICTS.put(type, new HashMap<>());
		DICT_TYPES.put(type, new DictInfo<>(registry, tagContainer));
	}

	/**
	 * Add custom type serializers and deserializers for dicts to use.
	 * @param factory A function that takes the passed Jankson builder, adds your serializers and deserializers, and returns the same Jankson builder.
	 */
	public void appendValueType(Function<Jankson.Builder, Jankson.Builder> factory) {
		JKSON_FACTORY.add(factory);
	}

	/**
	 * Register a dict.
	 * @param id The ID of this dict.
	 * @param type The class of registered object this dict is for.
	 * @param valueType The class of value this dict will store.
	 * @param <T> The type of registered object this dict is for.
	 * @param <V> The type of value this dict will store.
	 * @return The successfully-registered dict, or null.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T, V> Dict<T, V> registerDict(Identifier id, Class<T> type, Class<V> valueType) {
		if (DICT_TYPES.containsKey(type)) {
			DictInfo<T> info = (DictInfo<T>) DICT_TYPES.get(type);
			//TODO: more fastutil-like dict types? is it even worth it? It gets cast right back to a Dict anyway...
			if (DICTS.get(type).containsKey(id)) {
				MCDict.logger.error("[MCDict] Could not register dict {}, as it already exists for dict type {}", id.toString(), type.getName());
				return null;
			}
			if (valueType == Integer.class) {
				IntDict<T> ret = new IntDict<>(id, info.registry, info.container);
				DICTS.get(type).put(id, ret);
				return (Dict<T, V>) ret;
			} else {
				SimpleDict<T, V> ret = new SimpleDict<>(id, valueType, info.registry, info.container);
				DICTS.get(type).put(id, ret);
				return ret;
			}
		} else {
			MCDict.logger.error("[MCDict] Could not register dict {}, as class {} does not have a dict type", id.toString(), type.getName());
			return null;
		}
	}

	/**
	 * Get a dict.
	 * @param type The class of registered object to get a dict for.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <T> The type of registered object to get a dict for.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The dict of the specified ID, type, and value type, or null if it doesn't exist.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T, V> Dict<T, V> getDict(Class<T> type, Class<V> valueType, Identifier id) {
		if (!DICTS.containsKey(type)) {
			return null;
		}
		Map<Identifier, Dict<?, ?>> dictMap = DICTS.get(type);
		Dict<?, ?> ret = dictMap.get(id);
		if (ret.getType() == valueType) return (Dict<T, V>) ret;
		else return null;
	}

	/**
	 * Get a block dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The block dict of the specified ID and value type, or null if it doesn't exist.
	 */
	@Nullable
	public <V> Dict<Block, V> getBlockDict(Class<V> valueType, Identifier id) {
		return getDict(Block.class, valueType, id);
	}

	/**
	 * Get a item dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The item dict of the specified ID and value type, or null if it doesn't exist.
	 */
	@Nullable
	public <V> Dict<Item, V> getItemDict(Class<V> valueType, Identifier id) {
		return getDict(Item.class, valueType, id);
	}

	/**
	 * Get a fluid dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The fluid dict of the specified ID and value type, or null if it doesn't exist.
	 */
	public <V> Dict<Fluid, V> getFluidDict(Class<V> valueType, Identifier id) {
		return getDict(Fluid.class, valueType, id);
	}

	/**
	 * Get a entity dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The entity dict of the specified ID and value type, or null if it doesn't exist.
	 */
	public <V> Dict<Entity, V> getEntityDict(Class<V> valueType, Identifier id) {
		return getDict(Entity.class, valueType, id);
	}

	/**
	 * Internal helper class for storing the required registries and tag containers for dicts, since a Pair<Registry<T>, Supplier<TagContainer<T>>> is a very bad idea
	 * @param <T> The type of registered object this DictInfo is for.
	 */
	private static class DictInfo<T> {
		private Registry<T> registry;
		private Supplier<TagContainer<T>> container;

		private DictInfo(Registry<T> registry, Supplier<TagContainer<T>> container) {
			this.registry = registry;
			this.container = container;
		}
	}
}
