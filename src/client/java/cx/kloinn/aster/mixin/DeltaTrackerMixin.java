package cx.kloinn.aster.mixin;

import cx.kloinn.aster.utils.TimerUtils;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public abstract class DeltaTrackerMixin {
    @Inject(
            method = "advanceGameTime",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/DeltaTracker$Timer;deltaTicks:F",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void advanceGameTimeMX(
            long currentMs,
            CallbackInfoReturnable<Integer> cir
    ) {
        TimerUtils.INSTANCE.tick();
    }
}