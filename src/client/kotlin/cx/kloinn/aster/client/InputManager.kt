package cx.kloinn.aster.client

import com.mojang.blaze3d.platform.InputConstants
import cx.kloinn.aster.utils.UIPosition
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonInfo
import net.minecraft.network.chat.Component

class InputManager(private val clientRef: AsterClient) {
    enum class MouseState {
        Released,
        Grabbed
    }

    private val pressedKeys = mutableSetOf<Int>()
    private var leftClickingTicks = 0
    private var rightClickingTicks = 0
    private var _leftClicking = false
    private var _rightClicking = false
    private var cursorPosition = UIPosition(0, 0)

    fun isKeyPressed(key: Int): Boolean = key in pressedKeys

    fun getCursorPos(): UIPosition = cursorPosition

    fun isLeftClicking(): Boolean = leftClickingTicks > 0
    fun isRightClicking(): Boolean = rightClickingTicks > 0

    fun isLeftClickingFirstTime(): Boolean = leftClickingTicks == 1
    fun isRightClickingFirstTime(): Boolean = rightClickingTicks == 1

    fun receiveKeyboardInput(action: Int, event: KeyEvent): Boolean {
        when (action) {
            InputConstants.PRESS -> pressedKeys.add(event.key())
            InputConstants.RELEASE -> pressedKeys.remove(event.key())
        }

        return clientRef.moduleManager.onKeyInput(action, event)
    }

    fun receiveMouseButtonInput(action: Int, event: MouseButtonInfo) {
        val clicking = action == InputConstants.PRESS

        when (event.button()) {
            InputConstants.MOUSE_BUTTON_LEFT -> _leftClicking = clicking
            InputConstants.MOUSE_BUTTON_RIGHT -> _rightClicking = clicking
        }

        clientRef.moduleManager.onMouseButtonInput(action, event)
    }

    fun tick() {
        if (_leftClicking) {
            leftClickingTicks++
        } else {
            leftClickingTicks = 0
        }

        if (_rightClicking) {
            rightClickingTicks++
        } else {
            rightClickingTicks = 0
        }
    }

    fun receiveCursorInput(x: Double, y: Double) {
        val normX = x.toInt()
        val normY = y.toInt()

        cursorPosition = UIPosition(normX, normY)
        clientRef.moduleManager.onCursorInput(normX, normY)
    }

    fun setMouseState(state: MouseState) {
        val inputScreen = object : Screen(Component.empty()) {
            override fun isPauseScreen(): Boolean { return false; }
            override fun isAllowedInPortal(): Boolean { return true; }
            override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {}
        }

        when (state) {
            MouseState.Grabbed -> { Minecraft.getInstance().gui.setScreen(inputScreen) }
            MouseState.Released -> { Minecraft.getInstance().gui.setScreen(null) }
        }
    }
}
