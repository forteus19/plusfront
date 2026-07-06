package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownGame;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownPlayerManager;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownWaitingStage;
import dev.vuis.plusfront.PFTemp;
import dev.vuis.plusfront.compat.voicechat.PFVoicechat;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TroubleTownWaitingStage.class)
public abstract class TroubleTownWaitingStageMixin {
	@Inject(
		method = "onStageStart",
		at = @At("TAIL")
	)
	private void clearVoicechatGroupsOnStart(
		@NotNull GameStageContext<TroubleTownGame, TroubleTownPlayerManager> context,
		CallbackInfo ci
	) {
		if (PFTemp.voicechatLoaded) {
			PFVoicechat vc = PFVoicechat.getInstance();

			for (UUID playerUuid : context.players()) {
				vc.removeFromGroup(playerUuid);
			}
		}
	}
}
