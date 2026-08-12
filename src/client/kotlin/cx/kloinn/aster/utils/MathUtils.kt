package cx.kloinn.aster.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MathUtils {
    // Moves a player forward
    fun moveForward(yaw: Float, xxa: Float, zza: Float, handleKeyboard: Boolean = true): Vec3 {
        val yaw = Math.toRadians(yaw.toDouble())

        var forward = 0.0f
        var strafe = 0.0f

        if (handleKeyboard) {
            forward = zza
            strafe = xxa

            val length = sqrt(forward * forward + strafe * strafe)

            if (length > 1.0) {
                forward /= length
                strafe /= length
            }
        }

        val sin = sin(yaw)
        val cos = cos(yaw)

        // https://en.wikipedia.org/wiki/Rotation_matrix
        // https://matthew-brett.github.io/teaching/rotation_2d.html
        return Vec3(
            strafe * cos + forward * -sin,
            0.0,
            strafe * sin + forward * cos
        )
    }

    fun normalizeVelocity(value: Float): Float {
        if (value == 0.0f) {
            return 0.0f
        }

        return value / 100.0f
    }

    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    // Pair<Yaw, Pitch>
    fun calculateRotation(localPlayer: LocalPlayer, entity: Entity): Pair<Float, Float> {
        val source = localPlayer.eyePosition
        val target = entity.boundingBox.center

        val deltaX = target.x - source.x
        val deltaY = target.y - source.y
        val deltaZ = target.z - source.z

        val horizontalDistance = sqrt(deltaX * deltaX + deltaZ * deltaZ)

        val yaw = Math.toDegrees(atan2(deltaZ, deltaX)).toFloat() - 90.0f
        val pitch = -Math.toDegrees(atan2(deltaY, horizontalDistance)).toFloat()

        return yaw to pitch
    }

    fun isInFieldOfView(player: LocalPlayer, yaw: Float, pitch: Float): Boolean {
        val yawDiff = Mth.wrapDegrees(yaw - player.yRot)
        val pitchDiff = Mth.wrapDegrees(pitch - player.xRot)

        val angle = sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff)
        val fov = Minecraft.getInstance().options.fov().get().toFloat()

        return angle <= fov / 2.0f
    }
}