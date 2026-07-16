package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameCombatManager;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.game.tag.IConditionalCombatStats;
import java.util.Set;
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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameCombatManager.class)
public abstract class GameCombatManagerMixin {
	@Shadow
	@Final
	private @NotNull AbstractGame<?, ?, ?> game;

	@Shadow
	protected abstract void handlePlayerDeath(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerPlayer player,
		@Nullable Entity sourceEntity,
		@NotNull Set<UUID> players
	);

	@Shadow
	protected abstract void handlePlayerKill(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull DamageSource source,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid,
		@NotNull LivingEntity killedEntity,
		@NotNull Set<UUID> players
	);

	@Redirect(
		method = "onPlayerDeath",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameCombatManager;handlePlayerDeath(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/common/player/PlayerDataHandler;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Ljava/util/Set;)V",
			ordinal = 0
		)
	)
	private void skipDeathHandlerIfNotCounted(
		GameCombatManager<?> instance,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerPlayer player,
		@Nullable Entity sourceEntity,
		@NotNull Set<UUID> players,
		@Local DamageSource source
	) {
		if (!(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldCountDeath(player, source)) {
			handlePlayerDeath(manager, dataHandler, player, sourceEntity, players);
		}
	}

	@Redirect(
		method = "onPlayerDeath",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameCombatManager;handlePlayerKill(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/common/player/PlayerDataHandler;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/UUID;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/Set;)V",
			ordinal = 0
		)
	)
	private void skipKillHandlerIfNotCounted(
		GameCombatManager<?> instance,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull DamageSource source,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid,
		@NotNull LivingEntity killedEntity,
		@NotNull Set<UUID> players
	) {
		if (!(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldCountKill(player, killedEntity)) {
			handlePlayerKill(manager, dataHandler, source, player, uuid, killedEntity, players);
		}
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
	private boolean skipKillFeedEntryStatsIfNotCounted(
		boolean original,
		@Local(argsOnly = true) LivingEntity killedEntity,
		@Local(ordinal = 1) ServerPlayer sourcePlayer
	) {
		return original
			&& (!(game instanceof IConditionalCombatStats conditionalGame) || conditionalGame.shouldCountKill(sourcePlayer, killedEntity));
	}
}
