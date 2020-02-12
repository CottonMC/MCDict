package io.github.cottonmc.mcdict;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCDict implements ModInitializer {
	public static final String MODID = "mcdict";

	public static final Logger logger = LogManager.getLogger();


	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PackDictLoader());
	}
}
