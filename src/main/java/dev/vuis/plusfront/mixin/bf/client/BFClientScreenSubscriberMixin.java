package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.ac.BFClientAntiCheat;
import com.boehmod.blockfront.client.event.BFClientScreenSubscriber;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BFClientScreenSubscriber.class)
public abstract class BFClientScreenSubscriberMixin {
	@Definition(id = "TitleScreen", type = TitleScreen.class)
	@Expression("? instanceof TitleScreen")
	@ModifyExpressionValue(
		method = "onOpenScreen",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private static boolean preventLobbyScreenOverriding(boolean original) {
		if (original) {
			// no shenanigans (hopefully)
			BFClientAntiCheat.enabled = true;
		}
		return false;
	}
}
