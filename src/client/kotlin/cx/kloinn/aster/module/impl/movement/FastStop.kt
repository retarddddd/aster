package cx.kloinn.aster.module.impl.movement

import cx.kloinn.aster.client.isUsingKeyboardMoveKeys
import cx.kloinn.aster.client.ticksOffGround
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class FastStop : Module() {
    override val name = "Fast Stop"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Movement

    enum class FastStopCategory {
        Vanilla,
        NCP
    }

    private val modeSetting = EnumSetting("Mode", FastStopCategory::class, FastStopCategory.Vanilla)

    override val settings: ArrayList<Setting> = arrayListOf(modeSetting)

    override fun onMove() {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        val moving = player.isUsingKeyboardMoveKeys

        if (!moving && !player.abilities.flying && !player.isInLiquid) {
            if (modeSetting.selectedValue == FastStopCategory.NCP) {
                // works on NCP by only applying fast stop when it won't make the player fall more than one block and when we haven't already fallen for too long
                // otherwise NCP will lag the player back
                var wouldFallBlocks = 0

                for (i in 0..10) {
                    val pos = BlockPos(player.blockX, player.blockY - i, player.blockZ)
                    val block = player.level().getBlockState(pos)

                    if (block.isAir) {
                        wouldFallBlocks++
                    } else {
                        break
                    }
                }

                if (wouldFallBlocks > 1 || (player.ticksOffGround in 6..<9)) return
            }

            player.lerpMotion(Vec3(0.0, -1.0, 0.0))
        }
    }
}