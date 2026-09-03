package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameStageTimer;
import dev.vuis.plusfront.client.render.IconRenderer;
import dev.vuis.plusfront.ex.GameStageTimerEx;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameStageTimer.class)
public abstract class GameStageTimerMixin implements GameStageTimerEx {
	@Unique
	private IconRenderer pf$iconRenderer;

	@Override
	public @Nullable IconRenderer pf$getIconRenderer() {
		return pf$iconRenderer;
	}

	@Override
	public void pf$setIconRenderer(@Nullable IconRenderer renderer) {
		pf$iconRenderer = renderer;
	}
}
