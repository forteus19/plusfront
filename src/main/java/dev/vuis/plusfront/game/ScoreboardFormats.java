package dev.vuis.plusfront.game;

import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.util.CompoundTagWrapper;
import com.boehmod.blockfront.util.StringUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScoreboardFormats {
	private ScoreboardFormats() {
		throw new AssertionError();
	}

	public static @NotNull Formatter ping() {
		return (data, info, tag) -> info != null ? StringUtils.formatLong(info.getLatency()) : "???";
	}

	public static @NotNull Formatter tagInt(@NotNull BFStat stat) {
		return (data, info, tag) -> StringUtils.formatLong(tag.getInt(stat.getKey()));
	}

	public static @NotNull Formatter tagPercent(@NotNull BFStat numStat, @NotNull BFStat denStat) {
		return (data, info, tag) -> {
			int num = tag.getInt(numStat.getKey());
			int den = tag.getInt(denStat.getKey());

			if (den == 0) {
				return "0";
			}

			return Integer.toString(Mth.floor((float) num / (float) den * 100f));
		};
	}

	@FunctionalInterface
	public interface Formatter extends TriFunction<@NotNull BFClientPlayerData, @Nullable PlayerInfo, @NotNull CompoundTagWrapper, @NotNull String> {
	}
}
