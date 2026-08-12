package cx.kloinn.aster.mixin;


import cx.kloinn.aster.client.AsterClient;
import cx.kloinn.aster.module.impl.combat.Reach;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void entityInteractionRange(CallbackInfoReturnable<Double> cir) {
        if ((Object) this instanceof LocalPlayer) {
            var reachMod = (Reach) AsterClient.Companion.getSINGLETON().getModuleManager().getModule("Reach");

            if (!reachMod.getEnabled()) return;

            cir.setReturnValue((double) reachMod.getCombatReach().getValue());
        }
    }

    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void blockInteractionRange(CallbackInfoReturnable<Double> cir) {
        if ((Object) this instanceof LocalPlayer) {
            var reachMod = (Reach) AsterClient.Companion.getSINGLETON().getModuleManager().getModule("Reach");

            if (!reachMod.getEnabled()) return;

            cir.setReturnValue((double) reachMod.getBlockReach().getValue());
        }
    }
}
