package dev.vuis.plusfront.mapeffect;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.net.packet.BFSoundPositionPacket;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.map.effect.edit.AbstractPromptField;
import com.boehmod.blockfront.map.effect.edit.BoundedFloatPromptField;
import com.boehmod.blockfront.map.effect.edit.BoundedIntegerPromptField;
import com.boehmod.blockfront.map.effect.edit.SoundPromptField;
import com.boehmod.blockfront.util.PacketUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.PlusFront;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class PositionedRandomSoundMapEffect extends AbstractMapEffect {
	public static final String ID = "pf_positioned_random_sound";

	public static final MapCodec<PositionedRandomSoundMapEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("sound", SoundEvents.EMPTY).forGetter(PositionedRandomSoundMapEffect::getSound),
			Codec.FLOAT.fieldOf("volume").forGetter(PositionedRandomSoundMapEffect::getVolume),
			Codec.FLOAT.fieldOf("pitch").forGetter(PositionedRandomSoundMapEffect::getPitch),
			Codec.FLOAT.fieldOf("activationDistance").forGetter(PositionedRandomSoundMapEffect::getActivationDistance),
			Codec.FLOAT.fieldOf("chance").forGetter(PositionedRandomSoundMapEffect::getChance),
			Codec.INT.fieldOf("cooldown").forGetter(PositionedRandomSoundMapEffect::getCooldown),
			Codec.INT.fieldOf("minDelay").forGetter(PositionedRandomSoundMapEffect::getMinDelay),
			Codec.INT.fieldOf("maxDelay").forGetter(PositionedRandomSoundMapEffect::getMaxDelay)
		).apply(
			instance, PositionedRandomSoundMapEffect::new
		));

	private static final ResourceLocation DEBUG_TEXTURE = PlusFront.res("textures/misc/debug/mapeffect/sound_random.png");

	@Getter
	@Setter
	private @NotNull SoundEvent sound;
	@Getter
	@Setter
	private float volume;
	@Getter
	@Setter
	private float pitch;
	@Getter
	@Setter
	private float activationDistance;
	@Getter
	@Setter
	private float chance;
	@Getter
	@Setter
	private int cooldown;
	@Getter
	@Setter
	private int minDelay;
	@Getter
	@Setter
	private int maxDelay;

	private int cooldownTimer = 0;
	private int delayTimer = -1;

	@SuppressWarnings("unused")
	public PositionedRandomSoundMapEffect() {
		this(
			SoundEvents.EMPTY,
			1f,
			1f,
			16f,
			0.5f,
			100,
			0,
			0
		);
	}

	public PositionedRandomSoundMapEffect(
		@NotNull SoundEvent sound,
		float volume,
		float pitch,
		float activationDistance,
		float chance,
		int cooldown,
		int minDelay,
		int maxDelay
	) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.activationDistance = activationDistance;
		this.chance = chance;
		this.cooldown = cooldown;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
	}

	@Override
	public void updateServer(
		@NotNull ServerLevel level,
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull AbstractGame<?, ?, ?> game,
		@NotNull RandomSource random,
		@NotNull Set<UUID> players
	) {
		if (delayTimer >= 0) {
			if (delayTimer == 0) {
				play(game);
				cooldownTimer = cooldown;
			}

			delayTimer--;
			return;
		}

		if (cooldownTimer > 0) {
			cooldownTimer--;
			return;
		}

		boolean activate = false;

		float activationDistanceSqr = activationDistance * activationDistance;
		for (UUID playerUuid : players) {
			ServerPlayer player = GameUtils.getPlayerByUUID(playerUuid);

			if (player != null && player.distanceToSqr(position) <= activationDistanceSqr) {
				activate = true;
				break;
			}
		}

		if (!activate) {
			return;
		}

		if (random.nextFloat() > chance) {
			cooldownTimer = cooldown;
			return;
		}

		int delay = random.nextIntBetweenInclusive(minDelay, maxDelay);

		if (delay == 0) {
			play(game);
			cooldownTimer = cooldown;
		} else {
			delayTimer = delay - 1;
		}
	}

	private void play(@NotNull AbstractGame<?, ?, ?> game) {
		PacketUtils.sendToGamePlayers(
			new BFSoundPositionPacket(
				BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
				SoundSource.AMBIENT,
				volume,
				pitch,
				position
			),
			game
		);
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

	@Override
	public @NotNull ResourceLocation getDebugTexture() {
		return DEBUG_TEXTURE;
	}

	@Override
	public void getInfoLines(
		@NotNull Minecraft minecraft,
		@NotNull BFClientManager manager,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level,
		@NotNull List<Component> lineList
	) {
		super.getInfoLines(minecraft, manager, player, level, lineList);

		lineList.add(Component.literal("Sound: " + BuiltInRegistries.SOUND_EVENT.getKey(sound)));
		lineList.add(Component.literal(String.format("Volume: %.2f, Pitch: %.2f", volume, pitch)));
		lineList.add(Component.literal(String.format("Activation Distance: %.1f", activationDistance)));
		lineList.add(Component.literal("Chance: " + Math.round(chance * 100f) + "%"));
		lineList.add(Component.literal("Cooldown: " + cooldown));
		lineList.add(Component.literal("Min Delay: " + minDelay + ", Max Delay: " + maxDelay));
	}

	@Override
	public @NotNull List<AbstractPromptField<?>> getEditableFields() {
		List<AbstractPromptField<?>> fields = super.getEditableFields();

		fields.add(new SoundPromptField(
			Component.literal("Sound"),
			this::getSound, this::setSound
		));
		fields.add(new BoundedFloatPromptField(
			Component.literal("Volume"),
			this::getVolume, this::setVolume,
			0f, 2560f
		));
		fields.add(new BoundedFloatPromptField(
			Component.literal("Pitch"),
			this::getPitch, this::setPitch,
			0f, 2f
		));
		fields.add(new BoundedFloatPromptField(
			Component.literal("Activation Distance"),
			this::getActivationDistance, this::setActivationDistance,
			0f, 512f
		));
		fields.add(new BoundedFloatPromptField(
			Component.literal("Chance"),
			this::getChance, this::setChance,
			0f, 1f
		));
		fields.add(new BoundedIntegerPromptField(
			Component.literal("Cooldown"),
			this::getCooldown, this::setCooldown,
			0, Integer.MAX_VALUE
		));
		fields.add(new BoundedIntegerPromptField(
			Component.literal("Min Delay"),
			this::getMinDelay, this::setMinDelay,
			0, Integer.MAX_VALUE
		));
		fields.add(new BoundedIntegerPromptField(
			Component.literal("Max Delay"),
			this::getMaxDelay, this::setMaxDelay,
			0, Integer.MAX_VALUE
		));

		return fields;
	}
}
