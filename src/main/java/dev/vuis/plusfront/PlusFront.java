package dev.vuis.plusfront;

import com.boehmod.blockfront.game.GameType;
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
		"server_match_feature_ping", true
	);

	public static boolean voicechatLoaded = false;

	public PlusFront() {
		LOGGER.info("dom icon: {}", GameType.DOMINATION.getIconTexture());
	}

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
