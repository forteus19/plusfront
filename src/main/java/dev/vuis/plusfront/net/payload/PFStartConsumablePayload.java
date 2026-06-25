package dev.vuis.plusfront.net.payload;

import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.util.PFUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public final class PFStartConsumablePayload implements CustomPacketPayload {
	public static final Type<PFStartConsumablePayload> TYPE = new Type<>(PlusFront.res("start_consumable"));

	public static final PFStartConsumablePayload INSTANCE = new PFStartConsumablePayload();
	public static final StreamCodec<ByteBuf, PFStartConsumablePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private PFStartConsumablePayload() {
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void register(PayloadRegistrar registrar) {
		registrar.playToServer(TYPE, STREAM_CODEC, PFStartConsumablePayload::handleServer);
	}

	public static void sendToServer() {
		PacketDistributor.sendToServer(INSTANCE);
	}

	public static void handleServer(PFStartConsumablePayload payload, IPayloadContext context) {
		Player player = context.player();

		if (PFUtil.getPlayerGame(player) instanceof DefusalGame defusalGame) {
			defusalGame.getPlayerManager().onStartConsumable(player.level(), player, player.getMainHandItem());
		}
	}
}
