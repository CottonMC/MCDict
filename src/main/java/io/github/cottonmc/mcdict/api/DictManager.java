package io.github.cottonmc.mcdict.api;

import blue.endless.jankson.Jankson;
import io.github.cottonmc.mcdict.MCDict;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.*;
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
	public static final Map<String, DictInfo<?>> DICT_TYPES = new HashMap<>();
	public static final List<Function<Jankson.Builder, Jankson.Builder>> FACTORIES = new ArrayList<>();

	public static final DictManager DATA_PACK = new DictManager();
	public static final DictManager STATIC_DATA = new DictManager();

	public Map<String, Map<Identifier, Dict<?, ?>>> dicts = new HashMap<>();

	private DictManager() {
		innerRegisterDictType("blocks", Registry.BLOCK, BlockTags::getContainer);
		innerRegisterDictType("items", Registry.ITEM, ItemTags::getContainer);
		innerRegisterDictType("fluids", Registry.FLUID, FluidTags::getContainer);
		innerRegisterDictType("entity_types", Registry.ENTITY_TYPE, EntityTypeTags::getContainer);
	}

	/**
	 * Register a new type of dict, based on a class of registered object.
	 * @param subfolder The subfolder in the `dicts` folder this type will belong to.
	 * @param registry The registry which entries in this dict type are registered to.
	 * @param tagContainer The tag container which tags for entries in this dict type are stored in.
	 * @param <T> The type of registered object this dict will support.
	 */
	public static <T> void registerDictType(String subfolder, Registry<T> registry, Supplier<TagContainer<T>> tagContainer) {
		if (DICT_TYPES.containsKey(subfolder)) {
			MCDict.logger.error("[MCDict] Could not register dict type {} as it already exists", subfolder);
			return;
		}
		DATA_PACK.innerRegisterDictType(subfolder, registry, tagContainer);
		STATIC_DATA.innerRegisterDictType(subfolder, registry, tagContainer);
	}

	private <T> void innerRegisterDictType(String subfolder, Registry<T> registry, Supplier<TagContainer<T>> tagContainer) {
		dicts.put(subfolder, new HashMap<>());
		DICT_TYPES.putIfAbsent(subfolder, new DictInfo<>(registry, tagContainer));
	}

	/**
	 * Add custom type serializers and deserializers for dicts to use.
	 * @param factory A function that takes the passed Jankson builder, adds your serializers and deserializers, and returns the same Jankson builder.
	 */
	public static void addValueFactory(Function<Jankson.Builder, Jankson.Builder> factory) {
		FACTORIES.add(factory);
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
	public <T, V> Dict<T, V> registerDict(Identifier id, String type, Class<V> valueType) {
		if (DICT_TYPES.containsKey(type)) {
			DictInfo<T> info = (DictInfo<T>) DICT_TYPES.get(type);
			if (dicts.get(type).containsKey(id)) {
				MCDict.logger.error("[MCDict] Could not register dict {}, as it already exists for dict type {}", id.toString(), type);
				return null;
			}
			//TODO: more fastutil-like dict types? is it even worth it? It gets cast right back to a Dict anyway...
			if (valueType == Integer.class) {
				IntDict<T> ret = new IntDict<>(id, info.registry, info.container);
				dicts.get(type).put(id, ret);
				return (Dict<T, V>) ret;
			} else {
				SimpleDict<T, V> ret = new SimpleDict<>(id, valueType, info.registry, info.container);
				dicts.get(type).put(id, ret);
				return ret;
			}
		} else {
			MCDict.logger.error("[MCDict] Could not register dict {}, as class {} does not have a dict type", id.toString(), type);
			return null;
		}
	}

	public <V> Dict<Block, V> registerBlockDict(Identifier id, Class<V> valueType) {
		return registerDict(id, "blocks", valueType);
	}

	public <V> Dict<Item, V> registerItemDict(Identifier id, Class<V> valueType) {
		return registerDict(id, "items", valueType);
	}

	public <V> Dict<Fluid, V> registerFluidDict(Identifier id, Class<V> valueType) {
		return registerDict(id, "fluids", valueType);
	}

	public <V> Dict<EntityType<?>, V> registerEntityTypeDict(Identifier id, Class<V> valueType) {
		return registerDict(id, "entity_types", valueType);
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
	public <T, V> Dict<T, V> getDict(String type, Class<V> valueType, Identifier id) {
		if (!dicts.containsKey(type)) {
			return null;
		}
		Map<Identifier, Dict<?, ?>> dictMap = dicts.get(type);
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
		return getDict("blocks", valueType, id);
	}

	/**
	 * Get an item dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The item dict of the specified ID and value type, or null if it doesn't exist.
	 */
	@Nullable
	public <V> Dict<Item, V> getItemDict(Class<V> valueType, Identifier id) {
		return getDict("items", valueType, id);
	}

	/**
	 * Get a fluid dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The fluid dict of the specified ID and value type, or null if it doesn't exist.
	 */
	public <V> Dict<Fluid, V> getFluidDict(Class<V> valueType, Identifier id) {
		return getDict("fluids", valueType, id);
	}

	/**
	 * Get an entity type dict.
	 * @param valueType The class of value to get a dict for.
	 * @param id The ID of the dict to get.
	 * @param <V> The type of value stored in the dict you want.
	 * @return The entity type dict of the specified ID and value type, or null if it doesn't exist.
	 */
	public <V> Dict<EntityType<?>, V> getEntityTypeDict(Class<V> valueType, Identifier id) {
		return getDict("entity_types", valueType, id);
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
