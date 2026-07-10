package dev.vuis.plusfront.net.payload;

import com.boehmod.blockfront.client.BFClientManager;
import dev.vuis.plusfront.PlusFront;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public final class PFStopMusicPayload implements CustomPacketPayload {
	public static final Type<PFStopMusicPayload> TYPE = new Type<>(PlusFront.res("stop_music"));

	public static final PFStopMusicPayload INSTANCE = new PFStopMusicPayload();
	public static final StreamCodec<ByteBuf, PFStopMusicPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void register(PayloadRegistrar registrar) {
		registrar.playToClient(TYPE, STREAM_CODEC, PFStopMusicPayload::handleClient);
	}

	private void handleClient(IPayloadContext context) {
		BFClientManager manager = BFClientManager.getInstance();
		if (manager == null) {
			return;
		}

		PlusFront.LOGGER.info("Stopping current BF music.");

		manager.getMusicManager().stopCurrent();
	}
}
