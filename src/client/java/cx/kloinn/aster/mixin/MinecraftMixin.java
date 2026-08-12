package cx.kloinn.aster.mixin;

import cx.kloinn.aster.client.AsterClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void runTick(final boolean advanceGameTime, CallbackInfo ci) {
        AsterClient.SINGLETON.inputTick();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        AsterClient.SINGLETON.tick();
    }
}
