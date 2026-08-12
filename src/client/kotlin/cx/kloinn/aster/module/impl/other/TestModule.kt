package cx.kloinn.aster.module.impl.other

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting

class TestModule : Module() {
    override val name = "Test"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.Other
    override val settings: ArrayList<Setting> = arrayListOf()
}