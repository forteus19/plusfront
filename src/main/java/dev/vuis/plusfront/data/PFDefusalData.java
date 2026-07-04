package dev.vuis.plusfront.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.game.impl.def.BombSiteCodec;
import java.util.List;

public record PFDefusalData(
	List<BombSiteCodec> bombSites
) {
	public static final Codec<PFDefusalData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			BombSiteCodec.CODEC.listOf().fieldOf("bombSites").forGetter(PFDefusalData::bombSites)
		).apply(
			instance, PFDefusalData::new
		));
}
