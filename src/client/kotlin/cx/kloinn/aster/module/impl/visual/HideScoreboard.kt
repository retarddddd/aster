package cx.kloinn.aster.module.impl.visual

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

class HideScoreboard : Module() {
    override val name = "Hide Scoreboard"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category: ModuleManager.Category = ModuleManager.Category.Visual
    override val settings: ArrayList<Setting> = arrayListOf()

    override fun onScoreboardRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker): Boolean {
        return true
    }
}