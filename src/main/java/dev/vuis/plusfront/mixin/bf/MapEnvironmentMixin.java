package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.map.MapEnvironment;
import dev.vuis.plusfront.ex.MapEnvironmentEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MapEnvironment.class)
public abstract class MapEnvironmentMixin implements MapEnvironmentEx {
	@Unique
	private float pf$brightness = MapEnvironmentEx.BRIGHTNESS_UNSET;

	@Override
	public float pf$getBrightness() {
		return pf$brightness;
	}

	@Override
	public void pf$setBrightness(float brightness) {
		pf$brightness = brightness;
	}
}
