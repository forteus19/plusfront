package dev.vuis.plusfront.world;

import dev.vuis.plusfront.util.index.FeatureFlagIndex;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public final class PFSavedData extends SavedData {
	private static final Factory<PFSavedData> FACTORY = new Factory<>(PFSavedData::new, PFSavedData::load);
	private static final String NAME = "plusfront";

	private final Object2BooleanMap<String> featureFlags;

	private PFSavedData(Object2BooleanMap<String> featureFlags) {
		this.featureFlags = featureFlags;
	}

	private PFSavedData() {
		this.featureFlags = FeatureFlagIndex.mutableDefault();
	}

	public static PFSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
	}

	public Object2BooleanMap<String> getFeatureFlags() {
		return featureFlags;
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider lookupProvider) {
		CompoundTag featureFlagsTag = new CompoundTag(featureFlags.size());
		for (Object2BooleanMap.Entry<String> featureFlag : featureFlags.object2BooleanEntrySet()) {
			featureFlagsTag.putBoolean(featureFlag.getKey(), featureFlag.getBooleanValue());
		}
		tag.put("featureFlags", featureFlagsTag);

		return tag;
	}

	public static @NotNull PFSavedData load(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider lookupProvider) {
		Object2BooleanMap<String> featureFlags;
		if (tag.contains("featureFlags", Tag.TAG_COMPOUND)) {
			CompoundTag featureFlagsTag = tag.getCompound("featureFlags");

			featureFlags = new Object2BooleanOpenHashMap<>(FeatureFlagIndex.DEFAULT.size());

			for (Map.Entry<String, Boolean> defaultEntry : FeatureFlagIndex.DEFAULT.entrySet()) {
				String key = defaultEntry.getKey();

				if (featureFlagsTag.contains(key, Tag.TAG_BYTE)) {
					featureFlags.put(key, featureFlagsTag.getBoolean(key));
				} else {
					featureFlags.put(key, defaultEntry.getValue().booleanValue());
				}
			}
		} else {
			featureFlags = FeatureFlagIndex.mutableDefault();
		}

		return new PFSavedData(featureFlags);
	}
}
