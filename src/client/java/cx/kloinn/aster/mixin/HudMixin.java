package cx.kloinn.aster.mixin;

import cx.kloinn.aster.client.AsterClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        var moduleManager = AsterClient.SINGLETON.getModuleManager();

        for (var module : moduleManager.getModules()) {
            if (!module.getEnabled()) continue;

            module.onHudRender$org_aconite_aster_client(graphics, deltaTracker);
        }
    }

    @Inject(
            method = "extractScoreboardSidebar",
            at = @At("HEAD"),
            cancellable = true
    )
    public void extractScoreboardSidebar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        var moduleManager =  AsterClient.SINGLETON.getModuleManager();

        for (var module : moduleManager.getModules()) {
            if (!module.getEnabled()) continue;

            if (module.onScoreboardRender$org_aconite_aster_client(graphics, deltaTracker)) {
                ci.cancel();
                return;
            }
        }
    }
}