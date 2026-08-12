package cx.kloinn.aster.module.setting.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.module.setting.PrecondFunc
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.utils.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

class BoolSetting(override val settingName: String, defaultValue: Boolean, override val precondFunc: PrecondFunc = { true }) : Setting(settingName, precondFunc) {
    var value: Boolean = defaultValue

    override fun render(graphics: GuiGraphicsExtractor, cursorX: Int, cursorY: Int, endCursorX: Int, endCursorY: Int) {
        val font = Minecraft.getInstance().font

        val client = AsterClient.SINGLETON
        val inputManager = client.inputManager
        val mousePos = client.inputManager.getCursorPos()

        val totalPad = getPaddingAfter() + getPaddingBefore()

        val checkboxX = endCursorX - 5 - 10
        val checkboxY = endCursorY + (totalPad - 10) / 2

        if (
            mousePos.x >= cursorX && mousePos.x < endCursorX &&
            mousePos.y >= endCursorY && mousePos.y < endCursorY + totalPad
        ) {
            if (inputManager.isLeftClickingFirstTime() || inputManager.isRightClickingFirstTime()) {
                value = !value
            }
        }

        graphics.fill(
            checkboxX,
            checkboxY,
            checkboxX + 10,
            checkboxY + 10,
            Color.toArgb(0x212121)
        )

        if (value) {
            graphics.fill(
                checkboxX + 2,
                checkboxY + 2,
                checkboxX + 10 - 2,
                checkboxY + 10 - 2,
                Color.toArgb(0xFFFFFF)
            )
        }

        graphics.text(
            font,
            settingName,
            cursorX + 5,
            endCursorY + 1 + (totalPad - font.lineHeight) / 2,
            Color.toArgb(0xFFFFFF)
        )
    }

    override fun getPaddingBefore(): Int {
        return 5
    }

    override fun getPaddingAfter(): Int {
        return 7
    }
}