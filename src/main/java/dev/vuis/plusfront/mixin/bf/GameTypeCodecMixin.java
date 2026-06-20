package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameTypeCodec;
import com.mojang.serialization.MapCodec;
import dev.vuis.plusfront.data.PFTroubleTownData;
import dev.vuis.plusfront.ex.TroubleTownCodecEx;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameTypeCodec.class)
public interface GameTypeCodecMixin {
	@Redirect(
		method = "<clinit>",
		at = @At(
			value = "FIELD",
			target = "Lcom/boehmod/blockfront/game/GameTypeCodec$TroubleTown;CODEC:Lcom/mojang/serialization/MapCodec;",
			opcode = Opcodes.GETSTATIC
		)
	)
	private static @NotNull MapCodec<GameTypeCodec.TroubleTown> redirectTroubleTownCodec() {
		return TroubleTownCodecEx.CODEC;
	}

	@Mixin(GameTypeCodec.TroubleTown.class)
	abstract class TroubleTownMixin implements TroubleTownCodecEx {
		@Unique
		private PFTroubleTownData pf$customData = PFTroubleTownData.EMPTY;

		@Override
		public PFTroubleTownData pf$getCustomData() {
			return pf$customData;
		}

		@Override
		public void pf$setCustomData(PFTroubleTownData data) {
			pf$customData = data;
		}

		@Inject(
			method = "codec",
			at = @At("HEAD"),
			cancellable = true
		)
		private void overrideCodec(CallbackInfoReturnable<MapCodec<GameTypeCodec.TroubleTown>> cir) {
			cir.setReturnValue(TroubleTownCodecEx.CODEC);
		}
	}
}
