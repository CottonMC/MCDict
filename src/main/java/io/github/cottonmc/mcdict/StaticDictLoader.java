package io.github.cottonmc.mcdict;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.ImmutableSet;
import io.github.cottonmc.mcdict.api.Dict;
import io.github.cottonmc.mcdict.api.DictManager;
import io.github.cottonmc.staticdata.StaticDataItem;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class StaticDictLoader {
	private static final String DATA_TYPE = "dicts/";
	private static final String EXTENSION = ".json5";

	public static void load() {
		Jankson.Builder builder = Jankson.builder();
		for (Function<Jankson.Builder, Jankson.Builder> factory : DictManager.FACTORIES) {
			factory.apply(builder);
		}
		Jankson jankson = builder.build();
		for (String key : DictManager.DICT_TYPES.keySet()) {
			Map<Identifier, Dict<?, ?>> dicts = DictManager.STATIC_DATA.dicts.get(key);
			Set<StaticDataItem> data = getContentInDirectory("dicts/" + key);
			for (StaticDataItem item : data) {
				Identifier id = item.getIdentifier();
				Identifier newId = new Identifier(id.getNamespace(), id.getPath().substring(DATA_TYPE.length() + key.length() + 1, id.getPath().length() - EXTENSION.length()));
				if (!dicts.containsKey(newId)) {
					MCDict.logger.error("[MCDict] Tried to load dict " + newId.toString() + " that wasn't registered");
					continue;
				}
				try {
					Dict<?, ?> dict = dicts.get(newId);
					JsonObject json = jankson.load(item.createInputStream());
					boolean replace = json.getBoolean("replace", false);
					boolean override = json.getBoolean("override", false);
					JsonObject vals = json.getObject("values");
					try {
						dict.fromJson(replace, override, vals);
					} catch (SyntaxError e) {
						MCDict.logger.error("[MCDict] Failed to load {} dict {}: {}", key, id.toString(), e.getMessage());
					}
				} catch (IOException | SyntaxError e) {
					MCDict.logger.error("[MCDict] Failed to load file(s) for dict " + id.toString() + ": " + e.getMessage());
				}
			}
		}
	}

	//TODO: PR into Static Data
	public static ImmutableSet<StaticDataItem> getContentInDirectory(String dirname) {
		ImmutableSet.Builder<StaticDataItem> builder = ImmutableSet.builder();
		for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
			Path staticDataPath = container.getRootPath().resolve("static_data");
			if (Files.isDirectory(staticDataPath)) {
				Path datadir = staticDataPath.resolve(dirname);
				if (Files.isDirectory(datadir)) {
					try(Stream<Path> files = Files.walk(datadir)) {
						files.forEach((it)->{
							if (Files.isDirectory(it)) return;
							builder.add(new StaticDataItem(toIdentifier(container, it), it));
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		Path globalStaticDataFolder = FabricLoader.getInstance().getGameDir().resolve("static_data/");
		if (Files.isDirectory(globalStaticDataFolder)) {
			Path contentdir = globalStaticDataFolder.resolve("content/");
			if (Files.isDirectory(contentdir)) {
				try (Stream<Path> namespaces = Files.walk(contentdir, 1)) {
					namespaces.forEach((namespace) -> {
						if (namespace.equals(contentdir)) return;
						String name = namespace.toString().substring(namespace.toString().replaceAll("\\\\", "/").lastIndexOf('/') + 1);
						Path datadir = namespace.resolve(dirname);
						if (Files.isDirectory(datadir)) {
							try (Stream<Path> files = Files.walk(datadir)) {
								files.forEach((it) -> {
									if (Files.isDirectory(it)) return;
									builder.add(new StaticDataItem(new Identifier(name, getRelative(namespace, it)), it));
								});
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return builder.build();
	}

	private static String getRelative(Path parent, Path child) {
		return parent.toAbsolutePath().relativize(child)
				.toString()
				.replace(File.separatorChar, '/')
				.toLowerCase(Locale.ROOT)
				.replace(' ', '_')
				;
	}

	private static Identifier toIdentifier(ModContainer container, Path path) {
		String rel = getRelative(container.getRootPath(), path);
		if (rel.startsWith("static_data/")) { //Should always be true
			rel = rel.substring("static_data/".length());
		}
		return new Identifier(container.getMetadata().getId(), rel);
	}
}
