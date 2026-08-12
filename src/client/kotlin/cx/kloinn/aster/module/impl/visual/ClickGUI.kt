package cx.kloinn.aster.module.impl.visual

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.client.InputManager
import cx.kloinn.aster.utils.UIPosition
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.Color
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

class ClickGUI : Module() {
    override val name = "Click GUI"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf(GLFW.GLFW_KEY_INSERT)
    override val category = ModuleManager.Category.Visual

    private enum class EnabledModuleStyle {
        Colored,
        Static
    }

    private val enabledModuleStyle = EnumSetting("Module Style", EnabledModuleStyle::class, EnabledModuleStyle.Colored)
    private val speed = FloatSetting("Color Speed", 2f, 2f, 100f, { enabledModuleStyle.selectedValue == EnabledModuleStyle.Colored })

    override val settings: ArrayList<Setting> = arrayListOf(enabledModuleStyle, speed)

    companion object {
        val CATEGORY_SIZE = UIPosition(125, 14)

        const val CATEGORY_PADDING = 10
        const val BACKGROUND_PADDING = 10

        var expandedModules: HashMap<Module, Boolean> = hashMapOf()
        fun setExpanded(module: Module, expanded: Boolean) { expandedModules[module] = expanded }
        fun isExpanded(module: Module) = expandedModules[module] ?: false
    }

    override fun onEnable() {
        AsterClient.SINGLETON.inputManager.setMouseState(InputManager.MouseState.Grabbed)
    }

    override fun onDisable() {
        AsterClient.SINGLETON.inputManager.setMouseState(InputManager.MouseState.Released)

        for (module in AsterClient.SINGLETON.moduleManager.modules) {
            for (setting in module.getSettings()) {
                setting.onClickGuiClosed()
            }
        }
    }

    override fun onHudRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        val client = AsterClient.SINGLETON
        val allModules = client.moduleManager.modules

        val window = Minecraft.getInstance().window
        val font = Minecraft.getInstance().font

        // bg
        graphics.fill(0, 0, window.guiScaledWidth, window.guiScaledHeight, Color.toArgb(0x090A0F, 0.82f))

        val nSpeed = speed.value.coerceIn(0f, 100f) / 100.0f
        val rainbowColor = Color.nextColor(deltaTracker.gameTimeDeltaTicks * nSpeed)

        // bottom background. it looks pretty ass but can be tweaked to look good
        // graphics.fillGradient(0, window.guiScaledHeight, window.guiScaledWidth, window.guiScaledHeight, rainbowColor.toArgb(0f), rainbowColor.toArgb(0.5f))

        val initPos = UIPosition(100, 25)

        for (category in ModuleManager.Category.ALL) {
            // category bg rect
            graphics.fill(
                initPos.x,
                initPos.y,
                initPos.x + CATEGORY_SIZE.x,
                initPos.y + CATEGORY_SIZE.y,
                Color.toArgb(0x212121)
            )

            // category name
            val categoryName = category.name
            graphics.text(
                font,
                categoryName,
                initPos.x + (CATEGORY_SIZE.x - font.width(categoryName)) / 2,
                initPos.y + 1 + (CATEGORY_SIZE.y - font.lineHeight) / 2,
                Color.toArgb(0xFFFFFF)
            )

            val modules = allModules
                .filter { it.category == category }
                .sortedByDescending { font.width(it.name) }

            val modulePos = UIPosition(initPos.x, initPos.y + CATEGORY_SIZE.y)

            for (module in modules) {
                // module bg
                val bgColor = if (module.enabled) {
                    when (enabledModuleStyle.selectedValue) {
                        EnabledModuleStyle.Colored -> rainbowColor.toArgb()
                        EnabledModuleStyle.Static -> Color.toArgb(0x9E9E9E)
                    }
                } else {
                    Color.toArgb(0x1A1A1A)
                }

                val moduleBgStart = UIPosition(modulePos.x, modulePos.y)
                val moduleBgEnd = UIPosition(modulePos.x + CATEGORY_SIZE.x, modulePos.y + CATEGORY_SIZE.y)

                val cursorPos = client.inputManager.getCursorPos()

                if (
                    ((cursorPos.x - moduleBgEnd.x) > -CATEGORY_SIZE.x && (cursorPos.x - moduleBgEnd.x) < 0) &&
                    ((cursorPos.y - moduleBgEnd.y) > -CATEGORY_SIZE.y && (cursorPos.y - moduleBgEnd.y) < 0)
                ) {
                    if (client.inputManager.isLeftClickingFirstTime()) {
                        client.moduleManager.toggleModule(module.name)
                    }

                    if (client.inputManager.isRightClickingFirstTime()) {
                        if (module.getSettings().isEmpty()) return // don't expand if there is nothing to expand

                        setExpanded(module, !isExpanded(module))
                    }
                }

                graphics.fill(
                    moduleBgStart.x,
                    moduleBgStart.y,
                    moduleBgEnd.x,
                    moduleBgEnd.y,
                    bgColor
                )

                // module text
                graphics.text(
                    font,
                    module.name,
                    modulePos.x + (CATEGORY_SIZE.x - font.width(module.name)) / 2,
                    modulePos.y + 1 + (CATEGORY_SIZE.y - font.lineHeight) / 2,
                    Color.toArgb(0xFFFFFF)
                )

                val filteredModules = module.getSettings().filter { it.precondFunc() }

                val settingsHeight = if (isExpanded(module)) {
                    filteredModules.sumOf {
                        val padding = it.getPaddingAfter() + it.getPaddingBefore()
                        padding
                    }
                } else {
                    0
                }

                if (isExpanded(module)) {
                    graphics.fill(
                        moduleBgStart.x,
                        moduleBgEnd.y,
                        moduleBgEnd.x,
                        moduleBgEnd.y + BACKGROUND_PADDING + settingsHeight,
                        Color.toArgb(0x141414)
                    )

                    var i = 0

                    for (setting in filteredModules) {
                        i++

                        if (i != 1) {
                            moduleBgStart.y += setting.getPaddingBefore()
                            moduleBgEnd.y += setting.getPaddingBefore()
                        }

                        setting.render(graphics, moduleBgStart.x, moduleBgStart.y + 5, moduleBgEnd.x, moduleBgEnd.y + 5)

                        moduleBgStart.y += setting.getPaddingAfter()
                        moduleBgEnd.y += setting.getPaddingAfter()
                    }
                }

                val backgroundPadding = if (isExpanded(module)) {
                    BACKGROUND_PADDING
                } else {
                    0
                }

                modulePos.y += CATEGORY_SIZE.y + settingsHeight + backgroundPadding
            }

            initPos.x += CATEGORY_SIZE.x + CATEGORY_PADDING
        }
    }

    override fun onKeyInput(action: Int, event: KeyEvent): Boolean {
        val client = AsterClient.SINGLETON

        if (event.key == GLFW.GLFW_KEY_ESCAPE) {
            client.moduleManager.toggleModule(this.name)
            return false
        }

        return true // cancel keyboard input
    }
}