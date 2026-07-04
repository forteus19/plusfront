package dev.vuis.plusfront.server.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PFServerConfig {
	public static final PFServerConfig INSTANCE;
	private static final ModConfigSpec SPEC;

	static {
		var pair = new ModConfigSpec.Builder().configure(PFServerConfig::new);
		INSTANCE = pair.getLeft();
		SPEC = pair.getRight();
	}

	public static void register(ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, SPEC);
	}

	private final ModConfigSpec.ConfigValue<String> bfApiHost;
	private final ModConfigSpec.BooleanValue autoFetchArmory;

	private PFServerConfig(ModConfigSpec.Builder builder) {
		bfApiHost = builder.define("bfapi_host", "https://blockfrontapi.vuis.dev");
		autoFetchArmory = builder.define("auto_fetch_armory", false);
	}

	public String getBfApiHost() {
		return bfApiHost.get();
	}

	public boolean getAutoFetchArmory() {
		return autoFetchArmory.get();
	}
}
