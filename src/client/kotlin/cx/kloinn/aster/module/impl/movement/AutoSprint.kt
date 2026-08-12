package cx.kloinn.aster.module.impl.movement

import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.utils.MoveUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket

class AutoSprint : Module() {
    override val name = "Auto Sprint"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled: Boolean = true
    override val category = ModuleManager.Category.Movement

    companion object {
        private var suppressNextTick = false

        fun stopForAttack(player: LocalPlayer) {
            if (player.isSprinting) {
                player.connection.send(
                    ServerboundPlayerCommandPacket(
                        player,
                        ServerboundPlayerCommandPacket.Action.STOP_SPRINTING
                    )
                )
            }
            player.isSprinting = false
            suppressNextTick = true
        }
    }

    private val omniSetting = BoolSetting("Omni", false)

    override val settings: ArrayList<Setting> = arrayListOf(omniSetting)

    override fun onTick() {
        if (suppressNextTick) {
            suppressNextTick = false
            return
        }
        MoveUtils.setSprinting(omniSetting.value)
    }
}