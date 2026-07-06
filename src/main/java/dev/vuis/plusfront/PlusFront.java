package dev.vuis.plusfront;

import dev.vuis.plusfront.registry.PFAttachmentTypes;
import dev.vuis.plusfront.server.config.PFServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PlusFront.MOD_ID)
public final class PlusFront {
	public static final String MOD_ID = "pf";
	public static final Logger LOGGER = LogManager.getLogger("PlusFront");

	public PlusFront(IEventBus modBus, ModContainer container) {
		LOGGER.info("Registering attachment types...");
		PFAttachmentTypes.register(modBus);

		LOGGER.info("Registering config...");
		PFServerConfig.register(container);

		LOGGER.info("PlusFront initialized!");
	}

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
