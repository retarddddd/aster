package cx.kloinn.aster.module.impl.movement

import cx.kloinn.aster.client.velocity
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.impl.visual.ClickGUI.EnabledModuleStyle
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.ChatUtils
import cx.kloinn.aster.utils.CrossThreadQueue
import cx.kloinn.aster.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.world.phys.Vec3

class Velocity : Module() {
    override val name = "Velocity"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Movement

    enum class VelocityMode {
        Modify,
        Cancel
    }

    private val modeSetting = EnumSetting("Mode", VelocityMode::class, VelocityMode.Modify)

    private val horizontal = FloatSetting("Horizontal", 0.0f, 0.0f, 100.0f, { modeSetting.selectedValue == VelocityMode.Modify })
    private val vertical = FloatSetting("Vertical", 0.0f, 0.0f, 100.0f, { modeSetting.selectedValue == VelocityMode.Modify })

    private val ignoreIfLargeVelocity = BoolSetting("Ignore if large", false)
    private val ignoreExplosions = BoolSetting("Ignore explosions", false)
    private val debug = BoolSetting("Debug", false)

    override val settings: ArrayList<Setting> = arrayListOf(modeSetting, horizontal, vertical, ignoreIfLargeVelocity, ignoreExplosions, debug)

    private val messageQueue = CrossThreadQueue<String>()

    override fun onPacketReceive(packet: Packet<*>): Boolean {
        val localPlayer = Minecraft.getInstance().player ?: return false

        if (packet is ClientboundSetEntityMotionPacket) {
            if (packet.id != localPlayer.id) {
                return false
            }

            val x = packet.movement.x
            val y = packet.movement.y
            val z = packet.movement.z

            if (debug.value) {
                messageQueue.add("[DEBUG] Velocity: $x, $y, $z")
            }

            return this.handleVelo(localPlayer, x, y, z)
        }

        if (packet is ClientboundExplodePacket) {
            if (ignoreExplosions.value) {
                return false
            }

            if (!packet.playerKnockback.isEmpty) {
                val kb = packet.playerKnockback.get()

                val x = kb.x()
                val y = kb.y()
                val z = kb.z()

                if (debug.value) {
                    messageQueue.add("[DEBUG] Explosion velocity: $x, $y, $z")
                }

                return this.handleVelo(localPlayer, x, y, z)
            }
        }

        return false
    }

    private fun handleVelo(localPlayer: LocalPlayer, x: Double, y: Double, z: Double): Boolean {
        if (ignoreIfLargeVelocity.value && (x > 1 || y > 1 || z > 1)) {
            localPlayer.velocity = Vec3(x, y, z)
            return false
        }

        when (modeSetting.selectedValue) {
            VelocityMode.Modify -> {
                localPlayer.velocity = Vec3(
                    x * MathUtils.normalizeVelocity(horizontal.value),
                    y * MathUtils.normalizeVelocity(vertical.value),
                    z * MathUtils.normalizeVelocity(horizontal.value)
                )

                return true
            }

            VelocityMode.Cancel -> {
                return true
            }
        }
    }

    override fun onTick() {
        messageQueue.drain {
            ChatUtils.sendMessage(it)
        }
    }
}