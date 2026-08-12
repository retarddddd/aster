package cx.kloinn.aster.module.impl.other

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.utils.CrossThreadQueue
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ServerboundPongPacket
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
import java.lang.reflect.Modifier

class PacketLogger : Module() {
    override val name = "Packet Logger"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.Other

    private val logReceiving = BoolSetting("Log Receiving", true)
    private val logSending = BoolSetting("Log Sending", true)
    private val ignoreSpam = BoolSetting("Ignore Spam", true)

    override val settings: ArrayList<Setting> = arrayListOf(logReceiving, logSending, ignoreSpam)

    private val queuedMessages = CrossThreadQueue<String>()

    override fun onPacketReceive(packet: Packet<*>): Boolean {
        if (logReceiving.value) {
            queuedMessages.add("[RECEIVED] ${this.dumpPacket(packet)}")
        }

        return false
    }

    override fun onPacketSend(packet: Packet<*>): Boolean {
        if (logSending.value) {
            if (ignoreSpam.value && (packet is ServerboundClientTickEndPacket || packet is ServerboundPongPacket)) {
                return false
            }

            queuedMessages.add("[SEND] ${this.dumpPacket(packet)}")
        }

        return false
    }

    override fun onTick() {
        queuedMessages.drain { message ->
            Minecraft.getInstance().player?.sendSystemMessage(Component.literal(message))
        }
    }

    private fun dumpPacket(packet: Packet<*>): String {
        val builder = StringBuilder()

        builder
            .append(packet.toString())
            .append(" - ")
            .append(packet.type())
            .append(" -\n")

        var clazz: Class<*>? = packet.javaClass

        // recursively go through every class and its parent and its parent's parent and so on so forth
        // until we no longer have anything to go through
        while (clazz != null && clazz != Any::class.java) {
            for (field in clazz.declaredFields) {
                field.isAccessible = true

                // ignore static fields... we don't care about those anyway
                if (Modifier.isStatic(field.modifiers)) continue

                val value = runCatching { field.get(packet) }
                    .getOrElse { "<error: ${it.message}>" }

                builder.append(" > ")
                    .append(field.name)
                    .append(" - ")
                    .append(value)
                    .append('\n')
            }

            clazz = clazz.superclass
        }

        return builder.toString()
    }
}