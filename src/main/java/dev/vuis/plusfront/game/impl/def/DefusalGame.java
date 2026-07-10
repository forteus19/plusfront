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
import com.boehmod.blockfront.game.GameBoundary;
import com.boehmod.blockfront.game.GameStageManager;
import com.boehmod.blockfront.game.GameStageTimer;
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
import com.mojang.brigadier.context.CommandContext;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.data.PFDefusalData;
import dev.vuis.plusfront.ex.TeamDeathmatchCodecEx;
import dev.vuis.plusfront.util.PFUtil;
import dev.vuis.plusfront.world.BombDamageSource;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.vuis.plusfront.util.AssetCommandUtil.executor;
import static dev.vuis.plusfront.util.AssetCommandUtil.executorPlayers;

public final class DefusalGame extends AbstractGame<DefusalGame, DefusalPlayerManager, GameStageManager<DefusalGame, DefusalPlayerManager>>
	implements IAllowsCallouts<DefusalGame>, IAllowsPings, IAllowsSoundboard, ICanSwitchTeams, IHasBombs, IHasClasses, IHasConsumables, IHasDominations, IUseKillIcons {

	public static final int SCORE_TO_WIN = 10;

	private static final float BOMB_EXPLOSION_RADIUS = 16f;

	private final List<BombSite> bombSites = new ObjectArrayList<>();

	private final AssetCommandBuilder command = new AssetCommandBuilder()
		.subCommand("bombsite", new AssetCommandBuilder()
			.subCommand("add", executor(new String[]{"name"}, (context, source, args) -> {
				String name = args[0];

				bombSites.add(new BombSite(name));

				CommandUtils.sendBfa(source, Component.literal("Added bomb site " + name + ". (" + bombSites.size() + ")"));
			}))
			.subCommand("clear", executor((context, source, args) -> {
				bombSites.clear();

				CommandUtils.sendBfa(source, Component.literal("Cleared all bombsites."));
			}))
			.subCommand("boundary", executorPlayers(new String[]{"name"}, this::suggestBombSites, (context, source, args) -> {
				String name = args[0];

				BombSite bombSite = getBombSiteByName(name);
				if (bombSite == null) {
					CommandUtils.sendBfa(source, Component.literal("Bomb site " + name + " was not found!"));
					return;
				}

				GameBoundary boundary = dataHandler.getPlayerData((Player) source).getRegionSelection().getBoundary();
				if (boundary == null) {
					CommandUtils.sendBfa(source, Component.literal("No region selection! Select an area with the region wand first."));
					return;
				}

				bombSite.boundary = boundary;

				CommandUtils.sendBfa(source, Component.literal("Set boundary for bomb site " + name + " from your selection. (" + boundary.numPoints() + " points)"));
			}))
			.subCommand("visibleY", executorPlayers(new String[]{"name"}, this::suggestBombSites, (context, source, args) -> {
				String name = args[0];

				BombSite bombSite = getBombSiteByName(name);
				if (bombSite == null) {
					CommandUtils.sendBfa(source, Component.literal("Bomb site " + name + " was not found!"));
					return;
				}

				bombSite.visibleY = ((Player) source).position().y;

				CommandUtils.sendBfa(source, Component.literal("Set visible Y for bomb site " + name + "."));
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

	private Collection<String> suggestBombSites(CommandContext<CommandSourceStack> context, String[] args) {
		return args.length == 0 ? bombSites.stream().map(site -> site.name).toList() : List.of();
	}

	private @Nullable BombSite getBombSiteByName(String name) {
		for (BombSite bombSite : bombSites) {
			if (bombSite.name.equalsIgnoreCase(name)) {
				return bombSite;
			}
		}

		return null;
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

	public void clearBombItem() {
		bombItemId = null;
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

	public boolean isPreStage() {
		return stageManager.getCurrentStage().getClass() == DefusalPreStage.class;
	}

	public boolean isRoundWaiting() {
		return stageManager.getCurrentStage().getClass() == DefusalWaitingStage.class;
	}

	public boolean isRoundInProgress() {
		return stageManager.getCurrentStage().getClass() == DefusalGameStage.class;
	}

	public @Nullable GameStageTimer getInProgressTimer() {
		if (isRoundInProgress()) {
			return ((DefusalGameStage) stageManager.getCurrentStage()).getStageTimer(this);
		} else {
			return null;
		}
	}

	public boolean isRoundFinished() {
		return stageManager.getCurrentStage().getClass() == DefusalFinishedStage.class;
	}

	public boolean finishRound() {
		if (isRoundInProgress()) {
			stageManager.forceAdvance();
			return true;
		} else {
			return false;
		}
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
		onRoundFinished();
	}

	@Override
	public @NotNull GameTypeCodec getCodecData() {
		GameTypeCodec.TeamDeathmatch codec = new GameTypeCodec.TeamDeathmatch(
			Optional.empty()
		);

		List<BombSiteCodec> bombSiteCodecs = new ObjectArrayList<>(bombSites.size());
		for (BombSite bombSite : bombSites) {
			bombSiteCodecs.add(bombSite.toCodec());
		}

		TeamDeathmatchCodecEx.cast(codec).pf$setDefusalData(Optional.of(new PFDefusalData(
			bombSiteCodecs
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
		for (BombSiteCodec bombSiteCodec : defusalData.bombSites()) {
			bombSites.add(BombSite.fromCodec(bombSiteCodec));
		}
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
	public void writeForClient(@NotNull ByteBuf buf) throws IOException {
		super.writeForClient(buf);

		buf.writeBoolean(isRoundInProgress());
		buf.writeBoolean(isRoundFinished());
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
		return (isRoundInProgress() || isRoundWaiting()) ? SpectatorScope.SAME_TEAM : SpectatorScope.ANYONE;
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
	public boolean shouldShowPlayerMessage(@NotNull UUID senderUuid, @NotNull ServerPlayer receivingPlayer, @NotNull UUID receivingUuid, boolean isTeamMessage, boolean isSenderUnavailable) {
		GameTeam senderTeam = playerManager.getPlayerTeam(senderUuid);
		GameTeam receivingTeam = playerManager.getPlayerTeam(receivingUuid);
		if (senderTeam == null || receivingTeam == null) {
			return false;
		}

		if (isRoundWaiting() || isRoundInProgress()) {
			return (!isSenderUnavailable && !isTeamMessage) || senderTeam == receivingTeam;
		} else {
			return !isTeamMessage || senderTeam == receivingTeam;
		}
	}

	@Override
	public boolean shouldShowDeadMessages() {
		PlusFront.LOGGER.error("[Defusal] shouldShowDeadMessages called!");
		return false;
	}

	@Override
	public @Nullable CloudAchievement getVictoryAchievement() {
		return null;
	}

	@Override
	public void getErrorMessages(@NotNull List<MutableComponent> messages) {
		super.getErrorMessages(messages);

		if (bombSites.isEmpty()) {
			messages.add(Component.literal("Bomb sites for game " + name + " are missing."));
		}

		for (BombSite bombSite : bombSites) {
			if (bombSite.boundary.isEmpty()) {
				messages.add(Component.literal("Boundary for bomb site " + bombSite.name + " for game " + name + " is missing."));
			}
		}
	}

	@Override
	public void onCallout(@NotNull ServerPlayer player, @NotNull UUID uuid, @NotNull MatchCallout callout) {
		handleCallout(this, uuid, callout);
	}

	@Override
	public void onPingRequest(@NotNull BFAbstractManager<?, ?, ?> manager, @NotNull ServerPlayer player, @NotNull Vec3 position) {
		if (PFUtil.isPlayerUnavailable(player)) {
			return;
		}

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

	public void clearBombPlanted() {
		isBombPlanted = false;
	}

	@Override
	public void onBombExplode(@NotNull BombEntity bomb, @NotNull Level level) {
		doBombExplosion(bomb, level);

		clearBombPlanted();
		playerManager.setBombPlanter(null);

		onRoundWin(playerManager.getPlayers(), false);
	}

	private void doBombExplosion(BombEntity bomb, Level level) {
		Vec3 bombPosition = bomb.position();

		BombDamageSource damageSource = new BombDamageSource(
			level,
			playerManager.getBombPlanter()
		);

		Explosion explosion = new Explosion(
			level,
			bomb,
			damageSource,
			null,
			bombPosition.x,
			bombPosition.y,
			bombPosition.z,
			BOMB_EXPLOSION_RADIUS,
			false,
			Explosion.BlockInteraction.KEEP,
			ParticleTypes.EXPLOSION,
			ParticleTypes.EXPLOSION_EMITTER,
			SoundEvents.GENERIC_EXPLODE
		);

		explosion.explode();
		explosion.finalizeExplosion(true);
	}

	@Override
	public void onBombDefused(@NotNull BombEntity bomb, @Nullable ServerPlayer player, @NotNull UUID playerUuid) {
		if (player == null) {
			return;
		}

		BFAbstractManager<?, ?, ?> manager = PFUtil.blockfrontManager();

		isBombPlanted = false;

		GameUtils.changePlayerStat(manager, this, player.getUUID(), BFStats.SCORE, 3);

		Set<UUID> players = playerManager.getPlayers();

		GameUtils.sendNotification(
			players,
			Component.translatable("pf.message.gamemode.notification.bomb.defused")
				.withStyle(ChatFormatting.BLUE),
			100,
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
		if (isBombPlanted) {
			return false;
		}

		GameTeam team = playerManager.getPlayerTeam(player.getUUID());
		if (team == null || !team.getName().equals(DefusalPlayerManager.T_NAME)) {
			return false;
		}

		Vec3 playerPos = player.position();

		return checkBombSiteArea(playerPos);
	}

	/**
	 * Checks if the given position is within the planting area of any bomb site.
	 *
	 * @param playerPos the position to check against
	 * @return if the given position is within the planting area of any bomb site
	 */
	public boolean checkBombSiteArea(Vec3 playerPos) {
		for (BombSite site : bombSites) {
			if (site.isWithinArea(playerPos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onBombPlanted(@NotNull Level level, @NotNull Player player, @NotNull ItemStack heldStack) {
		BFAbstractManager<?, ?, ?> manager = PFUtil.blockfrontManager();

		if (!checkBombSiteArea(player.position())) {
			PlusFront.LOGGER.warn("Player {} tried to plant outside of bombsite radius!", player.getScoreboardName());
			return;
		}

		isBombPlanted = true;

		BombEntity bomb = new BombEntity(BFEntityTypes.BOMB.value(), level);
		bomb.setGame(this);
		bomb.moveTo(player.position(), player.getYRot(), 0f);

		level.addFreshEntity(bomb);

		player.getInventory().removeItem(heldStack);
		playerManager.clearBombHolder();
		playerManager.setBombPlanter(player);

		GameUtils.changePlayerStat(manager, this, player.getUUID(), BFStats.SCORE, 3);

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

	/**
	 * Called when a team wins the current round. Marks the current round as finished, and does nothing if a round is not in progress.
	 *
	 * @param players all players in the match
	 * @param ctWin {@code true} if the Counter-Terrorists won, {@code false} if the Terrorists won
	 */
	public void onRoundWin(Set<UUID> players, boolean ctWin) {
		if (!finishRound()) {
			return;
		}

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
			120,
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

	/**
	 * Called when the current round ends in a draw. Marks the current round as finished, and does nothing if a round is not in progress.
	 *
	 * @param players all players in the match
	 */
	public void onRoundDraw(Set<UUID> players) {
		if (!finishRound()) {
			return;
		}

		GameUtils.sendNotification(
			players,
			Component.translatable("pf.message.gamemode.notification.round.draw")
				.withStyle(ChatFormatting.GOLD),
			120,
			"round.end"
		);

		GameUtils.playSound(
			players,
			BFSounds.MATCH_GAMEMODE_DOM_POINT_LOST.value(),
			SoundSource.NEUTRAL
		);
	}

	public void onRoundFinished() {
		clearBombPlanted();
		clearBombItem();
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
