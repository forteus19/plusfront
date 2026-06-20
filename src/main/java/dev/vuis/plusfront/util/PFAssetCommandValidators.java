package dev.vuis.plusfront.util;

import com.boehmod.blockfront.assets.AssetCommandValidator;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import org.jetbrains.annotations.NotNull;

public final class PFAssetCommandValidators {
	private PFAssetCommandValidators() {
		throw new AssertionError();
	}

	public static @NotNull AssetCommandValidator count(String... argNames) {
		return AssetCommandValidators.count(argNames);
	}
}
