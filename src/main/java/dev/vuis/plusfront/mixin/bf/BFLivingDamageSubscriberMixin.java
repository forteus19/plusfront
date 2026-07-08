package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.event.BFLivingDamageSubscriber;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.game.AbstractGame;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BFLivingDamageSubscriber.class)
public abstract class BFLivingDamageSubscriberMixin {
	@WrapOperation(
		method = "onLivingDamagePre",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/common/player/BFAbstractPlayerData;addPlayerDamage(Ljava/util/UUID;F)V",
			ordinal = 0
		)
	)
	private static void wrapAssistDamageOperation(
		BFAbstractPlayerData<?, ?, ?, ?> instance,
		UUID player,
		float damage,
		Operation<Void> original,
		@Local(ordinal = 1) Player targetPlayer,
		@Local(ordinal = 0) AbstractGame<?, ?, ?> game
	) {
		if (!(game instanceof DefusalGame defusalGame)) {
			original.call(instance, player, damage);
			return;
		}
		if (defusalGame.getPlayerManager().shouldAddAssistDamage(player, targetPlayer.getUUID())) {
			original.call(instance, player, damage);
		}
	}
}
