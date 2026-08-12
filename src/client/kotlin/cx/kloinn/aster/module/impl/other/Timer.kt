package cx.kloinn.aster.module.impl.other

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.impl.movement.Velocity.VelocityMode
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.MoveUtils
import cx.kloinn.aster.utils.TimerUtils
import cx.kloinn.aster.utils.TimerUtils.speedMultiplier
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft

class Timer : Module() {
    override val name = "Timer"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.Other

    private val timer = FloatSetting("Game Speed", 1.0f, 0.0f, 5.5f)

    override val settings: ArrayList<Setting> = arrayListOf(timer)

    override fun onTick() {
        speedMultiplier = timer.value
    }

    override fun onDisable() {
        speedMultiplier = 1.0f
    }
}