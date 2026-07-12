package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.command.BFCoreCommand;
import com.boehmod.blockfront.game.AbstractGame;
import dev.vuis.plusfront.PFTemp;
import dev.vuis.plusfront.util.PFUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFCoreCommand.class)
public abstract class BFCoreCommandMixin {
	@Redirect(
		method = "runJoin",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/common/BFAbstractManager;assignPlayerToGame(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Lcom/boehmod/blockfront/game/AbstractGame;)Z",
			ordinal = 0
		)
	)
	private static boolean autoJoinOthers(
		BFAbstractManager<?, ?, ?> manager,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		@NotNull AbstractGame<?, ?, ?> game
	) {
		if (!player.getUUID().equals(PFTemp.autoJoinPlayer)) {
			return manager.assignPlayerToGame(level, player, game);
		}

		for (ServerPlayer levelPlayer : level.players()) {
			PFUtil.forceJoinGame(manager, levelPlayer, game);
		}
		return true;
	}
}
