package dev.vuis.plusfront.registry;

import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.player.PFCustomArmory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PFAttachmentTypes {
	private static final DeferredRegister<AttachmentType<?>> DR = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PlusFront.MOD_ID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<PFCustomArmory>> ARMORY = DR.register(
		"armory", () ->
			AttachmentType
				.builder(PFCustomArmory::new)
				.serialize(PFCustomArmory.CODEC, PFCustomArmory::hasData)
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
