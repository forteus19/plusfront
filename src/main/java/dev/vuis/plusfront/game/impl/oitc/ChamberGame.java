package dev.vuis.plusfront.game.impl.oitc;

import com.boehmod.bflib.cloud.common.player.achievement.CloudAchievement;
import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.ClassType;
import com.boehmod.blockfront.game.GameStageManager;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameTypeCodec;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.IdleGameStage;
import com.boehmod.blockfront.game.Loadout;
import com.boehmod.blockfront.game.TeamJoinType;
import com.boehmod.blockfront.game.TeamType;
import com.boehmod.blockfront.game.tag.IAllowsRegeneration;
import com.boehmod.blockfront.game.tag.IAllowsRespawning;
import com.boehmod.blockfront.game.tag.IAllowsSoundboard;
import com.boehmod.blockfront.game.tag.IAnnounceFirstBlood;
import com.boehmod.blockfront.game.tag.IHasDominations;
import com.boehmod.blockfront.game.tag.IHasKillStreaks;
import com.boehmod.blockfront.game.tag.IUseKillIcons;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.math.BFPose;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.ex.TeamDeathmatchCodecEx;
import dev.vuis.plusfront.game.CustomTeamTypes;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.vuis.plusfront.util.AssetCommandUtil.executor;

public final class ChamberGame extends AbstractGame<ChamberGame, ChamberPlayerManager, GameStageManager<ChamberGame, ChamberPlayerManager>>
	implements
	IAllowsRegeneration,
	IAllowsRespawning,
	IAllowsSoundboard,
	IAnnounceFirstBlood,
	IHasDominations,
	IHasKillStreaks,
	IUseKillIcons {

	private final AssetCommandBuilder command = new AssetCommandBuilder()
		.subCommand("spawn", new AssetCommandBuilder()
			.subCommand("add", executor((context, source, args) -> {
				int count = playerManager.addPlayerSpawn(new BFPose((Player) source));

				CommandUtils.sendBfa(source, Component.literal("Player spawn added. (" + count + ")"));
			}))
			.subCommand("clear", executor((context, source, args) -> {
				playerManager.allies().clearPlayerSpawns();

				CommandUtils.sendBfa(source, Component.literal("Player spawns cleared."));
			})));

	public ChamberGame(@NotNull BFAbstractManager<?, ?, ?> manager) {
		super(manager, "oitc", "One In The Chamber");
	}

	@Override
	public @NotNull AbstractGameClient<?, ?> createGameClient(@NotNull BFClientManager manager) {
		return new ChamberGameClient(manager, this, (ClientPlayerDataHandler) dataHandler);
	}

	@Override
	protected @NotNull ChamberPlayerManager createPlayerManager() {
		return new ChamberPlayerManager(this, dataHandler);
	}

	@Override
	public @NotNull AbstractGameStage<ChamberGame, ChamberPlayerManager> createFirstStage() {
		return new IdleGameStage<>(ChamberPreStage::new);
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
		PlusFront.LOGGER.info("[Chamber] Player {} joining", player.getScoreboardName());

		GameTeam joiningTeam = playerManager.getNextJoiningTeam();

		if (joiningTeam == null) {
			PlusFront.LOGGER.warn("[Chamber] No team available for {}", player.getScoreboardName());
			return false;
		}

		return playerManager.playerJoinTeam(manager, joinType, level, player, joiningTeam);
	}

	@Override
	public void specificReset(@Nullable Level level) {
	}

	@Override
	public @NotNull GameTypeCodec getCodecData() {
		GameTypeCodec.TeamDeathmatch codec = new GameTypeCodec.TeamDeathmatch(
			Optional.empty()
		);

		TeamDeathmatchCodecEx.cast(codec).pf$setChamber(true);

		return codec;
	}

	@Override
	public void readCodecData(@NotNull GameTypeCodec data) {
	}

	@Override
	public boolean shouldUseStamina(@NotNull Player player) {
		return true;
	}

	@Override
	public boolean shouldRespawnAutomatically(@NotNull Player player) {
		return true;
	}

	@Override
	public int getMinimumPlayers() {
		return 2;
	}

	@Override
	public boolean shouldAnnounceRageQuits() {
		return false;
	}

	@Override
	public @NotNull TeamType getAlliesDivision() {
		return CustomTeamTypes.US_COWBOY;
	}

	@Override
	public boolean shouldShowDeadMessages() {
		return true;
	}

	@Override
	public @Nullable CloudAchievement getVictoryAchievement() {
		return null;
	}

	@Override
	public @NotNull AssetCommandBuilder getCommand() {
		return super.getCommand().inherit(command);
	}

	@Override
	public boolean playerCanRegenerate(@NotNull Player player) {
		return player.getHealth() < player.getMaxHealth();
	}

	@Override
	public float getRegenerationAmount(@NotNull Player player) {
		return 1f;
	}

	@Override
	public int getRegenerationTicks(@NotNull Player player) {
		return 40;
	}

	@Override
	public int getSpawnProtectionTicks() {
		return 0;
	}

	@Override
	public boolean shouldCancelSpawnProtection(@NotNull UUID uuid) {
		return false;
	}

	@Override
	public boolean shouldCancelSpawnProtectionOnShoot(@NotNull UUID uuid) {
		return false;
	}

	@Override
	public int getMaximumPlayerSounds(@NotNull ServerPlayer serverPlayer) {
		return 8;
	}

	@Override
	public int getSoundboardCooldown() {
		return 160;
	}

	@Override
	public boolean shouldAnnounceFirstBlood(@NotNull ServerPlayer player) {
		return true;
	}

	@Override
	public int getDominationThreshold() {
		return 4;
	}

	@Override
	public boolean shouldAddKillFeedEntries() {
		return true;
	}

	public void giveLoadout(ServerLevel level, ServerPlayer player) {
		ClassType rifleman = ClassType.getByKey("rifleman");
		assert rifleman != null;

		Loadout loadout = getAlliesDivision().getLoadout(rifleman, 0);
		if (loadout == null) {
			PlusFront.LOGGER.error("Rifleman I loadout is missing!");
			return;
		}

		GameUtils.giveLoadout(level, player, loadout);
	}
}
