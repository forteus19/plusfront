package dev.vuis.plusfront.ex;

import com.boehmod.blockfront.game.GameTypeCodec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.data.PFDefusalData;
import java.util.Optional;

// since GameTypeCodec is sealed, we have to hack the defusal data into tdm since its the most similar
public interface TeamDeathmatchCodecEx {
	MapCodec<GameTypeCodec.TeamDeathmatch> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			GameTypeCodec.TeamDeathmatch.CODEC.forGetter(c -> c),
			PFDefusalData.CODEC.optionalFieldOf("defusal").forGetter(c -> cast(c).pf$getDefusalData())
		).apply(instance, (originalData, defusalData) -> {
			cast(originalData).pf$setDefusalData(defusalData);
			return originalData;
		}));

	@SuppressWarnings("DataFlowIssue")
	static TeamDeathmatchCodecEx cast(GameTypeCodec.TeamDeathmatch codec) {
		return (TeamDeathmatchCodecEx) (Object) codec;
	}

	Optional<PFDefusalData> pf$getDefusalData();

	void pf$setDefusalData(Optional<PFDefusalData> data);
}
