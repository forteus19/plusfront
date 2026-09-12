package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameSequence;
import com.boehmod.blockfront.game.GameTypeCodec;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.MapCodec;
import dev.vuis.plusfront.data.PFDefusalData;
import dev.vuis.plusfront.data.PFTroubleTownData;
import dev.vuis.plusfront.ex.TeamDeathmatchCodecEx;
import dev.vuis.plusfront.ex.TroubleTownCodecEx;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameTypeCodec.class)
public interface GameTypeCodecMixin {
	@Redirect(
		method = "<clinit>",
		at = @At(
			value = "FIELD",
			target = "Lcom/boehmod/blockfront/game/GameTypeCodec$TeamDeathmatch;CODEC:Lcom/mojang/serialization/MapCodec;",
			opcode = Opcodes.GETSTATIC
		)
	)
	private static @NotNull MapCodec<GameTypeCodec.TeamDeathmatch> redirectTeamDeathmatchCodec() {
		return TeamDeathmatchCodecEx.CODEC;
	}

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

	@Mixin(GameTypeCodec.TeamDeathmatch.class)
	abstract class TeamDeathmatchMixin implements TeamDeathmatchCodecEx {
		@Unique
		private Optional<PFDefusalData> pf$defusalData;
		@Unique
		private boolean pf$isChamber;

		@Inject(
			method = "<init>",
			at = @At("TAIL")
		)
		private void initCustom(Optional<GameSequence> outroSequence, CallbackInfo ci) {
			pf$defusalData = Optional.empty();
			pf$isChamber = false;
		}

		@Override
		public Optional<PFDefusalData> pf$getDefusalData() {
			return pf$defusalData;
		}

		@Override
		public void pf$setDefusalData(Optional<PFDefusalData> data) {
			pf$defusalData = data;
		}

		@Override
		public boolean pf$isChamber() {
			return pf$isChamber;
		}

		@Override
		public void pf$setChamber(boolean enabled) {
			pf$isChamber = enabled;
		}

		@ModifyReturnValue(
			method = "codec",
			at = @At("TAIL")
		)
		private MapCodec<GameTypeCodec.TeamDeathmatch> overrideCodec(MapCodec<GameTypeCodec.TeamDeathmatch> original) {
			return TeamDeathmatchCodecEx.CODEC;
		}
	}

	@Mixin(GameTypeCodec.TroubleTown.class)
	abstract class TroubleTownMixin implements TroubleTownCodecEx {
		@Unique
		private PFTroubleTownData pf$customData;

		@Override
		public PFTroubleTownData pf$getCustomData() {
			return pf$customData;
		}

		@Override
		public void pf$setCustomData(PFTroubleTownData data) {
			pf$customData = data;
		}

		@ModifyReturnValue(
			method = "codec",
			at = @At("TAIL")
		)
		private MapCodec<GameTypeCodec.TroubleTown> overrideCodec(MapCodec<GameTypeCodec.TroubleTown> original) {
			return TroubleTownCodecEx.CODEC;
		}
	}
}
