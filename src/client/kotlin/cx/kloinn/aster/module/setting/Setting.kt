package cx.kloinn.aster.module.setting

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent

typealias PrecondFunc = () -> Boolean

abstract class Setting(open val settingName: String, open val precondFunc: PrecondFunc = { true }) {
    abstract fun render(graphics: GuiGraphicsExtractor, cursorX: Int, cursorY: Int, endCursorX: Int, endCursorY: Int)
    open fun onKeyInput(action: Int, event: KeyEvent) {}
    open fun onClickGuiClosed() {}

    abstract fun getPaddingBefore(): Int
    abstract fun getPaddingAfter(): Int
}