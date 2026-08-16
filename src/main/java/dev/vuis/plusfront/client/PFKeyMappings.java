package dev.vuis.plusfront.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class PFKeyMappings {
	private static final String CATEGORY_NAME = "key.categories.pf";

	public static KeyMapping showWaypoints;

	private PFKeyMappings() {
		throw new AssertionError();
	}

	public static void register(Consumer<KeyMapping> registrar) {
		registrar.accept(showWaypoints = new KeyMapping(
			"key.pf.showWaypoints",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_Z,
			CATEGORY_NAME
		));
	}
}
