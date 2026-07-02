package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.stat.BFPopupStat;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFPopupStat.class)
public abstract class BFPopupStatMixin {
	@Shadow
	@Final
	private @NotNull String message;

	@Redirect(
		method = "doPopup",
		at = @At(
			value = "FIELD",
			target = "Lcom/boehmod/blockfront/common/stat/BFPopupStat;message:Ljava/lang/String;",
			opcode = Opcodes.GETFIELD,
			ordinal = 0
		)
	)
	private String negativeAmountHandling(BFPopupStat instance, @Local(argsOnly = true) int amount, @Local MutableComponent amountComponent) {
		if (amount < 0) {
			amountComponent.withStyle(ChatFormatting.DARK_RED);
			return message + ".negative";
		} else {
			return message;
		}
	}
}
