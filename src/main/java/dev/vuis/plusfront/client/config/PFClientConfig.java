package dev.vuis.plusfront.client.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PFClientConfig {
	private static final PFClientConfig INSTANCE;
	private static final ModConfigSpec SPEC;

	static {
		var pair = new ModConfigSpec.Builder().configure(PFClientConfig::new);
		INSTANCE = pair.getLeft();
		SPEC = pair.getRight();
	}

	public static void register(ModContainer container) {
		container.registerConfig(ModConfig.Type.CLIENT, SPEC);
	}

	private final ModConfigSpec.EnumValue<GameGuiStyle> gameGuiStyle;

	private PFClientConfig(ModConfigSpec.Builder builder) {
		gameGuiStyle = builder.defineEnum("game_gui_style", GameGuiStyle.MODERN);
	}

	public static GameGuiStyle getGameGuiStyle() {
		return INSTANCE.gameGuiStyle.get();
	}

	public static void setGameGuiStyle(GameGuiStyle style) {
		INSTANCE.gameGuiStyle.set(style);
	}
}
