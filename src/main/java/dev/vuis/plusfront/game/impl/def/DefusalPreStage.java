package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.PreGameStage;
import com.boehmod.blockfront.util.PacketUtils;
import dev.vuis.plusfront.net.payload.PFStopMusicPayload;
import org.jetbrains.annotations.NotNull;

public final class DefusalPreStage extends PreGameStage<DefusalGame, DefusalPlayerManager> {
	public DefusalPreStage() {
		super(20);
	}

	@Override
	public void onStageEnd(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		AbstractGame<?, ?, ?> game = context.game();

		GameUtils.discardMatchEntities(context.serverLevel(), game, context.playerHandler());
		PacketUtils.sendToGamePlayers(PFStopMusicPayload.INSTANCE, game);
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return new DefusalWaitingStage();
	}
}
