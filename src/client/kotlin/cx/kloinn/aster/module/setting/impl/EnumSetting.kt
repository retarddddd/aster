package cx.kloinn.aster.module.setting.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.module.setting.PrecondFunc
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.utils.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.reflect.KClass

class EnumSetting<T : Enum<T>>(override val settingName: String, enumClass: KClass<T>, defaultValue: T, override val precondFunc: PrecondFunc = { true }) : Setting(settingName, precondFunc) {
    val values: Array<T> = enumClass.java.enumConstants
    var selectedValue: T = defaultValue

    override fun render(graphics: GuiGraphicsExtractor, cursorX: Int, cursorY: Int, endCursorX: Int, endCursorY: Int) {
        val inputManager = AsterClient.SINGLETON.inputManager
        val mousePos = inputManager.getCursorPos()

        val font = Minecraft.getInstance().font

        val valueText = selectedValue.toString()

        val totalPad = getPaddingAfter() + getPaddingBefore()

        val textX = cursorX + 5
        val textY = endCursorY + 1 + (totalPad - font.lineHeight) / 2
        val valueX = endCursorX - 5 - font.width(valueText)

        val isHovered = mousePos.x >= cursorX && mousePos.x < endCursorX && mousePos.y >= endCursorY && mousePos.y < endCursorY + totalPad

        if (isHovered) {
            val idx = selectedValue.ordinal

            if (inputManager.isLeftClickingFirstTime()) {
                val nextVal = idx + 1

                if (nextVal >= values.size) {
                    selectedValue = values[0]
                } else {
                    selectedValue = values[nextVal]
                }
            }

            if (inputManager.isRightClickingFirstTime()) {
                val nextVal = idx - 1

                if (nextVal < 0) {
                    selectedValue = values[values.size - 1]
                } else {
                    selectedValue = values[nextVal]
                }
            }
        }

        graphics.text(
            font,
            settingName,
            textX,
            textY,
            Color.toArgb(0xFFFFFF)
        )

        graphics.text(
            font,
            valueText,
            valueX,
            textY,
            Color.toArgb(0x808080)
        )
    }

    override fun getPaddingBefore(): Int {
        return 5
    }

    override fun getPaddingAfter(): Int {
        return 6
    }
}