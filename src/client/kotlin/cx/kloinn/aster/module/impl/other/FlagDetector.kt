package cx.kloinn.aster.module.impl.other

import cx.kloinn.aster.client.pos
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.utils.ChatUtils
import cx.kloinn.aster.utils.CrossThreadQueue
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

class FlagDetector : Module() {
    override val name = "Flag Detector"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.Other

    override val settings: ArrayList<Setting> = arrayListOf()

    private val queuedMessages = CrossThreadQueue<String>()

    override fun onPacketReceive(packet: Packet<*>): Boolean {
        if (packet is ClientboundPlayerPositionPacket) {
            val teleportId = packet.id
            val pos = packet.change.position
            val velocity = packet.change.deltaMovement
            val rotationX = packet.change.yRot
            val rotationY = packet.change.xRot

            val lpPos = Minecraft.getInstance().player?.pos ?: return false

            val distance = pos.distanceTo(lpPos)

            // Lagbacks on most anticheats teleport the player 1–4 blocks from their actual position, rarely farther (e.g., 10 blocks).
            // Anything beyond 10 blocks is almost certainly a teleport by a plugin or command rather than an anticheat lagback.
            val label = if (distance < 10) {
                "lagback"
            } else {
                "teleport"
            }

            queuedMessages.add("Detected $label: $teleportId. pos=$pos; velo=$velocity; yaw=$rotationX; pitch=$rotationY")
        }

        return false
    }

    override fun onTick() {
        queuedMessages.drain { msg ->
            ChatUtils.sendMessage(msg)
        }
    }
}
