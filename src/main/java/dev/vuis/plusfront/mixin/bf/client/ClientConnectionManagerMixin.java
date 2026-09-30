package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.net.ConnectionMode;
import com.boehmod.blockfront.cloud.client.ClientConnectionManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientConnectionManager.class)
public abstract class ClientConnectionManagerMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "FIELD",
			target = "Lcom/boehmod/blockfront/client/net/ConnectionMode;UNDECIDED:Lcom/boehmod/blockfront/client/net/ConnectionMode;",
			opcode = Opcodes.GETSTATIC,
			ordinal = 0
		)
	)
	private ConnectionMode fixDefaultMode() {
		return ConnectionMode.OFFLINE;
	}
}
