package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.game.GameUtils;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.util.PFUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameUtils.class)
public abstract class GameUtilsMixin {
	@Inject(
		method = "giveLoadout(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Lcom/boehmod/blockfront/common/match/Loadout;Z)V",
		at = @At("TAIL")
	)
	private static void addLoadoutGiveEvent(ServerLevel level, ServerPlayer player, Loadout loadout, boolean keepUnset, CallbackInfo ci) {
		if (PFUtil.getPlayerGame(player) instanceof DefusalGame defusalGame) {
			defusalGame.getPlayerManager().onGiveLoadout(player);
		}
	}
}
