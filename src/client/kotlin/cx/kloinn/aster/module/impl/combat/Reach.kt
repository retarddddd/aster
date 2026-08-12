package cx.kloinn.aster.module.impl.combat

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.FloatSetting

class Reach : Module() {
    override val name = "Reach"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Combat

    val combatReach = FloatSetting("Combat Reach", 3.0f, 3.0f, 10.0f)
    val blockReach = FloatSetting("Block Reach", 3.0f, 3.0f, 10.0f)

    override val settings: ArrayList<Setting> = arrayListOf(combatReach, blockReach)

    // See PlayerMixin
}