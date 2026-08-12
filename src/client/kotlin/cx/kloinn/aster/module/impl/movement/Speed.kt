package cx.kloinn.aster.module.impl.movement

import cx.kloinn.aster.client.isUsingKeyboardMoveKeys
import cx.kloinn.aster.client.ticksOffGround
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.Clock
import cx.kloinn.aster.utils.MoveUtils
import cx.kloinn.aster.utils.TimerUtils
import it.unimi.dsi.fastutil.floats.FloatSet
import net.minecraft.client.Minecraft
import net.minecraft.client.player.ClientInput
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ServerboundPongPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.world.entity.player.Input

class Speed : Module() {
    override val name = "Speed"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Movement

    enum class SpeedMode {
        Legit,
        Dex,
        UNCP
    }

    private val mode = EnumSetting("Mode", SpeedMode::class, SpeedMode.Legit)
    private val legitSprint = BoolSetting("Sprint", true, { mode.selectedValue == SpeedMode.Legit })
    private val timerSprint = FloatSetting("Timer", 10.0f, 1.5f, 100.0f, { mode.selectedValue == SpeedMode.Dex })

    override val settings: ArrayList<Setting> = arrayListOf(mode, legitSprint, timerSprint)

    private var tick = 0
    private var skipTransactions = false
    private var clock: Clock = Clock()

    override fun onTick() {
        when (mode.selectedValue) {
            SpeedMode.Legit -> {
                handleVanillaTick()
            }
            SpeedMode.Dex -> {
                handleDexTick()
            }
            SpeedMode.UNCP -> {
                handUNCPTick()
            }
        }
    }

    override fun onPacketSend(packet: Packet<*>): Boolean {
        if (mode.selectedValue == SpeedMode.Dex) {
            if (packet is ServerboundPongPacket && skipTransactions) {
                return true
            }
        }

        return false
    }

    private fun handleVanillaTick() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        if (!player.isUsingKeyboardMoveKeys) return

        MoveUtils.jump()
    }

    private fun handleDexTick() {
        // This is utterly retarded (writing this late at night, ignore my grammar pls)
        // The server this speed was made for uses NCP + their custom AC (named Dex iirc).
        // NCP on there only handles movement checks whilst their custom AC handles timer, misc checks, and combat checks.
        // NCP has timer checks disabled, therefore they're entirely delegated to their custom anticheat.
        //
        // Their custom AC's timer check ... disables itself if you sneak. I don't know why.
        // Seriously I figured that out by accident. I could go on Wurst, toggle some massive timer, get kicked
        // then do the exact same thing but sneak before toggling on the timer, and I'd be able to use it.
        // However, when I unsneak, their custom AC immediately starts spamming lagbacks to my current position
        // For whatever reason, if I stopped responding to ALL ping/transaction packets while sneaking, this would work and NOT lag me back at all
        //
        // ... I don't know why don't they have a check to just instantly ban you if you don't send transactions but
        // send other packets... It's like a 10-minute patch at maximum. I guess their anticheat developer is just severely retarded?
        // (I mean... not having basic ping spoof/transaction order checks, and disabling timer checks when sneaking is
        // some next level retardness). They also have a transaction timeout check however it is not a big of a deal
        // as the timeout is set to be around ~120 seconds (2 minutes)
        this.tick++

        if (tick == 1) {
            // Speed up.
            Minecraft.getInstance().options.keyShift.isDown = true
            skipTransactions = true

            TimerUtils.speedMultiplier = timerSprint.value
        }

        // Wait for 10 seconds.
        if (this.clock.hasTimePassed(10000)) {
            // Stop.
            TimerUtils.speedMultiplier = 1.0f
            Minecraft.getInstance().options.keyShift.isDown = false

            skipTransactions = false
            this.tick = -20 // One-second delay.
        }
    }

    private fun handUNCPTick() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        if (!player.isUsingKeyboardMoveKeys) return

        if (player.onGround()) {
            client.connection!!.send(ServerboundPlayerInputPacket(Input(player.input.keyPresses.forward, player.input.keyPresses.backward, player.input.keyPresses.left, player.input.keyPresses.right, true, false, true)))
            player.jumpFromGround()
        }

        if (player.ticksOffGround <= 3) {
            TimerUtils.speedMultiplier = 1.3f
        } else {
            TimerUtils.speedMultiplier = 1f
        }
    }

    override fun onDisable() {
        if (this.mode.selectedValue == SpeedMode.UNCP || this.mode.selectedValue == SpeedMode.Dex) {
            TimerUtils.speedMultiplier = 1f
        }
    }
}