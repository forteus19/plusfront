package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.map.effect.FlyByMapEffect;
import com.boehmod.blockfront.map.effect.edit.AbstractPromptField;
import com.boehmod.blockfront.map.effect.edit.ButtonPromptField;
import com.boehmod.blockfront.map.effect.edit.SelectPromptField;
import com.boehmod.blockfront.registry.BFBlocks;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlyByMapEffect.class)
public abstract class FlyByMapEffectMixin {
	@Shadow
	@Final
	private @NotNull ObjectList<Supplier<Block>> blocks;

	@Definition(id = "add", method = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;add(Ljava/lang/Object;)Z")
	@Definition(id = "SelectPromptField", type = SelectPromptField.class)
	@Expression("?.add(new SelectPromptField(?, ?, ?, ?, ?, ?, ?))")
	@Redirect(
		method = "getEditableFields",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private boolean betterPlaneBlockSelection(ObjectArrayList<AbstractPromptField<?>> instance, Object k) {
		for (int i = 0; i < blocks.size(); i++) {
			final int index = i;

			instance.add(new SelectPromptField<>(
				Component.literal("Plane Block " + index),
				() -> blocks.get(index).get(),
				block -> blocks.set(index, () -> block),
				Component.literal("Select Plane Block " + index),
				() -> BuiltInRegistries.BLOCK.stream().toList(),
				block -> BuiltInRegistries.BLOCK.getKey(block).toString(),
				block -> Component.literal(BuiltInRegistries.BLOCK.getKey(block).toString())
			));

			instance.add(new ButtonPromptField(
				Component.literal("Remove Plane Block " + index),
				() -> blocks.remove(index)
			));
		}

		instance.add(new ButtonPromptField(
			Component.literal("Add Plane Block"),
			() -> {
				Block defaultBlock = BFBlocks.P51_MUSTANG.value();
				blocks.add(() -> defaultBlock);
			}
		));

		return true;
	}
}
