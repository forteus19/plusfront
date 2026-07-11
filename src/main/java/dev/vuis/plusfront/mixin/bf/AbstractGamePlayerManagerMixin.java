package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import dev.vuis.plusfront.PFTemp;
import dev.vuis.plusfront.compat.voicechat.PFVoicechat;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
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
		method = "handlePlayerDeath",
		at = @At("TAIL")
	)
	private void voicechatHandlingOnDeath(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer killedPlayer,
		@NotNull UUID killedUuid,
		ServerPlayer sourcePlayer,
		UUID sourceUuid,
		@NotNull DamageSource source,
		@NotNull Set<UUID> players,
		CallbackInfo ci
	) {
		if (PFTemp.voicechatLoaded) {
			PFVoicechat.getInstance().addToDeadGroup(game.getUUID(), killedPlayer.getUUID());
		}
	}

	@Inject(
		method = "removePlayer(Lcom/boehmod/blockfront/common/BFAbstractManager;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;)V",
		at = @At("TAIL")
	)
	private void leaveGroupOnRemove(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		CallbackInfo ci
	) {
		if (PFTemp.voicechatLoaded) {
			PFVoicechat.getInstance().removeFromGroup(player.getUUID());
		}
	}

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
