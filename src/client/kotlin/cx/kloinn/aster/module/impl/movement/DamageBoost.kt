package cx.kloinn.aster.module.impl.movement

import cx.kloinn.aster.client.velocity
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.impl.visual.ClickGUI.EnabledModuleStyle
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.world.phys.Vec3

class DamageBoost : Module() {
    override val name = "Damage Boost"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Movement

    private val horizontal = FloatSetting("Horizontal", 100.0f, 100.0f, 200.0f)
    private val vertical = FloatSetting("Vertical", 100.0f, 100.0f, 200.0f)

    override val settings: ArrayList<Setting> = arrayListOf(horizontal, vertical)

    override fun onPacketReceive(packet: Packet<*>): Boolean {
        val localPlayer = Minecraft.getInstance().player ?: return false

        if (packet is ClientboundSetEntityMotionPacket) {
            if (packet.id != localPlayer.id) {
                return false
            }

            val x = packet.movement.x
            val y = packet.movement.y
            val z = packet.movement.z

            localPlayer.velocity = Vec3(
                x * MathUtils.normalizeVelocity(horizontal.value),
                y * MathUtils.normalizeVelocity(vertical.value),
                z * MathUtils.normalizeVelocity(horizontal.value)
            )

            return true
        }

        if (packet is ClientboundExplodePacket) {
            if (!packet.playerKnockback.isEmpty) {
                val kb = packet.playerKnockback.get()

                val x = kb.x()
                val y = kb.y()
                val z = kb.z()

                localPlayer.velocity = Vec3(
                    x * MathUtils.normalizeVelocity(horizontal.value),
                    y * MathUtils.normalizeVelocity(vertical.value),
                    z * MathUtils.normalizeVelocity(horizontal.value)
                )

                return true
            }
        }

        return false
    }
}