package dev.vuis.plusfront.util.index;

import com.boehmod.blockfront.client.ac.BFClientAntiCheat;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public final class FeatureFlagIndex {
	public static final String
		EVENT_HALLOWEEN = "event_halloween",
		EVENT_CHRISTMAS = "event_christmas",
		SERVER_SHOT_VALIDATION = "server_shot_validation",
		SERVER_SHOT_VALIDATION_SPREAD = "server_shot_validation_spread",
		SERVER_SHOT_VALIDATION_KICK = "server_shot_validation_kick",
		SERVER_SHOT_VALIDATION_REPORT = "server_shot_validation_report",
		SERVER_SHOT_VALIDATION_AIM = "server_shot_validation_aim",
		SERVER_MATCH_FEATURE_PING = "server_match_feature_ping",
		SERVER_GRENADE_COOK_ON_DEATH = "server_grenade_cook_drop_on_death",
		CLIENT_VEIL_FANCY_GUN_LIGHT = "client_veil_fancy_gun_light",
		SERVER_PLAYER_VOICE_SOUNDS = "server_player_voice_sounds",
		SERVER_CHAT_MARKDOWN = "server_chat_markdown",
		SERVER_OBJECTIVE_REWARDS = "server_objective_rewards",
		SERVER_SKILL_BALANCING = "server_skill_balancing",
		CLIENT_AC_LAYER_PREFIX = "client_ac_layer_";

	public static final Map<String, Boolean> DEFAULT;

	static {
		var tempDefault = ImmutableMap.<String, Boolean>builder()
			.put(EVENT_HALLOWEEN, false)
			.put(EVENT_CHRISTMAS, false)
			.put(SERVER_SHOT_VALIDATION, true)
			.put(SERVER_SHOT_VALIDATION_SPREAD, true)
			.put(SERVER_SHOT_VALIDATION_KICK, false)
			.put(SERVER_SHOT_VALIDATION_REPORT, false)
			.put(SERVER_SHOT_VALIDATION_AIM, false)
			.put(SERVER_MATCH_FEATURE_PING, false)
			.put(SERVER_GRENADE_COOK_ON_DEATH, true)
			.put(CLIENT_VEIL_FANCY_GUN_LIGHT, false)
			.put(SERVER_PLAYER_VOICE_SOUNDS, true)
			.put(SERVER_CHAT_MARKDOWN, true)
			.put(SERVER_OBJECTIVE_REWARDS, false)
			.put(SERVER_SKILL_BALANCING, false);

		for (int i = 1; i <= BFClientAntiCheat.ID_MAX; i++) {
			tempDefault.put(CLIENT_AC_LAYER_PREFIX + i, false);
		}

		DEFAULT = tempDefault.buildOrThrow();
	}

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
