package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.IdleGameStage;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.compat.voicechat.PFVoicechat;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IdleGameStage.class)
public abstract class IdleGameStageMixin<G extends AbstractGame<G, P, ?>, P extends AbstractGamePlayerManager<G>> extends AbstractGameStage<G, P> {
	@Override
	public void onStageEnd(@NotNull GameStageContext<G, P> context) {
		if (PlusFront.voicechatLoaded) {
			PFVoicechat.getInstance().onGameStart(context.game().getUUID());
		}
	}

	@Override
	public void onStageStart(@NotNull GameStageContext<G, P> context) {
		if (PlusFront.voicechatLoaded) {
			PFVoicechat.getInstance().onGameEnd(context.game().getUUID());
		}
	}
}
