package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameBoundary;
import com.boehmod.blockfront.map.effect.MapEffectCodecs;
import com.mojang.serialization.Codec;
import dev.vuis.plusfront.data.PFCodecs;
import dev.vuis.plusfront.ex.GameBoundaryEx;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameBoundary.class)
public abstract class GameBoundaryMixin implements GameBoundaryEx {
	@Shadow
	@Final
	private @NotNull ObjectList<Vec2> points;

	@Override
	public @NotNull ObjectList<Vec2> pf$getPoints() {
		return points;
	}

	@Redirect(
		method = "<clinit>",
		at = @At(
			value = "FIELD",
			target = "Lcom/boehmod/blockfront/map/effect/MapEffectCodecs;VEC2_CODEC:Lcom/mojang/serialization/Codec;",
			opcode = Opcodes.GETSTATIC,
			ordinal = 0
		)
	)
	private static @NotNull Codec<Vec2> vecEitherCodec() {
		return Codec.withAlternative(
			MapEffectCodecs.VEC2_CODEC,
			PFCodecs.VEC2
		);
	}
}
