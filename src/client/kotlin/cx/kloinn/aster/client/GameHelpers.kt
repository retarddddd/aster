package cx.kloinn.aster.client

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

// Data class that stores a bunch of commonly accessed data.
class GameHelpers {
    var ticksOffGround = 0
    var ticksOnGround = 0

    var previousVelo: Vec3? = null
    var currentVelo: Vec3? = null

    var currentPos: Vec3? = null
    var previousPos: Vec3? = null

    fun tick() {
        val localPlayer = Minecraft.getInstance().player ?: return

        previousVelo = currentVelo
        currentVelo = localPlayer.deltaMovement

        previousPos = currentPos
        currentPos = localPlayer.position()

        if (localPlayer.onGround()) {
            ticksOnGround++
            ticksOffGround = 0
        } else {
            ticksOffGround++
            ticksOnGround = 0
        }
    }
}

// I'm usually against using extension functions to rename variables; however, this is going to be an exception.
// "deltaMovement" is an utterly retarded name. "velocity" is much better because it explains what this field does
// and is familiar to everybody
var Entity.velocity: Vec3
    inline get() {
        return this.deltaMovement
    }
    inline set(value) {
        this.deltaMovement = value
    }

val LocalPlayer.previousVelocity: Vec3?
    get() {
        val gameHelpers = AsterClient.SINGLETON.gameHelpers
        return gameHelpers.previousVelo
    }

var Entity.pos: Vec3
    inline get() {
        return this.position()
    }
    set(value) {
        this.setPos(value)
    }

val LocalPlayer.previousPos: Vec3?
    get() {
        val gameHelpers = AsterClient.SINGLETON.gameHelpers
        return gameHelpers.previousPos
    }

val LocalPlayer.moveDelta: Vec3?
    get() {
        return this.previousPos?.minus(this.pos)
    }

var Entity.yaw: Float
    inline get() {
        return this.yRot
    }
    inline set(value) {
        this.yRot = value
    }

var Entity.pitch: Float
    inline get() {
        return this.xRot
    }
    inline set(value) {
        this.xRot = value
    }

val LocalPlayer.isUsingKeyboardMoveKeys: Boolean
    get() {
        val mc = Minecraft.getInstance()
        return mc.options.keyUp.isDown || mc.options.keyDown.isDown || mc.options.keyLeft.isDown || mc.options.keyRight.isDown || mc.options.keyJump.isDown
    }

val LocalPlayer.ticksOnGround: Int
    get() {
        return AsterClient.SINGLETON.gameHelpers.ticksOnGround
    }

val LocalPlayer.ticksOffGround: Int
    get() {
        return AsterClient.SINGLETON.gameHelpers.ticksOffGround
    }

val LocalPlayer.movingHorizontally: Boolean
    get() {
        return deltaMovement.horizontalDistanceSqr() > 0.001
    }

val LocalPlayer.movingVertically: Boolean
    get() {
        return deltaMovement.y > 0.001
    }

operator fun Vec3.times(value: Float): Vec3 {
    return Vec3(this.x * value, this.y * value, this.z * value)
}

operator fun Vec3.div(value: Float): Vec3 {
    return Vec3(this.x / value, this.y / value, this.z / value)
}

operator fun Vec3.minus(value: Vec3): Vec3 {
    return Vec3(this.x - value.x, this.y - value.y, this.z - value.z)
}

operator fun Vec3.plus(value: Vec3): Vec3 {
    return Vec3(this.x + value.x, this.y + value.y, this.z + value.z)
}