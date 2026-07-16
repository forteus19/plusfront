package dev.vuis.plusfront.mapeffect;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.map.MapEnvironment;
import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.map.effect.edit.AbstractPromptField;
import com.boehmod.blockfront.map.effect.edit.BoundedFloatPromptField;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.ex.MapEnvironmentEx;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class BrightnessMapEffect extends AbstractMapEffect {
	public static final String ID = "pf_brightness";
	public static final MapCodec<BrightnessMapEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			Codec.floatRange(0f, 1f).fieldOf("brightness").forGetter(BrightnessMapEffect::getBrightness)
		).apply(
			instance, BrightnessMapEffect::new
		));

	private static final ResourceLocation DEBUG_TEXTURE = BFRes.loc("textures/misc/debug/mapeffect/sky_settings.png");

	@Getter
	@Setter
	private float brightness;

	@SuppressWarnings("unused")
	public BrightnessMapEffect() {
		this(0.5f);
	}

	@Override
	public void onInitEnvironment(@NotNull Minecraft minecraft, @NotNull MapEnvironment environment) {
		super.onInitEnvironment(minecraft, environment);

		MapEnvironmentEx ex = (MapEnvironmentEx) environment;
		ex.pf$setBrightness(brightness);
	}

	@Override
	public @NotNull ResourceLocation getDebugTexture() {
		return DEBUG_TEXTURE;
	}

	@Override
	public @NotNull List<AbstractPromptField<?>> getEditableFields() {
		List<AbstractPromptField<?>> fields = super.getEditableFields();

		fields.add(new BoundedFloatPromptField(
			Component.literal("Brightness"),
			this::getBrightness, this::setBrightness,
			0f, 1f
		));

		return fields;
	}

	@Override
	public void getInfoLines(@NotNull Minecraft minecraft, @NotNull BFClientManager manager, @NotNull LocalPlayer player, @NotNull ClientLevel level, @NotNull List<Component> lineList) {
		super.getInfoLines(minecraft, manager, player, level, lineList);

		lineList.add(Component.literal(String.format("Brightness: %.2f", brightness)));
	}

	@Override
	public void updateServer(
		@NotNull ServerLevel level,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull AbstractGame<?, ?, ?> game,
		@NotNull RandomSource random,
		@NotNull Set<UUID> players
	) {
	}

	@Override
	public void updateClient(
		@NotNull Minecraft minecraft,
		@NotNull BFClientManager manager,
		@NotNull RandomSource random,
		@NotNull AbstractGame<?, ?, ?> game,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level,
		float renderTime
	) {
	}
}
