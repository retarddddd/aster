package cx.kloinn.aster.module.impl.visual

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.utils.UIPosition
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

class ModuleList : Module() {
    enum class SortingModule {
        Longest,
        Shortest,
    }

    override val name = "Module List"
    override var enabled: Boolean = true
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.Visual

    private val sortingSetting = EnumSetting("Sorting", SortingModule::class, SortingModule.Longest)
    private val padX = FloatSetting("X Padding", 5.0f, 1.0f, 100.0f)
    private val padY = FloatSetting("Y Padding", 5.0f, 1.0f, 100.0f)

    override val settings: ArrayList<Setting> = arrayListOf(sortingSetting, padX, padY)

    override fun onHudRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        val modManager = AsterClient.SINGLETON.moduleManager

        val window = Minecraft.getInstance().window

        val startingPos = UIPosition(window.guiScaledWidth - padX.value.toInt(), padY.value.toInt())
        val font = Minecraft.getInstance().font

        val sortedModules = if (sortingSetting.selectedValue == SortingModule.Longest) {
            modManager.modules.sortedByDescending { font.width(it.name) }
        } else {
            modManager.modules.sortedBy { font.width(it.name) }
        }

        for (module in sortedModules) {
            // non null-asserted since the setting will always exist
            val showSetting = module.getSettings().find { it.settingName == "Show In Module List" }!! as BoolSetting

            if (module.enabled && showSetting.value) {
                graphics.text(
                    font,
                    module.name,
                    startingPos.x - font.width(module.name),
                    startingPos.y,
                    0xFFFFFFFF.toInt()
                )

                startingPos.y += font.lineHeight
            }
        }
    }
}