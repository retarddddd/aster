package cx.kloinn.aster.mixin;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DeltaTracker.Timer.class)
public interface DeltaTrackerMixinAccessor {
    @Accessor("deltaTicks")
    float getDeltaTicks();

    @Accessor("deltaTicks")
    void setDeltaTicks(float value);
}
