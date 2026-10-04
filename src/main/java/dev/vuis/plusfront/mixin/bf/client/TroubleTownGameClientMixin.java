package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownGame;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownGameClient;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownPlayerManager;
import dev.vuis.plusfront.game.PFGameClientHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TroubleTownGameClient.class)
public abstract class TroubleTownGameClientMixin extends AbstractGameClient<TroubleTownGame, TroubleTownPlayerManager> {
	public TroubleTownGameClientMixin(
		@NotNull BFClientManager manager,
		@NotNull TroubleTownGame game,
		@NotNull ClientPlayerDataHandler dataHandler
	) {
		super(manager, game, dataHandler);
	}

	@Redirect(
		method = "renderPlayerInfo",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z",
			ordinal = 0
		)
	)
	private boolean handleInfoDistance(LocalPlayer localPlayer, Entity target) {
		return PFGameClientHelper.isInfoVisible(game, localPlayer, target);
	}

	@Redirect(
		method = "renderWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z",
			ordinal = 0
		)
	)
	private boolean handleRoleDistance(LocalPlayer localPlayer, Entity target) {
		return PFGameClientHelper.isInfoVisible(game, localPlayer, target);
	}
}
