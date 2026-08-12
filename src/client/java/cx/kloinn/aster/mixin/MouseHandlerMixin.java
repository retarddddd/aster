package cx.kloinn.aster.mixin;

import cx.kloinn.aster.client.AsterClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long window, MouseButtonInfo event, int action, CallbackInfo ci) {
        AsterClient.SINGLETON.getInputManager().receiveMouseButtonInput(action, event);
    }

    @Inject(method = "onMove", at = @At("HEAD"))
    private void onCursorMove(long window, double x, double y, CallbackInfo ci) {
        var mcWindow = Minecraft.getInstance().getWindow();

        // do the same thing the game does - see getScaledXPos
        AsterClient.SINGLETON.getInputManager().receiveCursorInput(
                x * (double)mcWindow.getGuiScaledWidth() / (double)mcWindow.getScreenWidth(),
                y * (double)mcWindow.getGuiScaledHeight() / (double)mcWindow.getScreenHeight()
        );
    }
}
