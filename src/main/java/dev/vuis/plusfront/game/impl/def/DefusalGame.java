package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.common.player.achievement.CloudAchievement;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.entity.BombEntity;
import com.boehmod.blockfront.common.item.BFConsumableItem;
import com.boehmod.blockfront.common.match.MatchCallout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.common.net.packet.BFRegularPingPacket;
import com.boehmod.blockfront.common.net.packet.BFRegularPingTriggerPacket;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageManager;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameTypeCodec;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.IdleGameStage;
import com.boehmod.blockfront.game.SpectatorScope;
import com.boehmod.blockfront.game.TeamJoinType;
import com.boehmod.blockfront.game.tag.IAllowsCallouts;
import com.boehmod.blockfront.game.tag.IAllowsPings;
import com.boehmod.blockfront.game.tag.IAllowsSoundboard;
import com.boehmod.blockfront.game.tag.ICanSwitchTeams;
import com.boehmod.blockfront.game.tag.IHasBombs;
import com.boehmod.blockfront.game.tag.IHasClasses;
import com.boehmod.blockfront.game.tag.IHasConsumables;
import com.boehmod.blockfront.game.tag.IHasDominations;
import com.boehmod.blockfront.game.tag.IUseKillIcons;
import com.boehmod.blockfront.registry.BFEntityTypes;
import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.registry.BFSounds;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.math.BFPose;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.data.PFDefusalData;
import dev.vuis.plusfront.ex.TeamDeathmatchCodecEx;
import dev.vuis.plusfront.util.PFUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.vuis.plusfront.util.AssetCommandUtil.executor;
import static dev.vuis.plusfront.util.AssetCommandUtil.executorPlayers;

public final class DefusalGame extends AbstractGame<DefusalGame, DefusalPlayerManager, GameStageManager<DefusalGame, DefusalPlayerManager>>
	implements IAllowsCallouts<DefusalGame>, IAllowsPings, IAllowsSoundboard, ICanSwitchTeams, IHasBombs, IHasClasses, IHasConsumables, IHasDominations, IUseKillIcons {

	public static final int SCORE_TO_WIN = 8;

	private final List<BombSite> bombSites = new ObjectArrayList<>();

	private final AssetCommandBuilder command = new AssetCommandBuilder()
		.subCommand("bombsite", new AssetCommandBuilder()
			.subCommand("add", executorPlayers(new String[]{"name", "radius"}, (context, source, args) -> {
				String name = args[0];

				float radius;
				try {
					radius = Float.parseFloat(args[1]);
				} catch (NumberFormatException e) {
					CommandUtils.sendBfa(source, Component.literal("Invalid radius."));
					return;
				}

				bombSites.add(new BombSite(
					PFUtil.copyVec3(((Player) source).position()),
					name,
					radius
				));

				CommandUtils.sendBfa(source, Component.literal("Added bombsite " + name + "."));
			}))
			.subCommand("clear", executor((context, source, args) -> {
				bombSites.clear();

				CommandUtils.sendBfa(source, Component.literal("Cleared all bombsites."));
			})))
		.subCommand("spawn", new AssetCommandBuilder()
			.subCommand("add", executorPlayers(new String[]{"team"}, (context, source, args) -> {
				String teamName = args[0];

				GameTeam team = playerManager.getTeamByName(teamName);
				if (team == null) {
					CommandUtils.sendBfa(source, Component.literal("Team " + teamName + " was not found!"));
					return;
				}

				team.addPlayerSpawn(new BFPose((Player) source));

				CommandUtils.sendBfa(source, Component.literal(teamName + " team spawn added. (" + team.getPlayerSpawns().size() + ")"));
			}))
			.subCommand("clear", executor((context, source, args) -> {
				String teamName = args[0];

				GameTeam team = playerManager.getTeamByName(teamName);
				if (team == null) {
					CommandUtils.sendBfa(source, Component.literal("Team " + teamName + " was not found!"));
					return;
				}

				team.clearPlayerSpawns();

				CommandUtils.sendBfa(source, Component.literal(teamName + " team's spawns cleared."));
			})));

	private boolean isBombPlanted = false;
	private @Nullable Integer bombItemId = null;

	public DefusalGame(@NotNull BFAbstractManager<?, ?, ?> manager) {
		super(manager, "def", "Defusal");
	}

	public List<BombSite> getBombSites() {
		return bombSites;
	}

	public @Nullable ItemEntity getBombItem(Level level) {
		if (bombItemId != null) {
			Entity entity = level.getEntity(bombItemId);

			if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() == BFItems.BOMB.value()) {
				return itemEntity;
			} else {
				bombItemId = null;
				return null;
			}
		} else {
			return null;
		}
	}

	public void setBombItem(@Nullable ItemEntity bombItem) {
		if (bombItem != null) {
			if (bombItem.getItem().getItem() != BFItems.BOMB.value()) {
				throw new IllegalArgumentException("bf:bomb expected");
			}
			bombItemId = bombItem.getId();
		} else {
			bombItemId = null;
		}
	}

	@Override
	public @NotNull AssetCommandBuilder getCommand() {
		return super.getCommand().inherit(command);
	}

	@Override
	public @NotNull AbstractGameClient<?, ?> createGameClient(@NotNull BFClientManager manager) {
		return new DefusalGameClient(manager, this, (ClientPlayerDataHandler) dataHandler);
	}

	@Override
	protected @NotNull DefusalPlayerManager createPlayerManager() {
		return new DefusalPlayerManager(this, dataHandler);
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createFirstStage() {
		return new IdleGameStage<>(DefusalPreStage::new);
	}

	@Override
	public void updateStageManager(@NotNull BFAbstractManager<?, ?, ?> manager, @NotNull PlayerDataHandler<?> dataHandler, @NotNull ServerLevel level, @NotNull Set<UUID> players) {
		stageManager.update(manager, dataHandler, level);
	}

	@Override
	protected boolean isMatchSuccess() {
		return true;
	}

	@Override
	public boolean playerJoinTeam(@NotNull BFAbstractManager<?, ?, ?> manager, @NotNull TeamJoinType joinType, @NotNull ServerLevel level, @NotNull ServerPlayer player) {
		PlusFront.LOGGER.info("[Defusal] Player {} joining", player.getScoreboardName());

		GameTeam joiningTeam = playerManager.getNextJoiningTeam();

		if (joiningTeam == null) {
			PlusFront.LOGGER.warn("[Defusal] No team available for {}", player.getScoreboardName());
			return false;
		}

		return playerManager.playerJoinTeam(manager, joinType, level, player, joiningTeam);
	}

	@Override
	public void specificReset(@Nullable Level level) {
		isBombPlanted = false;
		bombItemId = null;
	}

	@Override
	public @NotNull GameTypeCodec getCodecData() {
		GameTypeCodec.TeamDeathmatch codec = new GameTypeCodec.TeamDeathmatch(
			Optional.empty()
		);

		TeamDeathmatchCodecEx.cast(codec).pf$setDefusalData(Optional.of(new PFDefusalData(
			List.copyOf(bombSites)
		)));

		return codec;
	}

	@Override
	public void readCodecData(@NotNull GameTypeCodec data) {
		if (!(data instanceof GameTypeCodec.TeamDeathmatch tdmData)) {
			return;
		}

		// should never throw
		PFDefusalData defusalData = TeamDeathmatchCodecEx.cast(tdmData).pf$getDefusalData().orElseThrow();

		bombSites.clear();
		bombSites.addAll(defusalData.bombSites());
	}

	@Override
	public void writeAll(@NotNull ByteBuf buf, boolean writeMap) throws IOException {
		super.writeAll(buf, writeMap);

		VarInt.write(buf, bombSites.size());
		for (BombSite site : bombSites) {
			site.write(buf);
		}

		buf.writeBoolean(isBombPlanted);

		buf.writeBoolean(bombItemId != null);
		if (bombItemId != null) {
			VarInt.write(buf, bombItemId);
		}
	}

	@Override
	public void readAll(@NotNull ByteBuf buf) throws IOException {
		super.readAll(buf);

		bombSites.clear();
		int numBombSites = VarInt.read(buf);
		for (int i = 0; i < numBombSites; i++) {
			bombSites.add(BombSite.read(buf));
		}

		isBombPlanted = buf.readBoolean();

		bombItemId = null;
		if (buf.readBoolean()) {
			bombItemId = VarInt.read(buf);
		}
	}

	@Override
	public boolean shouldUseStamina(@NotNull Player player) {
		return false;
	}

	@Override
	public boolean shouldRespawnAutomatically(@NotNull Player player) {
		return true;
	}

	@Override
	public @NotNull SpectatorScope getSpectatorScope() {
		return SpectatorScope.SAME_TEAM;
	}

	@Override
	public boolean shouldSpectateOnRespawn() {
		return true;
	}

	@Override
	public boolean shouldAnnounceRageQuits() {
		return true;
	}

	@Override
	public boolean shouldShowDeadMessages() {
		return false;
	}

	@Override
	public @Nullable CloudAchievement getVictoryAchievement() {
		return null;
	}

	@Override
	public void onCallout(@NotNull ServerPlayer player, @NotNull UUID uuid, @NotNull MatchCallout callout) {
		handleCallout(this, uuid, callout);
	}

	@Override
	public void onPingRequest(@NotNull BFAbstractManager<?, ?, ?> manager, @NotNull ServerPlayer player, @NotNull Vec3 position) {
		UUID playerUuid = player.getUUID();

		GameTeam team = playerManager.getPlayerTeam(playerUuid);
		if (team == null) {
			return;
		}

		team.sendPacket(new BFRegularPingPacket(
			playerUuid, UUID.randomUUID(), position
		));
	}

	@Override
	public void onPingTriggerRequest(@NotNull BFAbstractManager<?, ?, ?> manager, @NotNull ServerPlayer player, @NotNull UUID pingUuid, @NotNull Vec3 position) {
		UUID playerUuid = player.getUUID();

		GameTeam team = playerManager.getPlayerTeam(playerUuid);
		if (team == null) {
			return;
		}

		team.sendPacket(new BFRegularPingTriggerPacket(
			playerUuid, pingUuid, position
		));
	}

	@Override
	public int getMaximumPlayerSounds(@NotNull ServerPlayer serverPlayer) {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getSoundboardCooldown() {
		return 160;
	}

	@Override
	public @Nullable Component getSwitchTeamMessage(@NotNull ServerPlayer player) {
		GameTeam ctTeam = playerManager.getTeamByName(DefusalPlayerManager.CT_NAME);
		assert ctTeam != null;
		GameTeam tTeam = playerManager.getTeamByName(DefusalPlayerManager.T_NAME);
		assert tTeam != null;

		return getSwitchTeamMessage(player, tTeam.numPlayers(), ctTeam.numPlayers());
	}

	@Override
	public void playerSwitchTeam(@NotNull BFAbstractManager<?, ?, ?> manager, @NotNull ServerLevel level, @NotNull ServerPlayer player, @NotNull UUID uuid) {
		playerSwitchTeamInternal(manager, level, player, uuid);
	}

	@Override
	public int getTeamSwitchCooldown() {
		return 2 * 60 * 20;
	}

	@Override
	public int method_3386() {
		// unused
		return 0;
	}

	public boolean isBombPlanted() {
		return isBombPlanted;
	}

	public void resetBombPlanted() {
		isBombPlanted = false;
	}

	@Override
	public void onBombExplode(@NotNull BombEntity bomb, @NotNull Level level) {
		isBombPlanted = false;

		onRoundWin(playerManager.getPlayers(), false);
	}

	@Override
	public void onBombDefused(@NotNull BombEntity bomb, @Nullable ServerPlayer player, @NotNull UUID playerUuid) {
		isBombPlanted = false;

		Set<UUID> players = playerManager.getPlayers();

//		GameUtils.playSound(
//			players,
//			BFSounds.ITEM_BOMB_DEFUSE.value(),
//			SoundSource.NEUTRAL
//		);
		GameUtils.sendNotification(
			players,
			Component.translatable("pf.message.gamemode.notification.bomb.defused")
				.withStyle(ChatFormatting.BLUE),
			90,
			"bomb.defused"
		);

		onRoundWin(players, true);
	}

	@Override
	public boolean canDefuseBomb(@NotNull Level level, @NotNull Player player, @NotNull BombEntity bomb) {
		GameTeam team = playerManager.getPlayerTeam(player.getUUID());
		return team != null && team.getName().equals(DefusalPlayerManager.CT_NAME);
	}

	@Override
	public boolean canPlantBomb(@NotNull Level level, @NotNull Player player) {
		if (isBombPlanted || !player.onGround()) {
			return false;
		}

		GameTeam team = playerManager.getPlayerTeam(player.getUUID());
		if (team == null || !team.getName().equals(DefusalPlayerManager.T_NAME)) {
			return false;
		}

		return checkBombSiteDistance(player.position());
	}

	@Override
	public void onBombPlanted(@NotNull Level level, @NotNull Player player, @NotNull ItemStack heldStack) {
		if (!checkBombSiteDistance(player.position())) {
			PlusFront.LOGGER.warn("Player {} tried to plant outside of bombsite radius!", player.getScoreboardName());
			return;
		}

		isBombPlanted = true;

		BombEntity bomb = new BombEntity(BFEntityTypes.BOMB.value(), level);
		bomb.setGame(this);
		bomb.setPos(player.position());

		level.addFreshEntity(bomb);

		playerManager.clearBombPlayer();
		player.getInventory().removeItem(heldStack);

		Set<UUID> players = playerManager.getPlayers();

		GameUtils.sendNotification(
			players,
			Component.translatable("pf.message.gamemode.notification.bomb.planted")
				.withStyle(ChatFormatting.RED),
			100,
			"bomb.planted"
		);

		GameUtils.playSound(
			players,
			BFSounds.ITEM_BOMB_PLANT.value(),
			SoundSource.NEUTRAL
		);
		for (UUID playerUuid : players) {
			if (!playerUuid.equals(player.getUUID())) {
				GameUtils.playSound(
					playerUuid,
					SoundEvents.ENDER_DRAGON_FLAP,
					SoundSource.NEUTRAL,
					1.0f, 1.5f
				);
			}
		}
	}

	public boolean checkBombSiteDistance(Vec3 playerPos) {
		for (BombSite site : bombSites) {
			float radius = site.radius();

			if (playerPos.distanceToSqr(site.position()) <= radius * radius) {
				return true;
			}
		}

		return false;
	}

	public void onRoundWin(Set<UUID> players, boolean ctWin) {
		if (!(stageManager.getCurrentStage() instanceof DefusalGameStage gameStage) || gameStage.isFinished) {
			return;
		}
		gameStage.isFinished = true;

		GameTeam ctTeam = playerManager.getTeamByName(DefusalPlayerManager.CT_NAME);
		assert ctTeam != null;
		GameTeam tTeam = playerManager.getTeamByName(DefusalPlayerManager.T_NAME);
		assert tTeam != null;

		GameTeam winningTeam = ctWin ? ctTeam : tTeam;
		GameTeam losingTeam = ctWin ? tTeam : ctTeam;

		winningTeam.putStatInt(BFStats.SCORE, winningTeam.getStatInt(BFStats.SCORE) + 1);

		GameUtils.sendNotification(
			players,
			Component.translatable(
				"pf.message.gamemode.notification.round.win",
				Component.literal(winningTeam.getName()).withStyle(winningTeam.getStyleText())
			),
			90,
			"round.end"
		);

		GameUtils.playSound(
			winningTeam.getPlayers(),
			BFSounds.MATCH_GAMEMODE_DOM_POINT_CAPTURED.value(),
			SoundSource.NEUTRAL
		);
		GameUtils.playSound(
			losingTeam.getPlayers(),
			BFSounds.MATCH_GAMEMODE_DOM_POINT_LOST.value(),
			SoundSource.NEUTRAL
		);
	}

	public void onRoundDraw(Set<UUID> players) {
		if (!(stageManager.getCurrentStage() instanceof DefusalGameStage gameStage) || gameStage.isFinished) {
			return;
		}
		gameStage.isFinished = true;

		GameUtils.sendNotification(
			players,
			Component.translatable("pf.message.gamemode.notification.round.draw")
				.withStyle(ChatFormatting.GOLD),
			90,
			"round.end"
		);

		GameUtils.playSound(
			players,
			BFSounds.MATCH_GAMEMODE_DOM_POINT_LOST.value(),
			SoundSource.NEUTRAL
		);
	}

	@Override
	public boolean hasClassLimits() {
		return true;
	}

	@Override
	public boolean shouldClassesRequireExp() {
		return true;
	}

	@Override
	public boolean shouldClassesRequireRank() {
		return true;
	}

	@Override
	public boolean shouldForceClassSelection() {
		return true;
	}

	@Override
	public int getClassLimit(@NotNull MatchClass matchClass) {
		return switch (matchClass.getKey()) {
			case "assault" -> 3;
			case "support", "medic", "sniper" -> 2;
			case "gunner", "specialist", "anti_tank", "commander" -> 1;
			default -> 0;
		};
	}

	@Override
	public @NotNull Set<MatchClass> getBannedClasses() {
		MatchClass gunner = MatchClass.getByKey("gunner");
		assert gunner != null;
		MatchClass antiTank = MatchClass.getByKey("anti_tank");
		assert antiTank != null;
		MatchClass specialist = MatchClass.getByKey("specialist");
		assert specialist != null;
		MatchClass commander = MatchClass.getByKey("commander");
		assert commander != null;

		return Set.of(gunner, antiTank, specialist, commander);
	}

	@Override
	public boolean canUseConsumable(@NotNull Level level, @NotNull Player player, @NotNull BFConsumableItem item, @NotNull ItemStack stack) {
		return true;
	}

	@Override
	public void onUseConsumable(@NotNull ServerLevel level, @NotNull Player player, @NotNull BFConsumableItem item, @NotNull ItemStack stack) {
	}

	@Override
	public int getDominationThreshold() {
		return 4;
	}

	@Override
	public boolean shouldAddKillFeedEntries() {
		return true;
	}
}
