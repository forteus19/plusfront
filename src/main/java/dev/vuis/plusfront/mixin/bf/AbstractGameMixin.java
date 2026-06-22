package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.AbstractGame;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.compat.voicechat.PFVoicechat;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGame.class)
public abstract class AbstractGameMixin {
	@Shadow
	public abstract @NotNull UUID getUUID();

	@Inject(
		method = "reset",
		at = @At("TAIL")
	)
	private void handleVoicechat(ServerLevel level, CallbackInfo ci) {
		if (PlusFront.voicechatLoaded) {
			PFVoicechat.getInstance().onGameEnd(getUUID());
		}
	}
}
