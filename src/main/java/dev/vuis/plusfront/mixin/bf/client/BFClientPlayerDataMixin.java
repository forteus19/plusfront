package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.game.ClassType;
import com.boehmod.blockfront.game.TeamType;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.game.PFGameClientHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFClientPlayerData.class)
public abstract class BFClientPlayerDataMixin {
	@Redirect(
		method = "method_1149",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/ClassType;getKey()Ljava/lang/String;",
			ordinal = 1
		)
	)
	private String fixMatchClassKey(ClassType instance, @Local(ordinal = 0) TeamType teamType) {
		String originalKey = instance.getKey();
		if (originalKey.equals("specialist")) {
			return PFGameClientHelper.SPECIALIST_UNIFORMS.contains(teamType.getResourceLocation()) ? originalKey : "anti_tank";
		} else {
			return originalKey;
		}
	}
}
