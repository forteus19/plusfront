package dev.vuis.plusfront.gun;

import com.boehmod.blockfront.common.gun.GunConfig;
import com.boehmod.blockfront.common.gun.GunConfigRegistry;
import com.boehmod.blockfront.common.item.GunItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.vuis.plusfront.PlusFront;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class CustomGunConfigs {
	private CustomGunConfigs() {
		throw new AssertionError();
	}

	public static void load(String id) {
		try (InputStream stream = CustomGunConfigs.class.getResourceAsStream("/assets/pf/guns/" + id + ".json")) {
			if (stream == null) {
				PlusFront.LOGGER.error("Failed to find custom gun config for '{}'", id);
				return;
			}

			JsonElement root = JsonParser.parseReader(new InputStreamReader(stream));

			DataResult<Pair<GunConfig, JsonElement>> result = GunConfig.CODEC.decode(JsonOps.INSTANCE, root);
			if (result.isError()) {
				PlusFront.LOGGER.error("Failed to parse custom gun config: {}", result.error().orElseThrow().message());
				return;
			}

			GunConfigRegistry.register(PlusFront.res(id), result.result().orElseThrow().getFirst());
			PlusFront.LOGGER.info("Registered custom gun config for '{}'", id);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void load(DeferredHolder<Item, ? extends GunItem> item) {
		load(item.getId().getPath());
	}
}
