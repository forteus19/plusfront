package dev.vuis.plusfront.ex;

import com.boehmod.blockfront.game.GameTypeCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.data.PFDefusalData;
import java.util.Optional;

// since GameTypeCodec is sealed, we have to hack custom data into tdm since it has nothing special
public interface TeamDeathmatchCodecEx {
	MapCodec<GameTypeCodec.TeamDeathmatch> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			GameTypeCodec.TeamDeathmatch.CODEC.forGetter(c -> c),
			PFDefusalData.CODEC.optionalFieldOf("defusal").forGetter(c -> cast(c).pf$getDefusalData()),
			Codec.BOOL.optionalFieldOf("chamber", false).forGetter(c -> cast(c).pf$isChamber())
		).apply(instance, (originalData, defusalData, isChamber) -> {
			TeamDeathmatchCodecEx ex = cast(originalData);
			ex.pf$setDefusalData(defusalData);
			ex.pf$setChamber(isChamber);
			return originalData;
		}));

	@SuppressWarnings("DataFlowIssue")
	static TeamDeathmatchCodecEx cast(GameTypeCodec.TeamDeathmatch codec) {
		return (TeamDeathmatchCodecEx) (Object) codec;
	}

	Optional<PFDefusalData> pf$getDefusalData();

	void pf$setDefusalData(Optional<PFDefusalData> data);

	boolean pf$isChamber();

	void pf$setChamber(boolean enabled);
}
