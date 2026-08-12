package cx.kloinn.aster.utils

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput

object InventoryUtils {
    // TODO: This should use an enum, however:
    // 1) I'm lazy
    // 2) It's 4 AM for me
    // 0 = crafting result
    // 1..4 = crafting grid
    // 5..8 = armor
    // 9..35 = main inventory
    // 36..44 = hotbar
    // 45 = offhand
    const val INV_SLOT_CRAFTING_RESULT = 0
    const val INV_SLOT_CRAFTING_GRID_START = 1
    const val INV_SLOT_CRAFTING_GRID_END = 4
    const val INV_SLOT_ARMOR_START = 5
    const val INV_SLOT_ARMOR_END = 8
    const val INV_SLOT_MAIN_INVENTORY_START = 9
    const val INV_SLOT_MAIN_INVENTORY_END = 35
    const val INV_SLOT_HOTBAR_START = 36
    const val INV_SLOT_HOTBAR_END = 44

    const val INV_SLOT_OFFHAND = 45
    const val INV_SLOT_HELMET = 5
    const val INV_SLOT_CHESTPLATE = 6
    const val INV_SLOT_LEGGINGS = 7
    const val INV_SLOT_BOOTS = 8

    const val INV_SLOTS_IN_ROW = 9

    // only used in cheststealer/cheststorer
    fun moveChestItems(container: ChestMenu, sourceSlots: IntRange, destinationSlots: IntRange, input: ContainerInput, clock: Clock, delay: Float) {
        val client = Minecraft.getInstance()
        val player = client.player ?: return
        val gamemode = client.gameMode ?: return

        for (slotId in sourceSlots) {
            if (!container.getSlot(slotId).hasItem()) {
                continue
            }

            if (!clock.hasTimePassed((1000.0f / delay).toLong())) {
                return
            }

            if (input == ContainerInput.QUICK_MOVE) {
                gamemode.handleContainerInput(container.containerId, slotId, 0, input, player)
            } else if (input == ContainerInput.PICKUP) {
                val freeSlotId = destinationSlots.firstOrNull { !container.getSlot(it).hasItem() } ?: return

                gamemode.handleContainerInput(container.containerId, slotId, 0, input, player)
                gamemode.handleContainerInput(container.containerId, freeSlotId, 0, input, player)
            } else {
                throw IllegalStateException("Invalid input mode")
            }
        }
    }

    fun getItemByType(item: Item): Int? {
        val client = Minecraft.getInstance()
        val player = client.player ?: return null

        return player
            .inventoryMenu
            .slots
            .find { it.item.item == item }
            ?.index
    }

    fun isValidBlock(itemStack: ItemStack): Boolean {
        val item = itemStack.item

        val client = Minecraft.getInstance()
        val level = client.level ?: return false
        val pos = client.player?.blockPosition() ?: return false

        if (
            item == Items.SAND ||
            item == Items.GRAVEL ||
            item == Items.RED_SAND ||
            item == Items.CONCRETE_POWDER
        ) {
            return false
        }

        if (item is BlockItem) {
            val state = item.block.defaultBlockState()

            return !state.getCollisionShape(level, pos).isEmpty && state.isCollisionShapeFullBlock(level, pos)
        }

        return false
    }

    fun isBlockInHand(): Boolean {
        val client = Minecraft.getInstance()
        val player = client.player ?: return false

        val itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND)

        return itemInHand.item is BlockItem
    }

    fun countBlocks(): Int {
        val client = Minecraft.getInstance()
        val player = client.player ?: return 0

        var blocks = 0

        for (hotbarSlot in 0..8) {
            val itemStack = player.inventory.getItem(hotbarSlot)

            val isBlock = isValidBlock(itemStack)

            if (isBlock) {
                blocks += itemStack.count
            }
        }

        return blocks
    }

    fun getSlotWithBlocks(): Int? {
        val client = Minecraft.getInstance()
        val player = client.player ?: return null

        var slot: Int? = null

        for (hotbarSlot in 0..8) {
            val itemStack = player.inventory.getItem(hotbarSlot)

            val isBlock = isValidBlock(itemStack)

            if (isBlock) {
                slot = hotbarSlot
                break
            }
        }

        return slot
    }

    fun isGenerallyUsefulItem(item: Item): Boolean {
        val isUseful = {
            // swords
            item == Items.COPPER_SWORD ||
            item == Items.DIAMOND_SWORD ||
            item == Items.GOLDEN_SWORD ||
            item == Items.IRON_SWORD ||
            item == Items.NETHERITE_SWORD ||
            item == Items.STONE_SWORD ||
            item == Items.WOODEN_SWORD ||

            // axes
            item == Items.COPPER_AXE ||
            item == Items.DIAMOND_AXE ||
            item == Items.GOLDEN_AXE ||
            item == Items.IRON_AXE ||
            item == Items.NETHERITE_AXE ||
            item == Items.STONE_AXE ||
            item == Items.WOODEN_AXE ||

            // pickaxes
            item == Items.COPPER_PICKAXE ||
            item == Items.DIAMOND_PICKAXE ||
            item == Items.GOLDEN_PICKAXE ||
            item == Items.IRON_PICKAXE ||
            item == Items.NETHERITE_PICKAXE ||
            item == Items.STONE_PICKAXE ||
            item == Items.WOODEN_PICKAXE ||

            // armor
            isArmor(item) ||

            // shooting
            item == Items.BOW ||
            item == Items.CROSSBOW ||
            item == Items.ARROW ||

            // utilities
            item == Items.MACE ||
            item == Items.WIND_CHARGE ||
            item == Items.TNT ||
            item == Items.SHIELD ||
            item == Items.SNOWBALL ||
            item == Items.EGG ||
            item == Items.FIRE_CHARGE || // retarded name
            item == Items.ENDER_PEARL ||
            item == Items.WATER_BUCKET ||
            item == Items.LAVA_BUCKET ||
            item == Items.POWDER_SNOW_BUCKET ||
            item == Items.FISHING_ROD ||
            item == Items.FLINT_AND_STEEL ||
            item == Items.SHEARS ||
            item == Items.COBWEB ||
            item == Items.LADDER ||

            // food
            item == Items.GOLDEN_APPLE ||
            item == Items.ENCHANTED_GOLDEN_APPLE ||
            item == Items.GOLDEN_CARROT ||
            item == Items.COOKED_BEEF || // steaks
            item == Items.COOKED_PORKCHOP ||

            // potions
            item == Items.POTION ||
            item == Items.SPLASH_POTION ||
            item == Items.LINGERING_POTION ||

            // flying
            item == Items.ELYTRA ||
            item == Items.FIREWORK_ROCKET ||

            item == Items.TRIDENT ||
            item == Items.TOTEM_OF_UNDYING ||
            item == Items.MILK_BUCKET ||
            item == Items.CHORUS_FRUIT
        }

        return isUseful()
    }

    fun isHelmet(item: Item): Boolean {
        return item == Items.LEATHER_HELMET ||
                item == Items.COPPER_HELMET ||
                item == Items.CHAINMAIL_HELMET ||
                item == Items.GOLDEN_HELMET ||
                item == Items.IRON_HELMET ||
                item == Items.DIAMOND_HELMET ||
                item == Items.NETHERITE_HELMET ||
                item == Items.TURTLE_HELMET
    }

    fun isChestplate(item: Item): Boolean {
        return item == Items.LEATHER_CHESTPLATE ||
                item == Items.COPPER_CHESTPLATE ||
                item == Items.CHAINMAIL_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.IRON_CHESTPLATE ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.NETHERITE_CHESTPLATE
    }

    fun isLeggings(item: Item): Boolean {
        return item == Items.LEATHER_LEGGINGS ||
                item == Items.COPPER_LEGGINGS ||
                item == Items.CHAINMAIL_LEGGINGS ||
                item == Items.GOLDEN_LEGGINGS ||
                item == Items.IRON_LEGGINGS ||
                item == Items.DIAMOND_LEGGINGS ||
                item == Items.NETHERITE_LEGGINGS
    }

    fun isBoots(item: Item): Boolean {
        return item == Items.LEATHER_BOOTS ||
                item == Items.COPPER_BOOTS ||
                item == Items.CHAINMAIL_BOOTS ||
                item == Items.GOLDEN_BOOTS ||
                item == Items.IRON_BOOTS ||
                item == Items.DIAMOND_BOOTS ||
                item == Items.NETHERITE_BOOTS
    }

    fun isArmor(item: Item): Boolean {
        return isHelmet(item) ||
                isChestplate(item) ||
                isLeggings(item) ||
                isBoots(item)
    }
}