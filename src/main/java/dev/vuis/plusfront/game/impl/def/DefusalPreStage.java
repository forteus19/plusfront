package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.PreGameStage;
import dev.vuis.plusfront.net.payload.PFStopMusicPayload;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class DefusalPreStage extends PreGameStage<DefusalGame, DefusalPlayerManager> {
	public DefusalPreStage() {
		super(20);
	}

	@Override
	public void onStageEnd(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		GameUtils.discardMatchEntities(context.serverLevel(), context.game(), context.playerHandler());

		for (UUID playerUuid : context.players()) {
			ServerPlayer player = GameUtils.getPlayerByUUID(playerUuid);

			if (player != null) {
				PFStopMusicPayload.sendToClient(player);
			}
		}
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return new DefusalWaitingStage();
	}
}
