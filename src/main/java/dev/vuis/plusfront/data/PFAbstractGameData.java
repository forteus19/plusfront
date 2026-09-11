package dev.vuis.plusfront.data;

import com.boehmod.blockfront.game.TeamType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record PFAbstractGameData(
	Optional<TeamType> alliesTeamOverride,
	Optional<TeamType> axisTeamOverride
) {
	public static final Codec<PFAbstractGameData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			PFCodecs.TEAM_TYPE.optionalFieldOf("alliesTeamOverride").forGetter(PFAbstractGameData::alliesTeamOverride),
			PFCodecs.TEAM_TYPE.optionalFieldOf("axisTeamOverride").forGetter(PFAbstractGameData::axisTeamOverride)
		).apply(
			instance, PFAbstractGameData::new
		));
}
