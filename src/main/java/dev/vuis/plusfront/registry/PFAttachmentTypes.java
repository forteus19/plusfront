package dev.vuis.plusfront.registry;

import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.player.PFArmory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PFAttachmentTypes {
	private static final DeferredRegister<AttachmentType<?>> DR = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PlusFront.MOD_ID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<PFArmory.Weapons>> ARMORY_WEAPONS = DR.register(
		"armory_weapons", () ->
			AttachmentType
				.builder(PFArmory.Weapons::new)
				.serialize(PFArmory.Weapons.CODEC, PFArmory.Weapons::hasData)
				.copyOnDeath()
				.build()
	);
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<PFArmory.Extra>> ARMORY_EXTRA = DR.register(
		"armory_extra", () ->
			AttachmentType
				.builder(PFArmory.Extra::new)
				.serialize(PFArmory.Extra.CODEC, PFArmory.Extra::hasData)
				.sync(PFArmory.Extra.STREAM_CODEC)
				.copyOnDeath()
				.build()
	);

	private PFAttachmentTypes() {
		throw new AssertionError();
	}

	public static void register(IEventBus modBus) {
		DR.register(modBus);
	}
}
