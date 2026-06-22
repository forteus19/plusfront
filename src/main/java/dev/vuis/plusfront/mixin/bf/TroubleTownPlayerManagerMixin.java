package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownGame;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownPlayerManager;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.compat.voicechat.PFVoicechat;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TroubleTownPlayerManager.class)
public abstract class TroubleTownPlayerManagerMixin extends AbstractGamePlayerManager<TroubleTownGame> {
	public TroubleTownPlayerManagerMixin(@NotNull TroubleTownGame game, @NotNull PlayerDataHandler<?> dataHandler) {
		super(game, dataHandler);
	}

	@Inject(
		method = "initPlayer",
		at = @At("TAIL")
	)
	private void voicechatGroupInit(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid,
		CallbackInfo ci
	) {
		if (PlusFront.voicechatLoaded) {
			PFVoicechat.getInstance().addToDeadGroup(game.getUUID(), player.getUUID());
		}
	}
}
