package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.assets.impl.GameAssetCodec;
import com.boehmod.blockfront.game.GameTypeCodec;
import com.boehmod.blockfront.game.TeamCodec;
import com.boehmod.blockfront.util.math.BFPose;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.data.PFAbstractGameData;
import dev.vuis.plusfront.ex.GameAssetCodecEx;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameAssetCodec.class)
public abstract class GameAssetCodecMixin implements GameAssetCodecEx {
	@Unique
	private Optional<PFAbstractGameData> pf$customData;

	@Redirect(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;",
			ordinal = 0
		)
	)
	private static Codec<GameAssetCodec> injectCustomFields(
		Function<RecordCodecBuilder.Instance<GameAssetCodec>, ? extends App<RecordCodecBuilder.Mu<GameAssetCodec>, GameAssetCodec>> builder
	) {
		return RecordCodecBuilder.create(instance ->
			instance.group(
				RecordCodecBuilder.mapCodec(builder).forGetter(codec -> codec),
				PFAbstractGameData.CODEC.optionalFieldOf("pf").forGetter(codec -> ((GameAssetCodecEx) (Object) codec).pf$getCustomData())
			).apply(
				instance, (codec, pf) -> {
					((GameAssetCodecEx) (Object) codec).pf$setCustomData(pf);
					return codec;
				}
			));
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void initCustom(String gameName, String mapName, List<TeamCodec> teams, Optional<BFPose> lobby, GameTypeCodec gameModeData, boolean inRotation, CallbackInfo ci) {
		pf$customData = Optional.empty();
	}

	@Override
	public Optional<PFAbstractGameData> pf$getCustomData() {
		return pf$customData;
	}

	@Override
	public void pf$setCustomData(Optional<PFAbstractGameData> data) {
		pf$customData = data;
	}
}
