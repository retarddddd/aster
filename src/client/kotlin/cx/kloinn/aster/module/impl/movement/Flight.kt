package cx.kloinn.aster.module.impl.movement

import cx.kloinn.aster.utils.ChatUtils
import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.client.isUsingKeyboardMoveKeys
import cx.kloinn.aster.client.ticksOffGround
import cx.kloinn.aster.client.velocity
import cx.kloinn.aster.client.yaw
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.InventoryUtils
import cx.kloinn.aster.utils.MathUtils
import cx.kloinn.aster.utils.MoveUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

class Flight : Module() {
    override val name = "Flight"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Movement

    enum class FlightMode {
        Vanilla,
        OldNCPElytra
    }

    private val mode = EnumSetting("Mode", FlightMode::class, FlightMode.Vanilla)
    private val horizontalSpeed = FloatSetting("Horizontal Speed", 1.0f, 0.0f, 100.0f, { mode.selectedValue == FlightMode.Vanilla })
    private val verticalSpeed = FloatSetting("Vertical Speed", 1.0f, 0.0f, 100.0f, { mode.selectedValue == FlightMode.Vanilla })
    private val ignoreVelocity = BoolSetting("Ignore Velocity", true, { mode.selectedValue == FlightMode.OldNCPElytra })

    override val settings: ArrayList<Setting> = arrayListOf(mode, horizontalSpeed, verticalSpeed, ignoreVelocity)

    override fun onEnable() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        if (mode.selectedValue == FlightMode.OldNCPElytra) {
            if (player.onGround()) {
                ChatUtils.sendMessage("Please note that this flight mode needs to make you jump and requires at least 3 blocks of distance between the Y position during that jump and the ground to work properly.")
                player.jumpFromGround()
            }
        }
    }

    override fun onTick() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        when (mode.selectedValue) {
            FlightMode.Vanilla -> {
                this.onVanillaTick(client, player)
            }
            FlightMode.OldNCPElytra -> {
                this.onNCPTick(client, player)
            }
        }
    }

    private fun onNCPTick(client: Minecraft, player: LocalPlayer) {
        val gm = client.gameMode ?: return
        val connection = client.connection ?: return

        when (player.ticksOffGround) {
            1 -> {
                val elytraSlot = InventoryUtils.getItemByType(Items.ELYTRA)

                if (elytraSlot == null) {
                    ChatUtils.sendMessage("No elytra slot!")
                    AsterClient.SINGLETON.moduleManager.disableModule(this.name)
                    return
                }

                gm.handleContainerInput(0, elytraSlot, 0, ContainerInput.PICKUP, player)
                gm.handleContainerInput(0, InventoryUtils.INV_SLOT_CHESTPLATE, 0, ContainerInput.PICKUP, player)
            }
            2 -> { /* no-op, we have to wait a tick */ }
            3 -> {
                connection.send(ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING))
            }
            in 4..15 -> {
                MoveUtils.resetElytraState()

                // glide slightly up
                val old = Vec3(player.velocity.x, player.velocity.y + 0.2, player.velocity.z)
                player.velocity = old
            }
            else -> {
                MoveUtils.resetElytraState()

                // I thought I needed to glide to not flag the AC, but I guess I was just schizo?

                // val standingStill = !player.moving
                //
                // player.sendOverlayMessage(Component.literal("MOVING: ${player.moving} | VELO: ${player.velocity} | POS: ${player.position()}"))
                //
                // // glide if standing still
                // if (standingStill) {
                //     val old = Vec3(player.velocity.x, player.velocity.y - 0.001, player.velocity.z)
                //     player.velocity = old
                // }

                player.abilities.mayfly = true
                player.abilities.flying = true
            }
        }
    }

    private fun onVanillaTick(client: Minecraft, player: LocalPlayer) {
        val fwdVelo = MathUtils.moveForward(player.yaw, player.xxa, player.zza, true)

        if (client.options.keyJump.isDown && !client.options.keyShift.isDown) {
            player.velocity = Vec3(0.0, verticalSpeed.value.toDouble(), 0.0)
        } else if (client.options.keyShift.isDown && !client.options.keyJump.isDown) {
            player.velocity = Vec3(0.0, (-verticalSpeed.value).toDouble(), 0.0)
        } else if (player.isUsingKeyboardMoveKeys) {
            player.velocity = Vec3(fwdVelo.x * horizontalSpeed.value, 0.0, fwdVelo.z * horizontalSpeed.value)
        } else {
            player.velocity = Vec3(0.0, 0.0, 0.0)
        }
    }

    override fun onPacketReceive(packet: Packet<*>): Boolean {
        if (packet is ClientboundSetEntityMotionPacket && this.ignoreVelocity.value) {
            return true
        }

        return false
    }

    override fun onDisable() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        if (mode.selectedValue == FlightMode.OldNCPElytra) {
            player.abilities.mayfly = false
            player.abilities.flying = false
        }
    }
}