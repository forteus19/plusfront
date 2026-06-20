package dev.vuis.plusfront.ex;

import com.boehmod.blockfront.game.GameTypeCodec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.data.PFTroubleTownData;
import java.util.function.Function;

public interface TroubleTownCodecEx {
	MapCodec<GameTypeCodec.TroubleTown> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			GameTypeCodec.TroubleTown.CODEC.forGetter(Function.identity()),
			PFTroubleTownData.CODEC.fieldOf("pf").forGetter(codec -> cast(codec).pf$getCustomData())
		).apply(instance, (originalData, customData) -> {
			cast(originalData).pf$setCustomData(customData);
			return originalData;
		}));

	@SuppressWarnings("DataFlowIssue")
	static TroubleTownCodecEx cast(GameTypeCodec.TroubleTown codec) {
		return (TroubleTownCodecEx) (Object) codec;
	}

	PFTroubleTownData pf$getCustomData();

	void pf$setCustomData(PFTroubleTownData data);
}
