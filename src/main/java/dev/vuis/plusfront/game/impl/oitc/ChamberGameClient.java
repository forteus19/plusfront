package dev.vuis.plusfront.game.impl.oitc;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.client.render.game.element.ClientGameElement;
import com.boehmod.blockfront.client.render.game.element.StatProgressGameElement;
import com.boehmod.blockfront.client.render.game.element.TimeGameElement;
import com.boehmod.blockfront.client.render.minimap.MinimapWaypoint;
import com.boehmod.blockfront.client.screen.match.summary.MatchSummaryScreen;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.unnamed.BF_552;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.config.GameGuiStyle;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.client.render.game.PFGameGuiRendering;
import dev.vuis.plusfront.game.PFGameClientHelper;
import dev.vuis.plusfront.game.ScoreboardFormats;
import dev.vuis.plusfront.util.PFUtil;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.jetbrains.annotations.NotNull;

public final class ChamberGameClient extends AbstractGameClient<ChamberGame, ChamberPlayerManager> {
	private static final ResourceLocation TOPELEMENT_KILLS_TEXTURE = BFRes.loc("textures/gui/game/topelement/kills.png");

	public ChamberGameClient(@NotNull BFClientManager manager, @NotNull ChamberGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);

		manager.getCinematics().method_2205(new BF_552(game));
	}

	@Override
	protected @NotNull List<Component> getTips() {
		return List.of();
	}

	@Override
	protected @NotNull List<ClientGameElement<ChamberGame, ChamberPlayerManager>> getGameElements() {
		return List.of(
			new StatProgressGameElement<>(TOPELEMENT_KILLS_TEXTURE, BFStats.KILLS),
			new TimeGameElement<>()
		);
	}

	@Override
	public boolean shouldRenderBackpack(@NotNull AbstractClientPlayer player) {
		return true;
	}

	@Override
	public void onRenderGui(
		@NotNull Minecraft minecraft,
		@NotNull BFClientManager manager,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level,
		@NotNull BFClientPlayerData playerData,
		@NotNull GuiGraphics graphics,
		@NotNull Font font,
		@NotNull PoseStack poseStack,
		@NotNull MultiBufferSource bufferSource,
		@NotNull Set<UUID> players,
		int width,
		int height,
		int midX,
		int midY,
		float renderTime,
		float delta
	) {
		if (PFClientConfig.getGameGuiStyle() == GameGuiStyle.OLD) {
			PFGameGuiRendering.oldTimer(
				graphics, poseStack, font,
				midX,
				getStageTimer()
			);
		}
	}

	@Override
	public boolean canChangePerspective(@NotNull Player player) {
		return PFGameClientHelper.canChangePerspective(game, player);
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return new Scoreboard()
			.column("PING", ScoreboardFormats.ping())
			.column("K", ScoreboardFormats.tagInt(BFStats.KILLS))
			.column("D", ScoreboardFormats.tagInt(BFStats.DEATHS));
	}

	@Override
	public boolean canOpenInventory() {
		return false;
	}

	@Override
	public boolean shouldRenderNameTag(
		@NotNull Minecraft minecraft,
		@NotNull LivingEntity target,
		@NotNull Player localPlayer,
		@NotNull ClientLevel level
	) {
		return false;
	}

	@Override
	public boolean canSwitchItem() {
		return true;
	}

	@Override
	public @NotNull List<MinimapWaypoint> getSpecificMinimapWaypoints(
		@NotNull Minecraft minecraft,
		@NotNull Set<UUID> players,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level
	) {
		return List.of();
	}

	@Override
	public void setNameTagState(
		@NotNull Minecraft minecraft,
		@NotNull RenderNameTagEvent event,
		@NotNull Player player,
		@NotNull ClientLevel level
	) {
		event.setCanRender(PFUtil.triState(player.hasLineOfSight(event.getEntity())));
	}

	@Override
	public @NotNull Collection<? extends MatchSummaryScreen> getSummaryScreens(boolean onlyVote) {
		return getSummaryScreens(game, onlyVote);
	}

	@Override
	protected @NotNull BFStat getTopPlayersStat() {
		return BFStats.KILLS;
	}
}
