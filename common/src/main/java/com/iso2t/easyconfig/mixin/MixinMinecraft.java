package com.iso2t.easyconfig.mixin;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.platform.Services;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init (CallbackInfo info) {
		Constants.LOG.info("Loaded {} ({}) for Minecraft {} ({})", Constants.MOD_NAME, Constants.MOD_ID, SharedConstants.getCurrentVersion().name(), Services.PLATFORM.getPlatformName());
	}
}