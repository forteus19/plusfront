package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.assets.impl.GameAsset;
import com.boehmod.blockfront.game.GameTypeCodec;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.ex.TeamDeathmatchCodecEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameAsset.class)
public abstract class GameAssetMixin {
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

			if (ex.pf$getDefusalData().isPresent()) {
				PlusFront.LOGGER.info("Overriding TDM key with DEF.");

				key = "def";
			}
		}

		return key;
	}
}
