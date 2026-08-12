package cx.kloinn.aster.module.setting.impl

import com.mojang.blaze3d.platform.InputConstants
import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.module.impl.visual.ClickGUI
import cx.kloinn.aster.module.setting.PrecondFunc
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.utils.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

class FloatSetting(override val settingName: String, val defaultValue: Float, val min: Float = 0f, val max: Float = 100f, override val precondFunc: PrecondFunc = { true }) : Setting(settingName, precondFunc) {
    var value: Float = defaultValue

    companion object {
        private const val TYPED_VALUE_DEFAULT = "..."
        private var ACTIVE_TYPING_INSTANCE: FloatSetting? = null
    }

    private var dragging = false

    private var isTyping = false
    private var typedValue = TYPED_VALUE_DEFAULT

    override fun render(graphics: GuiGraphicsExtractor, cursorX: Int, cursorY: Int, endCursorX: Int, endCursorY: Int) {
        val font = Minecraft.getInstance().font

        val inputManager = AsterClient.SINGLETON.inputManager
        val mousePos = inputManager.getCursorPos()

        val labelX = cursorX + 5
        val labelY = endCursorY + 3

        val barWidth = ClickGUI.CATEGORY_SIZE.x - 10
        val barY = labelY + 10

        val isBarHovered = mousePos.x >= labelX && mousePos.x <= labelX + barWidth && mousePos.y >= barY - 2 && mousePos.y <= barY + 5
        val isBarHoveredExpanded = mousePos.x >= labelX - 10 && mousePos.x <= labelX + barWidth + 10 && mousePos.y >= barY - 10 && mousePos.y <= barY + 10

        if (!inputManager.isLeftClicking() || !isBarHoveredExpanded) {
            dragging = false
        } else if (dragging || isBarHovered) {
            dragging = true

            val progress = ((mousePos.x - labelX).toFloat() / barWidth).coerceIn(0f, 1f)
            value = min + progress * (max - min)
        }

        // text
        graphics.text(
            font,
            settingName,
            labelX,
            labelY,
            Color.toArgb(0xFFFFFF)
        )

        // value text
        var valueText = "%.2f".format(value)

        if (isTyping) {
            valueText = typedValue
        }

        val valueX = labelX + ClickGUI.CATEGORY_SIZE.x - 5 - 5 - font.width(valueText)

        val valueTextWidth = font.width(valueText)
        val isValueHovered = mousePos.x >= valueX && mousePos.x <= valueX + valueTextWidth && mousePos.y >= labelY && mousePos.y < labelY + font.lineHeight

        if (isValueHovered && inputManager.isLeftClickingFirstTime()) {
            ACTIVE_TYPING_INSTANCE?.stopTyping()
            ACTIVE_TYPING_INSTANCE = this
            isTyping = true
        }

        graphics.text(
            font,
            valueText,
            valueX,
            labelY,
            Color.toArgb(0x808080)
        )

        // progress bar
        graphics.fill(
            labelX,
            barY,
            labelX + barWidth,
            barY + 3,
            Color.toArgb(0xFFFFFF)
        )

        // filler for progress bar
        val filledWidth =
            ((value - min) / (max - min) * barWidth).coerceIn(0f, barWidth.toFloat()).toInt()

        graphics.fill(
            labelX,
            barY,
            labelX + filledWidth,
            barY + 3,
            Color.toArgb(0x123456)
        )
    }

    override fun onClickGuiClosed() {
        dragging = false
        stopTyping(true)
    }

    private fun stopTyping(resetTypedValue: Boolean = false) {
        isTyping = false

        if (resetTypedValue) {
            typedValue = TYPED_VALUE_DEFAULT
        }

        if (ACTIVE_TYPING_INSTANCE === this) {
            ACTIVE_TYPING_INSTANCE = null
        }
    }

    override fun onKeyInput(action: Int, event: KeyEvent) {
        if (!isTyping || action == InputConstants.PRESS) {
            return
        }

        if (event.key == GLFW.GLFW_KEY_BACKSPACE) {
            if (typedValue != TYPED_VALUE_DEFAULT && typedValue.isNotEmpty()) {
                typedValue = typedValue.dropLast(1)
            }

            return
        }

        if (event.key == GLFW.GLFW_KEY_ENTER) {
            val typedNumber = typedValue.toFloatOrNull()

            if (typedNumber != null && typedNumber in min..max) {
                value = typedNumber
                typedValue = value.toString()
            } else {
                typedValue = TYPED_VALUE_DEFAULT
                value = defaultValue
            }

            stopTyping()
            return
        }

        val typedChar = event.key.toChar()

        if (typedValue == TYPED_VALUE_DEFAULT) {
            typedValue = ""
        }

        val isValidChar = typedChar.digitToIntOrNull() != null || typedChar == '.'

        if (!isValidChar || typedChar == '.' && typedValue.contains('.')) {
            return
        }

        typedValue += typedChar
    }

    override fun getPaddingBefore(): Int {
        return 3
    }

    override fun getPaddingAfter(): Int {
        return 15
    }
}