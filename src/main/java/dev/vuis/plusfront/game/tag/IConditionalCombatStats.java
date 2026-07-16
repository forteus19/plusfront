package dev.vuis.plusfront.game.tag;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface IConditionalCombatStats {
	default boolean shouldCountDeath(
		@NotNull ServerPlayer player,
		@NotNull DamageSource source
	) {
		return true;
	}

	default boolean shouldCountKill(
		@NotNull ServerPlayer player,
		@NotNull LivingEntity killedEntity
	) {
		return true;
	}
}
