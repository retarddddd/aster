package cx.kloinn.aster.module.impl.world

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.client.movingHorizontally
import cx.kloinn.aster.client.movingVertically
import cx.kloinn.aster.client.pos
import cx.kloinn.aster.client.velocity
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.Color
import cx.kloinn.aster.utils.InventoryUtils
import cx.kloinn.aster.utils.PacketRewriter
import cx.kloinn.aster.utils.RotModifier
import cx.kloinn.aster.utils.TimerUtils
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Items
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3


class Scaffold : Module() {
    override val name = "Scaffold"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.World

    private enum class TowerMode {
        Off,
        Velocity,
        Timer
    }

    private val airPlace = BoolSetting("Air Place", false)
    private val autoSelect = BoolSetting("Auto Select", true)
    private val renderBlockCount = BoolSetting("Render Block Count", true)
    private val stopIfNoBlocks = BoolSetting("Stop If No Blocks", true)
    private val basicRotations = BoolSetting("Rotations (Basic)", true)
    private val towerMode = EnumSetting("Tower", TowerMode::class, TowerMode.Off)
    private val towerVelo = FloatSetting("Tower Velo", 0.42f, 0f, 1f, { towerMode.selectedValue == TowerMode.Velocity })
    private val towerTimer = FloatSetting("Tower Timer", 1.5f, 1f, 5f, { towerMode.selectedValue == TowerMode.Timer })

    private var lockedPos: Vec3? = null
    private var replacedPitch: Float? = null

    override val settings: ArrayList<Setting> = arrayListOf(airPlace, autoSelect, renderBlockCount, stopIfNoBlocks, basicRotations, towerMode, towerVelo, towerTimer)

    private enum class SupportBlockDirection {
        West,
        East,
        South,
        North,
        SouthWest,
        NorthEast,
        WestEast,
        NorthWest,
        Up,
        Down;

        fun toMC(): Direction? {
            return when (this) {
                West -> Direction.WEST
                East -> Direction.EAST
                South -> Direction.SOUTH
                North -> Direction.NORTH
                Up -> Direction.UP
                Down -> Direction.DOWN
                else -> null
            }
        }
    }

    private val packetRewriter = PacketRewriter(
        onRot = { yaw, pitch ->
            RotModifier(yaw, replacedPitch ?: pitch)
        }
    )

    override fun onPacketSend(packet: Packet<*>): Boolean {
        return this.packetRewriter.onPacketSend(packet)
    }

    override fun onHudRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        if (!renderBlockCount.value) {
            return
        }

        val client = Minecraft.getInstance()
        val font = client.font
        val player = client.player ?: return

        if (player.movingHorizontally && !InventoryUtils.isBlockInHand()) {
            return
        }

        val centerX = client.window.guiScaledWidth / 2
        val centerY = client.window.guiScaledHeight / 2

        val blockCount = InventoryUtils.countBlocks()
        val blockCountStr = blockCount.toString()

        val color = if (blockCount == 0) {
            Color.toArgb(0xFF2A00)
        } else {
            Color.toArgb(0xFFFFFF)
        }

        val width = 16 + 2 + font.width(blockCountStr)

        val baseX = centerX - width / 2
        val baseY = centerY + 10

        val itemStackInHand = player.getItemInHand(InteractionHand.MAIN_HAND)

        if (!InventoryUtils.isValidBlock(itemStackInHand) && itemStackInHand.item != Items.AIR) {
            return
        }

        graphics.item(itemStackInHand, baseX, baseY)
        graphics.text(
            font,
            blockCountStr,
            baseX + 16 + 2,
            baseY + (16 + 4 - font.lineHeight) / 2,
            color
        )
    }

    override fun onTick() {
        val client = Minecraft.getInstance()

        val gamemode = client.gameMode ?: return
        val player = client.player ?: return

        if (autoSelect.value) {
            val slot = InventoryUtils.getSlotWithBlocks()

            if (slot !== null) {
                lockedPos = null

                if (player.movingHorizontally) {
                    player.inventory.selectedSlot = slot
                }
            } else {
                if (stopIfNoBlocks.value) {
                    if (lockedPos == null) {
                        lockedPos = player.position()
                    }

                    player.velocity = Vec3.ZERO
                    player.pos = lockedPos!!
                }
            }
        }

        val mc = Minecraft.getInstance()
        if (mc.options.keyJump.isDown && !player.movingHorizontally) {
            this.handleTower()
        } else {
            val timerMod = AsterClient.SINGLETON.moduleManager.getModule("Timer")!!
            if (!timerMod.enabled) { // reset timer speed (if timer isn't enabled)
                TimerUtils.speedMultiplier = 1f
            }
        }

        if (!player.movingHorizontally && !InventoryUtils.isBlockInHand()) {
            return
        }

        if (basicRotations.value) {
            replacedPitch = -89F
        }

        val playerBlockPos = player.blockPosition().below()
        val blockHitPos = Vec3.atCenterOf(playerBlockPos)

        // attempt to get the direction of the supporting block
        // return if there isn't one on any axis (see the comment inside the function)
        var direction = getDirection() ?: return

        // convert our direction into the direction enum mc expects
        // since minecraft doesn't handle intercardinal directions (SouthWest, WestEast, etc), we have to place an extra support block
        // and then recalculate the direction (to get a regular cardinal direction after recalculating
        // that we can just give to minecraft's BlockHitResult constructor) so we don't randomly fall when scaffolding
        var mcDirection = direction.toMC()

        if (mcDirection == null) {
            // support block direction calc for X/Z
            val supportDirectionXZ = when (direction) {
                SupportBlockDirection.SouthWest, SupportBlockDirection.WestEast -> Direction.NORTH
                SupportBlockDirection.NorthEast, SupportBlockDirection.NorthWest -> Direction.SOUTH
                else -> return
            }

            // support block direction calc for y
            val supportDirectionY = when (direction) {
                SupportBlockDirection.SouthWest, SupportBlockDirection.NorthWest -> Direction.WEST
                SupportBlockDirection.NorthEast, SupportBlockDirection.WestEast -> Direction.EAST
            }

            val supportPos = playerBlockPos
                .relative(supportDirectionY)
                .relative(supportDirectionXZ.opposite)

            val hit = BlockHitResult(
                Vec3.atCenterOf(supportPos),
                supportDirectionXZ,
                supportPos,
                false
            )

            gamemode.useItemOn(
                player,
                InteractionHand.MAIN_HAND,
                hit
            )

            direction = getDirection() ?: return
            mcDirection = direction.toMC() ?: return
        }

        val hit = BlockHitResult(
            blockHitPos,
            mcDirection,
            playerBlockPos,
            false
        )

        gamemode.useItemOn(
            player,
            InteractionHand.MAIN_HAND,
            hit
        )
    }

    private fun handleTower() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        when (towerMode.selectedValue) {
            TowerMode.Off -> {}
            TowerMode.Velocity -> {
                replacedPitch = -89F
                player.velocity = Vec3(player.velocity.x, towerVelo.value.toDouble(), player.velocity.z)
            }
            TowerMode.Timer -> {
                TimerUtils.speedMultiplier = towerTimer.value
            }
        }
    }

    override fun onDisable() {
        replacedPitch = null
        lockedPos = null
    }

    private fun getDirection(): SupportBlockDirection? {
        val client = Minecraft.getInstance()
        val player = client.player ?: return null
        val world = player.level()
        val blockPos = player.blockPosition().below()

        return when {
            !world.getBlockState(BlockPos(blockPos.x + 1, blockPos.y + 0, blockPos.z + 0)).isAir -> SupportBlockDirection.East
            !world.getBlockState(BlockPos(blockPos.x - 1, blockPos.y + 0, blockPos.z + 0)).isAir -> SupportBlockDirection.West

            !world.getBlockState(BlockPos(blockPos.x + 0, blockPos.y + 0, blockPos.z + 1)).isAir -> SupportBlockDirection.South
            !world.getBlockState(BlockPos(blockPos.x + 0, blockPos.y + 0, blockPos.z - 1)).isAir -> SupportBlockDirection.North

            !world.getBlockState(BlockPos(blockPos.x - 1, blockPos.y + 0, blockPos.z + 1)).isAir -> SupportBlockDirection.SouthWest
            !world.getBlockState(BlockPos(blockPos.x + 1, blockPos.y + 0, blockPos.z - 1)).isAir -> SupportBlockDirection.NorthEast
            !world.getBlockState(BlockPos(blockPos.x + 1, blockPos.y + 0, blockPos.z + 1)).isAir -> SupportBlockDirection.WestEast
            !world.getBlockState(BlockPos(blockPos.x - 1, blockPos.y + 0, blockPos.z - 1)).isAir -> SupportBlockDirection.NorthWest

            !world.getBlockState(BlockPos(blockPos.x + 0, blockPos.y + 1, blockPos.z + 0)).isAir -> SupportBlockDirection.Up
            !world.getBlockState(BlockPos(blockPos.x + 0, blockPos.y - 1, blockPos.z + 0)).isAir -> SupportBlockDirection.Down

            else -> {
                // this means that there isn't any supporting block on any axis
                // ChatUtils.sendMessage("scaffold error")

                // we can try air-placing if the user allows us to do so
                if (airPlace.value) {
                    return SupportBlockDirection.Up
                }

                null
            }
        }
    }
}