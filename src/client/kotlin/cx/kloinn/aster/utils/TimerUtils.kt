package cx.kloinn.aster.utils

import cx.kloinn.aster.mixin.DeltaTrackerMixinAccessor
import net.minecraft.client.Minecraft

// This game is retarded
object TimerUtils {
    var speedMultiplier: Float = 1.0f

    fun getDeltaAccessor(): DeltaTrackerMixinAccessor {
        val dt = Minecraft.getInstance().deltaTracker
        val dtAccessor = dt as DeltaTrackerMixinAccessor

        return dtAccessor
    }

    fun tick() {
        this.getDeltaAccessor().deltaTicks *= speedMultiplier
    }
}