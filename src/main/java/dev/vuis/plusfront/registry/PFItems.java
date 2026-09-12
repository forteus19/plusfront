package dev.vuis.plusfront.registry;

import com.boehmod.bflib.cloud.common.player.challenge.GunType;
import com.boehmod.blockfront.common.gun.GunCameraConfigs;
import com.boehmod.blockfront.common.gun.GunDamageConfigs;
import com.boehmod.blockfront.common.gun.GunFireConfig;
import com.boehmod.blockfront.common.gun.GunFireMode;
import com.boehmod.blockfront.common.gun.GunScopeConfig;
import com.boehmod.blockfront.common.gun.GunSoundConfig;
import com.boehmod.blockfront.common.gun.GunSpreadConfigs;
import com.boehmod.blockfront.common.gun.GunStaticReloadConfig;
import com.boehmod.blockfront.common.item.BFCommonItem;
import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.registry.BFSounds;
import com.boehmod.blockfront.util.math.ShakeNodePresets;
import dev.vuis.plusfront.PlusFront;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public final class PFItems {
	private static final DeferredRegister.Items DR = DeferredRegister.createItems(PlusFront.MOD_ID);

	public static final DeferredItem<GunItem> GUN_COWBOY_REVOLVER = DR.registerItem(
		"gun_cowboy_revolver",
		properties -> new GunItem("gun_cowboy_revolver", properties)
			.defaultMag(1, 0)
			.damage(GunDamageConfigs.WELROD)
			.sound(
				GunSoundConfig.DEFAULT.clone()
					.fire(BFSounds.ITEM_GUN_WEBLEY_MK6_FIRE)
					.dryFire(BFSounds.ITEM_GUN_SHARED_DRYFIRE_REVOLVER)
					.pre(BFSounds.ITEM_GUN_WEBLEY_MK6_PRE, true, 1)
					.reload(BFSounds.ITEM_GUN_WEBLEY_MK6_RELOAD)
					.corebassClose(BFSounds.ITEM_GUN_SHARED_COREBASS_CLOSE_PISTOL)
					.corebassDistant(BFSounds.ITEM_GUN_SHARED_COREBASS_PISTOL_DISTANT)
					.zoomIn(BFSounds.ITEM_GUN_SHARED_ZOOM_PISTOL_IN)
					.zoomOut(BFSounds.ITEM_GUN_SHARED_ZOOM_PISTOL_OUT)
					.bulletLand(
						BFSounds.ITEM_GUN_SHARED_BULLET_LMG,
						BFSounds.ITEM_GUN_SHARED_BULLET_LMG_WOOD,
						BFSounds.ITEM_GUN_SHARED_BULLET_LMG_METAL,
						BFSounds.ITEM_GUN_SHARED_BULLET_LMG_DIRT,
						BFSounds.ITEM_GUN_SHARED_BULLET_LMG_WATER
					)
					.echoDistant(
						BFSounds.ITEM_GUN_SHARED_ECHO_DISTANT_REVOLVER,
						BFSounds.ITEM_GUN_SHARED_ECHO_DISTANT_REVOLVER_STEREO
					)
					.arm(BFSounds.ITEM_GUN_SHARED_FOLEY_ARM_PISTOL)
					.lowFreq(BFSounds.ITEM_GUN_SHARED_LFE_PISTOL)
					.towards(BFSounds.ITEM_GUN_SHARED_TOWARDS_PISTOL)
					.lastRound(BFSounds.ITEM_GUN_SHARED_LASTROUND_PISTOL)
					.equip(BFSounds.ITEM_GUN_SHARED_FOLEY_EQUIP_REVOLVER)
					.method_3975(1.2f)
			)
			.shake(ShakeNodePresets.field_6670)
			.method_8888(true)
			.weight(0f)
			.gunType(GunType.PISTOL)
			.method_8880(0.35f)
			.camera(GunCameraConfigs.field_6769)
			.shake(ShakeNodePresets.GUN_WEBLEY)
			.secondary()
			.holdCloser()
			.scope(GunScopeConfig.Type.DEFAULT, new GunScopeConfig())
			.spread(GunSpreadConfigs.REVOLVER)
			.bulletEject("pistol")
			.reload(new GunStaticReloadConfig(4.45f))
			.fireModes(new GunFireConfig[]{new GunFireConfig(GunFireMode.SEMI, 20)}),
		new Item.Properties()
			.stacksTo(1)
			.component(DataComponents.TOOL, BFCommonItem.getTool())
	);

	private PFItems() {
		throw new AssertionError();
	}

	public static String fixInternalId(String originalId) {
		return switch (originalId) {
			case "gun_cowboy_revolver" -> "gun_webley_mk6";
			default -> originalId;
		};
	}

	public static void register(IEventBus modBus) {
		DR.register(modBus);
	}
}
