package dev.vuis.plusfront.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.vuis.plusfront.PlusFront;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PFCustomMemes {
	private static final Codec<Map<String, ResourceLocation>> CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC);

	private static final Component READ_ERROR = Component.translatable("pf.message.memes.error.read");
	private static final Component PARSE_ERROR = Component.translatable("pf.message.memes.error.parse");

	public static final Map<String, Holder<SoundEvent>> LOADED = new Object2ObjectOpenHashMap<>();

	private PFCustomMemes() {
	}

	public static @Nullable Component load(@NotNull MinecraftServer server) {
		PlusFront.LOGGER.info("Loading custom memes");

		Path path = server.getServerDirectory().resolve(Path.of("plusfront", "bf_custom_memes.json"));

		if (!Files.exists(path)) {
			PlusFront.LOGGER.info("Custom memes not found, skipping");

			LOADED.clear();
			return null;
		}

		JsonElement rootElement;

		try (Reader reader = Files.newBufferedReader(path)) {
			rootElement = JsonParser.parseReader(reader);
		} catch (IOException e) {
			PlusFront.LOGGER.error("Failed to read custom memes");
			return READ_ERROR;
		} catch (JsonSyntaxException e) {
			PlusFront.LOGGER.error("Failed to parse custom memes");
			return PARSE_ERROR;
		}

		var parseResult = CODEC.parse(JsonOps.INSTANCE, rootElement);
		if (parseResult.isError()) {
			PlusFront.LOGGER.error(
				"Failed to parse custom memes ({})",
				parseResult.error().orElseThrow().messageSupplier().get()
			);
			return PARSE_ERROR;
		}

		LOADED.clear();

		parseResult.getOrThrow().forEach((name, memeLocation) -> {
			BuiltInRegistries.SOUND_EVENT.getHolder(memeLocation).ifPresent(meme -> LOADED.put(name, meme));
		});

		PlusFront.LOGGER.info("Loaded {} custom memes", LOADED.size());

		return null;
	}
}
