package cx.kloinn.aster.module.impl.combat

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.InventoryUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Items

class AutoTotem : Module() {
    override val name = "Auto Totem"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Combat
    override val settings: ArrayList<Setting> = arrayListOf()

    override fun onTick() {
        val client = Minecraft.getInstance()

        val player = client.player ?: return
        val gm = client.gameMode ?: return

        val totemSlot = InventoryUtils.getItemByType(Items.TOTEM_OF_UNDYING)

        if (totemSlot != null) {
            if (!player.offhandItem.`is`(Items.AIR)) {
                return
            }

            gm.handleContainerInput(0, totemSlot, 0, ContainerInput.PICKUP, player)
            gm.handleContainerInput(0, InventoryUtils.INV_SLOT_OFFHAND, 0, ContainerInput.PICKUP, player)
        }
    }
}