package dev.vuis.plusfront;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PlusFront.MOD_ID)
public final class PlusFront {
	public static final String MOD_ID = "pf";
	public static final Logger LOGGER = LogManager.getLogger("PlusFront");

	public static final Map<String, Boolean> FEATURE_FLAGS = Map.of(
		"server_shot_validation", false,
		"server_shot_validation_spread", false,
		"server_shot_validation_kick", false,
		"server_shot_validation_report", false,
		"server_match_feature_ping", true,
		"server_grenade_cook_drop_on_death", true,
		"client_fancy_bullet_effects", true,
		"client_veil_fancy_gun_light", true,
		"server_player_voice_sounds", false
	);

	public static boolean voicechatLoaded = false;

	public PlusFront() {
		PlusFront.LOGGER.info("PlusFront initialized!");
	}

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
