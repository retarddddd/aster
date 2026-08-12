package cx.kloinn.aster.utils

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor

object ChatUtils {
    fun sendMessage(msg: Component) {
        val lp = Minecraft.getInstance().player ?: return
        val component = Component.literal("▶ ")
            .withColor(TextColor.RED)
            .append(msg)

        lp.sendSystemMessage(component)
    }

    fun sendMessage(msg: String) {
        sendMessage(Component.literal(msg).withColor(TextColor.WHITE))
    }

    fun sendMessage(msg: String, color: TextColor) {
        sendMessage(Component.literal(msg).withColor(color))
    }

    fun sendSuccessMessage(msg: String) {
        sendMessage(Component.literal(msg).withColor(TextColor.GREEN))
    }

    fun sendErrorMessage(msg: String) {
        sendMessage(Component.literal(msg).withColor(TextColor.RED))
    }
}