package dev.vuis.plusfront.util.index;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public final class FeatureFlagIndex {
	public static final String EVENT_HALLOWEEN = "event_halloween";
	public static final String EVENT_CHRISTMAS = "event_christmas";
	public static final String SERVER_SHOT_VALIDATION = "server_shot_validation";
	public static final String SERVER_SHOT_VALIDATION_SPREAD = "server_shot_validation_spread";
	public static final String SERVER_SHOT_VALIDATION_KICK = "server_shot_validation_kick";
	public static final String SERVER_SHOT_VALIDATION_REPORT = "server_shot_validation_report";
	public static final String SERVER_MATCH_FEATURE_PING = "server_match_feature_ping";
	public static final String SERVER_GRENADE_COOK_ON_DEATH = "server_grenade_cook_drop_on_death";
	public static final String CLIENT_FANCY_BULLET_EFFECTS = "client_fancy_bullet_effects";
	public static final String CLIENT_VEIL_FANCY_GUN_LIGHT = "client_veil_fancy_gun_light";
	public static final String SERVER_PLAYER_VOICE_SOUNDS = "server_player_voice_sounds";
	public static final String SERVER_CHAT_MARKDOWN = "server_chat_markdown";

	public static final Map<String, Boolean> DEFAULT = ImmutableMap.<String, Boolean>builder()
		.put(EVENT_HALLOWEEN, false)
		.put(EVENT_CHRISTMAS, false)
		.put(SERVER_SHOT_VALIDATION, true)
		.put(SERVER_SHOT_VALIDATION_SPREAD, true)
		.put(SERVER_SHOT_VALIDATION_KICK, false)
		.put(SERVER_SHOT_VALIDATION_REPORT, false)
		.put(SERVER_MATCH_FEATURE_PING, false)
		.put(SERVER_GRENADE_COOK_ON_DEATH, false)
		.put(CLIENT_FANCY_BULLET_EFFECTS, false)
		.put(CLIENT_VEIL_FANCY_GUN_LIGHT, false)
		.put(SERVER_PLAYER_VOICE_SOUNDS, true)
		.put(SERVER_CHAT_MARKDOWN, true)
		.build();

	private FeatureFlagIndex() {
		throw new AssertionError();
	}

	public static Object2BooleanMap<String> mutableDefault() {
		return new Object2BooleanOpenHashMap<>(DEFAULT);
	}

	public static boolean isAcknowledged(String flag) {
		return DEFAULT.containsKey(flag);
	}

	public static SuggestionProvider<CommandSourceStack> suggestFeatureFlags() {
		return (context, builder) -> SharedSuggestionProvider.suggest(DEFAULT.keySet(), builder);
	}
}
