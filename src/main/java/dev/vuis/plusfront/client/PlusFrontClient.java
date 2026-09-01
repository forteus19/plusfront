package dev.vuis.plusfront.client;

import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.config.PFClientConfig;
import lombok.Getter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@SuppressWarnings("ClassCanBeRecord")
@Mod(
	value = PlusFront.MOD_ID,
	dist = Dist.CLIENT
)
public final class PlusFrontClient {
	private static PlusFrontClient instance = null;

	@Getter
	private final ModContainer container;

	public PlusFrontClient(ModContainer container) {
		instance = this;

		this.container = container;

		PlusFront.LOGGER.info("Registering client config...");
		PFClientConfig.register(container);

		PlusFront.LOGGER.info("Registering client extension points...");
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

		PlusFront.LOGGER.info("PlusFront client initialized!");
	}

	public static PlusFrontClient instance() {
		if (instance == null) {
			throw new IllegalStateException("PlusFront client not initialized");
		}
		return instance;
	}
}
