package cx.kloinn.aster.module.impl.world

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.Clock
import cx.kloinn.aster.utils.InventoryUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput

class ChestStorer : Module() {
    override val name = "Chest Storer"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.World

    private val clock = Clock()

    private enum class ChestStorerMode {
        ShiftClick,
        Click
    }

    private val chestStorerMode = EnumSetting("Mode", ChestStorerMode::class, ChestStorerMode.ShiftClick)
    private val delay = FloatSetting("Delay", 10.0f, 0.0f, 60.0f)

    override val settings: ArrayList<Setting> = arrayListOf(chestStorerMode, delay)

    override fun onTick() {
        val player = Minecraft.getInstance().player ?: return
        val container = player.containerMenu

        if (container is ChestMenu) {
            val chestSlots = container.rowCount * InventoryUtils.INV_SLOTS_IN_ROW // this also accounts for double chests

            InventoryUtils.moveChestItems(
                container,
                chestSlots until container.slots.size,
                0 until chestSlots,
                when (chestStorerMode.selectedValue) {
                    ChestStorerMode.ShiftClick -> ContainerInput.QUICK_MOVE
                    ChestStorerMode.Click -> ContainerInput.PICKUP
                },
                clock,
                delay.value
            )
        }
    }
}
