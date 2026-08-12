package cx.kloinn.aster.module.impl.visual

import cx.kloinn.aster.client.GameHelpers
import cx.kloinn.aster.client.minus
import cx.kloinn.aster.client.pos
import cx.kloinn.aster.client.previousPos
import cx.kloinn.aster.client.previousVelocity
import cx.kloinn.aster.client.velocity
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.utils.Color
import cx.kloinn.aster.utils.TimerUtils
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

class LevelInfo : Module() {
    override val name = "Level Info"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Visual
    override val settings: ArrayList<Setting> = arrayListOf()

    private var text = ""

    override fun onHudRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        val client = Minecraft.getInstance()

        if (client.player == null) {
            return
        }

        val screen = client.gui.screen()

        if (screen is ChatScreen) {
            return
        }

        val window = client.window
        val font = client.font

        val height = window.guiScaledHeight

        graphics.text(
            font,
            text,
            2,
            height - font.lineHeight - 1,
            Color.toArgb(0xFFFFFF)
        )
    }

    val bpses: ArrayList<Double> = arrayListOf()
    var ticks = 0
    var averageBps: Double = 0.0

    override fun onTick() {
        val client = Minecraft.getInstance()
        val player = client.player

        if (player == null) {
            text = ""
            return
        }

        ticks++

        if (player.previousPos != null) {
            val pos = player.pos

            val posNoY = Vec3(pos.x, 0.0, pos.z)
            val previousPosNoY = Vec3(player.previousPos!!.x, 0.0, player.previousPos!!.z)

            val bps = posNoY.distanceTo(previousPosNoY) * (20.0 * TimerUtils.speedMultiplier)
            bpses.plusAssign(bps)

            if (ticks % 20 == 0) {
                averageBps = bpses.sumOf { it } / bpses.size
                bpses.clear()
            }

            text = "XYZ: ${pos.x.roundToInt()} ${pos.y.roundToInt()} ${pos.z.roundToInt()} | BPS: ${"%.2f".format(bps)} (average: ${"%.2f".format(averageBps)})"
        }
    }
}
