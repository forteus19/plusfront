package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGamePlayerManager.class)
public abstract class AbstractGamePlayerManagerMixin<G extends AbstractGame<G, ?, ?>> {
	@Shadow
	@Final
	@NotNull
	protected G game;

	@Inject(
		method = "tickSpectator",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lcom/boehmod/blockfront/common/player/BFAbstractPlayerData;method_8879()Lcom/boehmod/blockfront/util/math/BFPose;",
			ordinal = 0
		),
		cancellable = true
	)
	private void preventSpectatorPositionLock(@NotNull ServerPlayer player, @NotNull BFAbstractPlayerData<?, ?, ?, ?> playerData, CallbackInfo ci) {
		ci.cancel();
	}
}
