package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.assets.impl.GameAsset;
import com.boehmod.blockfront.assets.impl.GameAssetCodec;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameTypeCodec;
import com.boehmod.blockfront.game.TeamType;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.data.PFAbstractGameData;
import dev.vuis.plusfront.ex.AbstractGameEx;
import dev.vuis.plusfront.ex.GameAssetCodecEx;
import dev.vuis.plusfront.ex.TeamDeathmatchCodecEx;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameAsset.class)
public abstract class GameAssetMixin {
	@ModifyReturnValue(
		method = "getCodecData",
		at = @At("TAIL")
	)
	private GameAssetCodec setCustomFields(GameAssetCodec original, @Local AbstractGame<?, ?, ?> game) {
		GameAssetCodecEx codecEx = (GameAssetCodecEx) (Object) original;
		AbstractGameEx gameEx = (AbstractGameEx) game;

		TeamType alliesOverride = gameEx.pf$getAlliesTeamOverride();
		TeamType axisOverride = gameEx.pf$getAxisTeamOverride();

		if (alliesOverride == null && axisOverride == null) {
			return original;
		}

		codecEx.pf$setCustomData(Optional.of(new PFAbstractGameData(
			Optional.ofNullable(alliesOverride),
			Optional.ofNullable(axisOverride)
		)));

		return original;
	}

	@Redirect(
		method = "createFromCodecData",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameTypeCodec;key()Ljava/lang/String;",
			ordinal = 0
		)
	)
	private static String replaceKeyIfDefusal(GameTypeCodec codec) {
		String key = codec.key();

		if (codec instanceof GameTypeCodec.TeamDeathmatch tdmCodec) {
			TeamDeathmatchCodecEx ex = TeamDeathmatchCodecEx.cast(tdmCodec);

			boolean overwritten = false;

			if (ex.pf$getDefusalData().isPresent()) {
				PlusFront.LOGGER.info("Overriding TDM key with DEF.");
				key = "def";
				overwritten = true;
			}

			if (ex.pf$isChamber()) {
				if (!overwritten) {
					PlusFront.LOGGER.info("Overriding TDM key with OITC.");
					key = "oitc";
				} else {
					throw new RuntimeException("Defusal and chamber data present!");
				}
			}
		}

		return key;
	}

	@Inject(
		method = "createFromCodecData",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/AbstractGame;readCodecData(Lcom/boehmod/blockfront/game/GameTypeCodec;)V",
			ordinal = 0
		)
	)
	private static void handleCustomData(
		@NotNull GameAssetCodec data,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		CallbackInfoReturnable<GameAsset> cir,
		@Local AbstractGame<?, ?, ?> game
	) {
		Optional<PFAbstractGameData> customDataOptional = ((GameAssetCodecEx) (Object) data).pf$getCustomData();
		if (customDataOptional.isEmpty()) {
			return;
		}
		PFAbstractGameData customData = customDataOptional.get();

		AbstractGameEx gameEx = (AbstractGameEx) game;

		customData.alliesTeamOverride().ifPresent(gameEx::pf$setAlliesTeamOverride);
		customData.axisTeamOverride().ifPresent(gameEx::pf$setAxisTeamOverride);
	}
}
