package dev.vuis.plusfront.mixin.minecraft.client;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
	@Invoker("flushIfUnmanaged")
	void invokeFlushIfUnmanaged();
}
