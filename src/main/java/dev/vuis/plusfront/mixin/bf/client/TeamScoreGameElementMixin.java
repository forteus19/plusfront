package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.render.game.element.TeamScoreGameElement;
import com.boehmod.blockfront.game.GameTeam;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TeamScoreGameElement.class)
public abstract class TeamScoreGameElementMixin {
	@ModifyConstant(
		method = "update",
		constant = @Constant(
			intValue = 8271921,
			ordinal = 1
		)
	)
	private int fixAxisColor(int constant, @Local(ordinal = 0) GameTeam axisTeam) {
		TextColor color = axisTeam.getStyleText().getColor();
		return color != null ? color.getValue() : constant;
	}

	@ModifyConstant(
		method = "update",
		constant = @Constant(
			intValue = 8159560,
			ordinal = 1
		)
	)
	private int fixAlliesColor(int constant, @Local(ordinal = 1) GameTeam alliesTeam) {
		TextColor color = alliesTeam.getStyleText().getColor();
		return color != null ? color.getValue() : constant;
	}
}
