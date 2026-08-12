package cx.kloinn.aster.mixin;


import cx.kloinn.aster.client.AsterClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "move", at = @At("HEAD"))
    private void move(final MoverType moverType, final Vec3 delta, CallbackInfo ci) {
        var moduleManager = AsterClient.SINGLETON.getModuleManager();

        for (var module : moduleManager.getModules()) {
            if (!module.getEnabled()) continue;

            module.onMove$org_aconite_aster_client();
        }
    }
}
