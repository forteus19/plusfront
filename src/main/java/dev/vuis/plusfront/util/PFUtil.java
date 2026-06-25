package dev.vuis.plusfront.util;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PFUtil {
	private PFUtil() {
		throw new AssertionError();
	}

	public static Vec3 copyVec3(Vec3 original) {
		return new Vec3(original.x, original.y, original.z);
	}

	public static @Nullable AbstractGame<?, ?, ?> getPlayerGame(UUID playerUuid) {
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			return null;
		}
		return manager.getPlayerGame(playerUuid);
	}

	public static @Nullable AbstractGame<?, ?, ?> getPlayerGame(Player player) {
		return getPlayerGame(player.getUUID());
	}
}
