package dev.vuis.plusfront.data;

import com.boehmod.blockfront.game.TeamType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PFCodecs {
	public static final Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap(
		list -> Util.fixedSize(list, 2).map(values -> new Vec2(values.get(0), values.get(1))),
		vec -> List.of(vec.x, vec.y)
	);
	public static final Codec<TeamType> TEAM_TYPE = ResourceLocation.CODEC.comapFlatMap(
		rl -> nullableResult(TeamType.getByResourceLocation(rl), "Invalid team type"),
		TeamType::getResourceLocation
	);

	private PFCodecs() {
		throw new AssertionError();
	}

	private static <T> DataResult<T> nullableResult(@Nullable T result, @NotNull String message) {
		return result != null ? DataResult.success(result) : DataResult.error(() -> message);
	}
}
