package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.GameBoundary;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a bomb site in a defusal game.
 *
 * @param boundary the boundary that makes up the plantable area
 * @param minY the minimum plantable Y position
 * @param maxY the maximum plantable Y position
 * @param visibleY the bottom Y position the boundary will be rendered at
 * @param waypoints a list of positions to show visible waypoints
 * @param name the name of the bomb site
 *
 * @see DefusalGame
 */
public record BombSiteCodec(
	String name,
	GameBoundary boundary,
	double minY,
	double maxY,
	double visibleY,
	List<Vec3> waypoints
) {
	/**
	 * Codec used for serialization.
	 */
	public static final Codec<BombSiteCodec> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.sizeLimitedString(BombSite.NAME_MAX_LENGTH).fieldOf("name").forGetter(BombSiteCodec::name),
			GameBoundary.CODEC.fieldOf("boundary").forGetter(BombSiteCodec::boundary),
			Codec.DOUBLE.fieldOf("minY").forGetter(BombSiteCodec::minY),
			Codec.DOUBLE.fieldOf("maxY").forGetter(BombSiteCodec::maxY),
			Codec.DOUBLE.optionalFieldOf("visibleY", 0.0).forGetter(BombSiteCodec::visibleY),
			Vec3.CODEC.listOf().fieldOf("waypoints").forGetter(BombSiteCodec::waypoints)
		).apply(
			instance, BombSiteCodec::new
		));
}
