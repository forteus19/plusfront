package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameUtils;
import dev.vuis.plusfront.PFTemp;
import dev.vuis.plusfront.util.PFUtil;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BFAbstractManager.class)
public abstract class BFAbstractManagerMixin {
	@Shadow
	@Final
	private @NotNull Map<UUID, AbstractGame<?, ?, ?>> playerGames;

	@Inject(
		method = "assignPlayerToGame",
		at = @At("TAIL")
	)
	private void autoJoinOthers(@NotNull ServerLevel level, @NotNull ServerPlayer player, @NotNull AbstractGame<?, ?, ?> game, CallbackInfoReturnable<Boolean> cir) {
		if (!player.getUUID().equals(PFTemp.autoJoinPlayer)) {
			return;
		}

		BFAbstractManager<?, ?, ?> self = (BFAbstractManager<?, ?, ?>) (Object) this;

		for (ServerPlayer otherPlayer : level.players()) {
			PFUtil.forceJoinGame(self, otherPlayer, game);
		}
	}

	@Inject(
		method = "clearPlayerGame",
		at = @At("HEAD"),
		cancellable = true
	)
	private void autoLeaveOthers(UUID uuid, CallbackInfo ci) {
		ci.cancel();

		AbstractGame<?, ?, ?> game = playerGames.remove(uuid);

		if (game != null && uuid.equals(PFTemp.autoJoinPlayer)) {
			BFAbstractManager<?, ?, ?> self = (BFAbstractManager<?, ?, ?>) (Object) this;

			AbstractGamePlayerManager<?> playerManager = game.getPlayerManager();

			for (UUID playerUuid : playerManager.getPlayers()) {
				ServerPlayer player = GameUtils.getPlayerByUUID(playerUuid);

				if (player != null) {
					playerManager.removePlayer(self, player.serverLevel(), player);
				}
			}
		}
	}
}
