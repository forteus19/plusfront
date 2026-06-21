package dev.vuis.plusfront;

import com.boehmod.blockfront.game.GameType;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PlusFront.MOD_ID)
public final class PlusFront {
	public static final String MOD_ID = "pf";
	public static final Logger LOGGER = LogUtils.getLogger();

	public PlusFront() {
		LOGGER.info("dom icon: {}", GameType.DOMINATION.getIconTexture());
	}

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
