package dev.vuis.plusfront.mixin.minecraft.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.game.AbstractGame;
import dev.vuis.plusfront.ex.MapEnvironmentEx;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
	@Redirect(
		method = "updateLightTexture",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/Double;floatValue()F",
			ordinal = 1
		)
	)
	private float overrideGamma(Double instance) {
		float original = instance.floatValue();

		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return original;
		}

		AbstractGame<?, ?, ?> game = manager.getGame();
		if (game == null) {
			return original;
		}

		MapEnvironmentEx environmentEx = (MapEnvironmentEx) game.getMapEnvironment();
		float brightness = environmentEx.pf$getBrightness();

		return brightness != MapEnvironmentEx.BRIGHTNESS_UNSET ? brightness : original;
	}
}
