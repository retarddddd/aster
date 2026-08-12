package cx.kloinn.aster.utils

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.world.entity.player.Input

data class RotModifier(val yaw: Float? = null, val pitch: Float? = null, val cancel: Boolean = false)
data class PosModifier(val x: Double? = null, val y: Double? = null, val z: Double? = null, val cancel: Boolean = false)
data class OnGroundModifier(val onGround: Boolean? = null, val cancel: Boolean = false)
data class HorizontallyCollidingModifier(val horizontallyColliding: Boolean? = null, val cancel: Boolean = false)
data class InputModifier(val forward: Boolean? = null, val backward: Boolean? = null, val left: Boolean? = null, val right: Boolean? = null, val jump: Boolean? = null, val shift: Boolean? = null, val sprint: Boolean? = null, val cancel: Boolean = false)

class PacketRewriter(
    val onRot: (Float, Float) -> RotModifier = { _, _ -> RotModifier(null, null, false) },
    val onPos: (Double, Double, Double) -> PosModifier = { _, _, _ -> PosModifier(null, null, null, false) },
    val onIsOnGround: (Boolean) -> OnGroundModifier = { _ -> OnGroundModifier(null, false) },
    val onIsHorizontallyColliding: (Boolean) -> HorizontallyCollidingModifier = { _ -> HorizontallyCollidingModifier(null, false) },
    val onInput: (Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> InputModifier = { _, _, _, _, _, _, _ -> InputModifier(null, null, null, null, null, null, null, false) },
    val rewriteInputPacket: (packet: ServerboundPlayerInputPacket) -> Boolean = { false },
    val rewriteMovementPacket: (packet: ServerboundMovePlayerPacket) -> Boolean = { false }
) {
    private var acceptNextPk = false

    fun onPacketSend(packet: Packet<*>): Boolean {
        val localPlayer = Minecraft.getInstance().player ?: return false

        if (packet is ServerboundMovePlayerPacket) {
            if (acceptNextPk) {
                acceptNextPk = false
                return false
            }

            val shouldCancel = this.rewriteMovementPacket(packet)

            if (shouldCancel) {
                return true
            }

            if (packet is ServerboundMovePlayerPacket.Rot) {
                val pitch = packet.getXRot(0F)
                val yaw = packet.getYRot(0F)

                val modifiedRot = this.onRot(yaw, pitch)

                if (modifiedRot.cancel) {
                    return true
                }

                if (modifiedRot.yaw != null || modifiedRot.pitch != null) {
                    val modifiedOnGround = this.onIsOnGround(packet.isOnGround)
                    if (modifiedOnGround.cancel) { return true }

                    val modifiedHorizontallyColliding = this.onIsHorizontallyColliding(packet.horizontalCollision())
                    if (modifiedHorizontallyColliding.cancel) { return true }

                    acceptNextPk = true
                    localPlayer.connection.send(
                        ServerboundMovePlayerPacket.Rot(
                            modifiedRot.yaw ?: yaw,
                            modifiedRot.pitch ?: pitch,
                            modifiedOnGround.onGround ?: packet.isOnGround,
                            modifiedHorizontallyColliding.horizontallyColliding ?: packet.horizontalCollision()
                        )
                    )

                    return true
                }
            }

            if (packet is ServerboundMovePlayerPacket.Pos) {
                val x = packet.getX(0.0)
                val y = packet.getY(0.0)
                val z = packet.getZ(0.0)

                val modifiedPos = this.onPos(x, y, z)

                if (modifiedPos.cancel) {
                    return true
                }

                if (modifiedPos.x != null || modifiedPos.y != null || modifiedPos.z != null) {
                    val modifiedOnGround = this.onIsOnGround(packet.isOnGround)
                    if (modifiedOnGround.cancel) { return true }

                    val modifiedHorizontallyColliding = this.onIsHorizontallyColliding(packet.horizontalCollision())
                    if (modifiedHorizontallyColliding.cancel) { return true }

                    acceptNextPk = true
                    localPlayer.connection.send(
                        ServerboundMovePlayerPacket.Pos(
                            modifiedPos.x ?: x,
                            modifiedPos.y ?: y,
                            modifiedPos.z ?: z,
                            modifiedOnGround.onGround ?: packet.isOnGround,
                            modifiedHorizontallyColliding.horizontallyColliding ?: packet.horizontalCollision()
                        )
                    )

                    return true
                }
            }

            if (packet is ServerboundMovePlayerPacket.PosRot) {
                val x = packet.getX(0.0)
                val y = packet.getY(0.0)
                val z = packet.getZ(0.0)

                val pitch = packet.getXRot(0F)
                val yaw = packet.getYRot(0F)

                val modifiedPos = this.onPos(x, y, z)

                if (modifiedPos.cancel) {
                    return true
                }

                val modifiedRot = this.onRot(yaw, pitch)

                if (modifiedRot.cancel) {
                    return true
                }

                if (
                    modifiedPos.x != null ||
                    modifiedPos.y != null ||
                    modifiedPos.z != null ||
                    modifiedRot.yaw != null ||
                    modifiedRot.pitch != null
                ) {
                    val modifiedOnGround = this.onIsOnGround(packet.isOnGround)
                    if (modifiedOnGround.cancel) { return true }

                    val modifiedHorizontallyColliding = this.onIsHorizontallyColliding(packet.horizontalCollision())
                    if (modifiedHorizontallyColliding.cancel) { return true }

                    acceptNextPk = true
                    localPlayer.connection.send(
                        ServerboundMovePlayerPacket.PosRot(
                            modifiedPos.x ?: x,
                            modifiedPos.y ?: y,
                            modifiedPos.z ?: z,
                            modifiedRot.yaw ?: yaw,
                            modifiedRot.pitch ?: pitch,
                            modifiedOnGround.onGround ?: packet.isOnGround,
                            modifiedHorizontallyColliding.horizontallyColliding ?: packet.horizontalCollision()
                        )
                    )

                    return true
                }
            }
        }

        if (packet is ServerboundPlayerInputPacket) {
            if (acceptNextPk) {
                acceptNextPk = false
                return false
            }

            val shouldCancel = this.rewriteInputPacket(packet)

            if (shouldCancel) {
                return true
            }

            val input = packet.input

            val modifiedInput = this.onInput(input.forward, input.backward, input.left, input.right, input.jump, input.shift, input.sprint)

            if (modifiedInput.cancel) {
                return true
            }

            if (
                modifiedInput.forward != null ||
                modifiedInput.backward != null ||
                modifiedInput.left != null ||
                modifiedInput.right != null ||
                modifiedInput.jump != null ||
                modifiedInput.shift != null ||
                modifiedInput.sprint != null
            ) {
                acceptNextPk = true
                localPlayer.connection.send(
                    ServerboundPlayerInputPacket(
                        Input(
                            modifiedInput.forward ?: packet.input.forward,
                            modifiedInput.backward ?: packet.input.backward,
                            modifiedInput.left ?: packet.input.left,
                            modifiedInput.right ?: packet.input.right,
                            modifiedInput.jump ?: packet.input.jump,
                            modifiedInput.shift ?: packet.input.shift,
                            modifiedInput.sprint ?: packet.input.sprint,
                        )
                    )
                )
                return true
            }
        }

        return false
    }
}