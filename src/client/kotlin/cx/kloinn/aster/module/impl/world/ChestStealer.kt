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

class ChestStealer : Module() {
    override val name = "Chest Stealer"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.World

    private val clock = Clock()

    private enum class ChestStealerMode {
        ShiftClick,
        Click
    }

    private val chestStealerMode = EnumSetting("Mode", ChestStealerMode::class, ChestStealerMode.ShiftClick)
    private val delay = FloatSetting("Delay", 10.0f, 0.0f, 60.0f)

    override val settings: ArrayList<Setting> = arrayListOf(chestStealerMode, delay)

    override fun onTick() {
        val player = Minecraft.getInstance().player ?: return
        val container = player.containerMenu

        if (container is ChestMenu) {
            val chestSlots = container.rowCount * InventoryUtils.INV_SLOTS_IN_ROW // this also accounts for double chests

            InventoryUtils.moveChestItems(
                container,
                0 until chestSlots,
                chestSlots until container.slots.size,
                when (chestStealerMode.selectedValue) {
                    ChestStealerMode.ShiftClick -> ContainerInput.QUICK_MOVE
                    ChestStealerMode.Click -> ContainerInput.PICKUP
                },
                clock,
                delay.value
            )
        }
    }
}
