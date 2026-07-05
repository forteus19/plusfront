package dev.vuis.plusfront.net.payload;

import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.util.PFUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public record PFFeatureFlagsPayload(Map<String, Boolean> featureFlags) implements CustomPacketPayload {
	public static final Type<PFFeatureFlagsPayload> TYPE = new Type<>(PlusFront.res("feature_flags"));

	public static final StreamCodec<ByteBuf, PFFeatureFlagsPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(Object2BooleanOpenHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.BOOL), PFFeatureFlagsPayload::featureFlags,
		PFFeatureFlagsPayload::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void register(PayloadRegistrar registrar) {
		registrar.playToClient(TYPE, STREAM_CODEC, PFFeatureFlagsPayload::handleClient);
	}

	private void handleClient(IPayloadContext context) {
		PlusFront.LOGGER.info("Setting {} feature flags from the server.", featureFlags.size());

		PFUtil.updateFeatureFlags(featureFlags);
	}
}
