package dev.vuis.plusfront.ex;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

public interface GameBoundaryEx {
	@NotNull ObjectList<Vec2> pf$getPoints();
}
