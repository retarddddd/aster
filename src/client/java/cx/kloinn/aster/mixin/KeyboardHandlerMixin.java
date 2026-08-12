package cx.kloinn.aster.mixin;

import cx.kloinn.aster.client.AsterClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(
            method = "keyPress",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (AsterClient.SINGLETON.getInputManager().receiveKeyboardInput(action, event)) {
            ci.cancel();
        }
    }
}
