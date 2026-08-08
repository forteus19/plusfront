package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameCombatManager;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.game.tag.IConditionalCombatStats;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameCombatManager.class)
public abstract class GameCombatManagerMixin {
	@Shadow
	@Final
	private @NotNull AbstractGame<?, ?, ?> game;

	@WrapWithCondition(
		method = "onPlayerDeath",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameCombatManager;handlePlayerDeath(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/common/player/PlayerDataHandler;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;)V",
			ordinal = 0
		)
	)
	private boolean checkDeathConditional(
		GameCombatManager<?> instance,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerPlayer player,
		@Nullable Entity sourceEntity,
		@Local DamageSource source
	) {
		return !(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldCountDeath(player, source);
	}

	@WrapWithCondition(
		method = "onPlayerDeath",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameCombatManager;handlePlayerKill(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/common/player/PlayerDataHandler;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/UUID;Lnet/minecraft/world/entity/LivingEntity;)V",
			ordinal = 0
		)
	)
	private boolean checkKillConditional(
		GameCombatManager<?> instance,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull DamageSource source,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid,
		@NotNull LivingEntity killedEntity
	) {
		return !(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldCountKill(player, killedEntity);
	}

	@WrapWithCondition(
		method = "handlePlayerKill",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameUtils;changePlayerStat(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/game/AbstractGame;Ljava/util/UUID;Lcom/boehmod/blockfront/common/stat/BFStat;I)V",
			ordinal = 0
		)
	)
	private boolean checkLongDistanceShotReward(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull AbstractGame<?, ?, ?> game,
		@NotNull UUID player,
		@NotNull BFStat stat,
		int change
	) {
		return !(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldRewardLongDistanceShot();
	}

	@Definition(id = "entity", local = @Local(type = LivingEntity.class, ordinal = 0))
	@Definition(id = "ServerPlayer", type = ServerPlayer.class)
	@Expression("entity instanceof ServerPlayer")
	@ModifyExpressionValue(
		method = "addKillFeedEntry",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 1
		)
	)
	private boolean checkKillFeedEntryStats(
		boolean original,
		@Local(argsOnly = true) LivingEntity killedEntity,
		@Local(ordinal = 1) ServerPlayer sourcePlayer
	) {
		return original
			&& (!(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldCountKill(sourcePlayer, killedEntity));
	}
}
