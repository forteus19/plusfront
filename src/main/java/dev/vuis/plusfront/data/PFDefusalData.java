package dev.vuis.plusfront.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.game.impl.def.BombSite;
import java.util.List;

public record PFDefusalData(
	List<BombSite> bombSites
) {
	public static final Codec<PFDefusalData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			BombSite.CODEC.listOf().fieldOf("bombSites").forGetter(PFDefusalData::bombSites)
		).apply(
			instance, PFDefusalData::new
		));
}
