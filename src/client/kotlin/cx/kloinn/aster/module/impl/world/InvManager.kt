package cx.kloinn.aster.module.impl.world

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.utils.InventoryUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType

class InvManager : Module() {
    override val name = "Inv Manager"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.World

    private enum class CleanerMode {
        Off,
        Minigame,
        SMP
    }

    private val pauseInLobby = BoolSetting("Pause In Lobby", true)
    private val cleanerMode = EnumSetting("Cleaner Mode", CleanerMode::class, CleanerMode.Off)
    private val autoArmor = BoolSetting("Auto Armor", true)
    private val ignoreChestplateSlot = BoolSetting("Ignore Chestplate", false, { autoArmor.value })

    override val settings: ArrayList<Setting> = arrayListOf(pauseInLobby, cleanerMode, autoArmor, ignoreChestplateSlot)

    override fun onTick() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        val gamemode = client.gameMode ?: return
        val inv = player.inventory

        if (player.gameMode() == GameType.ADVENTURE && pauseInLobby.value) {
            return
        }

        // cleaner
        for (slotIdx in InventoryUtils.INV_SLOT_MAIN_INVENTORY_START..InventoryUtils.INV_SLOT_HOTBAR_END) {
            if (cleanerMode.selectedValue == CleanerMode.Off) {
                continue
            }

            val inventorySlot = getInventorySlot(slotIdx) ?: continue
            val itemStack = inv.getItem(inventorySlot)

            if (itemStack.item == Items.AIR) {
                continue
            }

            if (!isUseful(itemStack)) {
                gamemode.handleContainerInput(
                    0, // 0 is always the inv iirc
                    slotIdx,
                    1, // 1 = throws everything, 0 = throws 1 item
                    ContainerInput.THROW,
                    player
                )
            }
        }

        // autoarmor
        for (slotIdx in InventoryUtils.INV_SLOT_MAIN_INVENTORY_START..InventoryUtils.INV_SLOT_HOTBAR_END) {
            if (!autoArmor.value) {
                continue
            }

            val inventorySlot = getInventorySlot(slotIdx) ?: continue
            val itemStack = inv.getItem(inventorySlot)
            val item = itemStack.item

            // This could be moved into `when` I think
            if (InventoryUtils.isHelmet(item)) {
                gamemode.handleContainerInput(0, slotIdx, 0, ContainerInput.PICKUP, player)
                gamemode.handleContainerInput(0, InventoryUtils.INV_SLOT_HELMET, 0, ContainerInput.PICKUP, player)
            }

            if (InventoryUtils.isChestplate(item) && !ignoreChestplateSlot.value) {
                gamemode.handleContainerInput(0, slotIdx, 0, ContainerInput.PICKUP, player)
                gamemode.handleContainerInput(0, InventoryUtils.INV_SLOT_CHESTPLATE, 0, ContainerInput.PICKUP, player)
            }

            if (InventoryUtils.isLeggings(item)) {
                gamemode.handleContainerInput(0, slotIdx, 0, ContainerInput.PICKUP, player)
                gamemode.handleContainerInput(0, InventoryUtils.INV_SLOT_LEGGINGS, 0, ContainerInput.PICKUP, player)
            }

            if (InventoryUtils.isBoots(item)) {
                gamemode.handleContainerInput(0, slotIdx, 0, ContainerInput.PICKUP, player)
                gamemode.handleContainerInput(0, InventoryUtils.INV_SLOT_BOOTS, 0, ContainerInput.PICKUP, player)
            }
        }
    }

    private fun getInventorySlot(containerSlot: Int): Int? {
        return when (containerSlot) {
            in InventoryUtils.INV_SLOT_MAIN_INVENTORY_START..InventoryUtils.INV_SLOT_MAIN_INVENTORY_END -> containerSlot
            in InventoryUtils.INV_SLOT_HOTBAR_START..InventoryUtils.INV_SLOT_HOTBAR_END ->
                containerSlot - InventoryUtils.INV_SLOT_HOTBAR_START
            else -> null
        }
    }

    private fun isUseful(itemStack: ItemStack): Boolean {
        val item = itemStack.item

        when (cleanerMode.selectedValue) {
            CleanerMode.Off -> {
                // unreachable anyway because of the if check in onTick
                return true
            }
            CleanerMode.Minigame -> {
                if (InventoryUtils.isValidBlock(itemStack)) {
                    return true
                }

                return InventoryUtils.isGenerallyUsefulItem(item)
            }
            CleanerMode.SMP -> {
                val item = itemStack.item

                if (
                    // it's 4 am i can't really think of other useless items
                    item == Items.WHEAT_SEEDS ||
                    item == Items.ROTTEN_FLESH ||

                    // menu items
                    item == Items.NETHER_STAR && itemStack.customName != null ||
                    item == Items.BLAZE_POWDER && itemStack.customName != null ||
                    item == Items.MUSIC_DISC_13 && itemStack.customName != null
                ) {
                    return false
                }

                return true
            }
        }
    }
}