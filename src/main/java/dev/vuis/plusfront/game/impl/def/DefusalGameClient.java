package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.client.render.game.element.ClientGameElement;
import com.boehmod.blockfront.client.render.game.element.TeamGameElement;
import com.boehmod.blockfront.client.render.game.element.TeamScoreGameElement;
import com.boehmod.blockfront.client.render.game.element.TimeGameElement;
import com.boehmod.blockfront.client.render.minimap.MinimapWaypoint;
import com.boehmod.blockfront.client.screen.match.summary.MatchSummaryScreen;
import com.boehmod.blockfront.client.settings.BFClientSettings;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.unnamed.BF_552;
import com.boehmod.blockfront.util.StringUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

public final class DefusalGameClient extends AbstractGameClient<DefusalGame, DefusalPlayerManager> {
	public DefusalGameClient(@NotNull BFClientManager manager, @NotNull DefusalGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);

		manager.getCinematics().method_2205(new BF_552(game));
	}

	@Override
	protected @NotNull List<Component> getTips() {
		return List.of();
	}

	@Override
	protected @NotNull List<ClientGameElement<DefusalGame, DefusalPlayerManager>> getGameElements() {
		return List.of(
			new TeamGameElement<>(),
			new TeamScoreGameElement<>(),
			new TimeGameElement<>()
		);
	}

	@Override
	public boolean shouldRenderBackpack(@NotNull AbstractClientPlayer player) {
		return true;
	}

	@Override
	public void render(
		@NotNull AbstractGamePlayerManager<?> playerManager,
		@NotNull Minecraft minecraft,
		@NotNull ClientLevel level,
		@NotNull LocalPlayer player,
		@NotNull RenderLevelStageEvent renderEvent,
		@NotNull MultiBufferSource.BufferSource bufferSource,
		@NotNull PoseStack poseStack,
		@NotNull Frustum frustum,
		@NotNull Font font,
		@NotNull GuiGraphics graphics,
		@NotNull Camera camera,
		boolean renderGameInfo,
		float renderTime,
		float partialTick
	) {
		super.render(playerManager, minecraft, level, player, renderEvent, bufferSource, poseStack, frustum, font, graphics, camera, renderGameInfo, renderTime, partialTick);

		if (BFClientSettings.UI_RENDER_WAYPOINTS.isEnabled()) {
			for (BombSite site : game.getBombSites()) {
				site.render(player, poseStack, font, graphics, camera);
			}
		}
	}

	@Override
	public void renderSpecific(
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
		float partialTick
	) {
	}

	@Override
	public boolean canChangePerspective(@NotNull Player player) {
		return game.getStatus() != GameStatus.GAME || player.getVehicle() != null;
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return new Scoreboard()
			.column("PING", (data, info, tag) -> info != null ? StringUtils.formatLong(info.getLatency()) : "???")
			.column("K", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.KILLS.getKey())))
			.column("A", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.ASSISTS.getKey())))
			.column("D", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.DEATHS.getKey())));
	}

	@Override
	public boolean canOpenInventory() {
		return false;
	}

	@Override
	public boolean shouldRenderNameTag(@NotNull Minecraft minecraft, @NotNull LivingEntity target, @NotNull LocalPlayer localPlayer, @NotNull ClientLevel level) {
		if (!(target instanceof Player targetPlayer)) {
			return false;
		}

		DefusalPlayerManager playerManager = game.getPlayerManager();

		GameTeam targetTeam = playerManager.getPlayerTeam(targetPlayer.getUUID());
		if (targetTeam == null) {
			return false;
		}

		GameTeam localTeam = playerManager.getPlayerTeam(localPlayer.getUUID());
		if (localTeam == null) {
			return false;
		}

		return localTeam.getName().equalsIgnoreCase(targetTeam.getName());
	}

	@Override
	public boolean canSwitchItem() {
		return true;
	}

	@Override
	public @NotNull List<MinimapWaypoint> getSpecificMinimapWaypoints(
		@NotNull Minecraft minecraft,
		@NotNull Set<UUID> players,
		@NotNull LocalPlayer localPlayer,
		@NotNull ClientLevel level
	) {
		DefusalPlayerManager playerManager = game.getPlayerManager();

		UUID localUuid = localPlayer.getUUID();

		GameTeam localTeam = playerManager.getPlayerTeam(localUuid);
		if (localTeam == null) {
			return List.of();
		}
		Set<UUID> localTeamPlayers = localTeam.getPlayers();

		List<MinimapWaypoint> waypoints = new ObjectArrayList<>();

		for (Player player : level.players()) {
			UUID playerUuid = player.getUUID();

			if (localTeamPlayers.contains(playerUuid) && !playerUuid.equals(localUuid)) {
				waypoints.add(
					new MinimapWaypoint(MinimapWaypoint.TEXTURE_PLAYER, player.position())
						.method_352(player.getYRot() - 180f)
				);
			}
		}

		return waypoints;
	}

	@Override
	public void setNameTagState(
		@NotNull Minecraft minecraft,
		@NotNull RenderNameTagEvent event,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level
	) {
		event.setCanRender(player.hasLineOfSight(event.getEntity()) ? TriState.TRUE : TriState.FALSE);
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
