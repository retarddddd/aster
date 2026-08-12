package cx.kloinn.aster.module.impl.combat

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.client.pos
import cx.kloinn.aster.client.ticksOffGround
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.impl.world.Scaffold
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.utils.MoveUtils
import cx.kloinn.aster.utils.PacketRewriter
import cx.kloinn.aster.utils.PosModifier
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.world.entity.player.Input
import net.minecraft.world.phys.Vec3

class Criticals : Module() {
    override val name = "Criticals"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Combat

    private enum class CriticalsMode {
        Jump,
        SilentJump
    }

    private val mode = EnumSetting("Mode", CriticalsMode::class, CriticalsMode.Jump)

    override val settings: ArrayList<Setting> = arrayListOf(mode)

    private var replacedPosY: Double? = null
    private var tick = 0
    private var jumping = false

    private val packetRewriter = PacketRewriter(
        onPos = { x, _, z ->
            if (replacedPosY == null) {
                PosModifier()
            }

            PosModifier(x, replacedPosY, z)
        }
    )

    override fun onPacketSend(packet: Packet<*>): Boolean {
        if (mode.selectedValue !== CriticalsMode.SilentJump) {
            return false
        }

        if (AsterClient.SINGLETON.moduleManager.getModule("Scaffold")!!.enabled) {
            return false
        }

        return this.packetRewriter.onPacketSend(packet)
    }

    override fun onEnable() {
        replacedPosY = null
        tick = 0
        jumping = false
    }

    // See KillAura::isReadyToAttack
    fun isReadyToAttack(player: LocalPlayer): Boolean {
        when (mode.selectedValue) {
            CriticalsMode.Jump -> {
                if (player.onGround()) {
                    MoveUtils.jump()
                }

                if (player.ticksOffGround >= 9) {
                    return true
                }
            }
            CriticalsMode.SilentJump -> {
                if (!jumping) {
                    replacedPosY = null
                    tick = 0
                    jumping = true
                }

                if (tick >= 9) {
                    return true
                }
            }
        }

        return false
    }

    override fun onTick() {
        if (mode.selectedValue == CriticalsMode.SilentJump) {
            if (!jumping) return

            val client = Minecraft.getInstance()
            val player = client.player ?: return

            tick++

            if (tick == 1) {
                client.connection!!.send(ServerboundPlayerInputPacket(Input(player.input.keyPresses.forward, player.input.keyPresses.backward, player.input.keyPresses.left, player.input.keyPresses.right, true, false, true)))
            }

            // tick > all ticks we have accounted for
            if (tick >= MoveUtils.JUMP_VALS.size) {
                replacedPosY = null
                tick = 0
                jumping = false
                return
            }

            val jumpDelta = MoveUtils.JUMP_VALS[tick - 1]

            val previousY = replacedPosY ?: player.pos.y

            val pos = Vec3(player.pos.x, previousY + jumpDelta, player.pos.z)
            replacedPosY = pos.y

            val onGround = tick == MoveUtils.JUMP_VALS.size

            player.connection.send(
                ServerboundMovePlayerPacket.Pos(
                    pos,
                    onGround,
                    player.horizontalCollision
                )
            )
        }
    }
}