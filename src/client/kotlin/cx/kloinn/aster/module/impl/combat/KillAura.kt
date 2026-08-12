package cx.kloinn.aster.module.impl.combat

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.client.pitch
import cx.kloinn.aster.client.pos
import cx.kloinn.aster.client.velocity
import cx.kloinn.aster.client.yaw
import cx.kloinn.aster.mixin.ClientLevelAccessor
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.Clock
import cx.kloinn.aster.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerboundAttackPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundSwingPacket
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.sqrt

class KillAura : Module() {
    override val name = "Kill Aura"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Combat

    private enum class SwingMode {
        Server,
        Client,
        Off
    }

    private enum class AttackMode {
        CPS_1_8,
        Modern
    }

    private enum class RotationMode {
        None,
        Flick
    }

    private val attackMode = EnumSetting("Attack Mode", AttackMode::class, AttackMode.CPS_1_8)
    private val rotationMode = EnumSetting("Rotation Mode", RotationMode::class, RotationMode.None)
    private val moveFix = BoolSetting("Move Fix", true, { rotationMode.selectedValue == RotationMode.Flick })
    private val viewCheck = BoolSetting("View Check", true, { rotationMode.selectedValue == RotationMode.Flick })
    private val distance = FloatSetting("Distance", 3.0f, 0.1f, 6.0f)
    private val cps = FloatSetting("CPS", 10.0f, 1.0f, 20.0f, { attackMode.selectedValue == AttackMode.CPS_1_8 })
    private val swingMode = EnumSetting("Swing Mode", SwingMode::class, SwingMode.Client)

    private enum class FlickState {
        None,
        Rotating,
        Hitting,
        RotatingBack
    }

    private var flickState: FlickState = FlickState.None
    private var targetEntity: Entity? = null
    private var frozenPlayerPos: Vec3? = null
    private var noEvent: Boolean = false

    private val attackClock = Clock()

    override val settings: ArrayList<Setting> = arrayListOf(attackMode, rotationMode, moveFix, viewCheck, distance, cps, swingMode)

    override fun onEnable() {
        flickState = FlickState.None
        targetEntity = null
        noEvent = false
        frozenPlayerPos = null
    }

    override fun onTick() {
        val client = Minecraft.getInstance()
        val localPlayer = client.player ?: return

        if (flickState != FlickState.None) {
            freezePlayer(localPlayer)
            return
        }

        val connection = localPlayer.connection
        val world = localPlayer.level()

        val entitiesIter = (world as ClientLevelAccessor).getEntityGetter().all

        for (entity in entitiesIter) {
            if (entity == localPlayer) {
                // well... we don't want to hit the local player (obviously)
                continue
            }

            if (!entity.isAttackable || !entity.isAlive) {
                continue
            }

            val dist = localPlayer.position().distanceTo(entity.pos)

            if (dist < distance.value && isReadyToAttack(localPlayer)) {
                when (rotationMode.selectedValue) {
                    RotationMode.None -> {
                        attackEntity(entity, localPlayer, connection)
                        handleSwing(localPlayer, connection)
                    }
                    RotationMode.Flick -> {
                        val (targetYaw, targetPitch) = MathUtils.calculateRotation(localPlayer, entity)

                        if (viewCheck.value && MathUtils.isInFieldOfView(localPlayer, targetYaw, targetPitch)) {
                            flickState = FlickState.None

                            attackEntity(entity, localPlayer, connection)
                            handleSwing(localPlayer, connection)

                            return
                        }

                        targetEntity = entity

                        flickState = FlickState.Rotating
                        frozenPlayerPos = if (moveFix.value) localPlayer.position() else null

                        freezePlayer(localPlayer)
                        return
                    }
                }
            }
        }
    }

    override fun onMove() {
        Minecraft.getInstance().player?.let(::freezePlayer)
    }

    // TODO: maybe convert this to use PacketRewriter?
    override fun onPacketSend(packet: Packet<*>): Boolean {
        if (noEvent) {
            noEvent = false
            return false
        }

        val mcInst = Minecraft.getInstance()
        val player = mcInst.player ?: return false
        val connection = mcInst.connection ?: return false

        when (flickState) {
            FlickState.None -> {}
            FlickState.Rotating -> {
                if (targetEntity == null || !targetEntity!!.isAlive) {
                    flickState = FlickState.None
                    frozenPlayerPos = null
                    return false
                }

                val targetRot = MathUtils.calculateRotation(player, targetEntity!!)

                if (packet is ServerboundMovePlayerPacket && !noEvent) {
                    noEvent = true

                    player.connection.send(
                        ServerboundMovePlayerPacket.Rot(
                            targetRot.first,
                            targetRot.second,
                            packet.isOnGround,
                            packet.horizontalCollision()
                        )
                    )

                    flickState = FlickState.Hitting

                    return true
                }
            }

            FlickState.Hitting -> {
                flickState = FlickState.RotatingBack

                if (targetEntity == null || !targetEntity!!.isAlive) {
                    flickState = FlickState.None
                    frozenPlayerPos = null
                    return false
                }

                attackEntity(targetEntity!!, player, connection)
                handleSwing(player, connection)
            }

            FlickState.RotatingBack -> {
                if (packet is ServerboundMovePlayerPacket && !noEvent) {
                    noEvent = true

                    player.connection.send(
                        ServerboundMovePlayerPacket.Rot(
                            player.pitch,
                            player.yaw,
                            packet.isOnGround,
                            packet.horizontalCollision()
                        )
                    )

                    flickState = FlickState.None
                    frozenPlayerPos = null

                    return true
                }
            }
        }

        return false
    }

    private fun freezePlayer(player: LocalPlayer) {
        val position = frozenPlayerPos ?: return
        player.pos = position
        player.velocity = Vec3.ZERO
    }

    override fun onDisable() {
        flickState = FlickState.None
        targetEntity = null
        frozenPlayerPos = null
        noEvent = false
    }

    private fun attackEntity(entity: Entity, localPlayer: LocalPlayer, connection: ClientPacketListener) {
        localPlayer.attack(entity) // attacks the entity client-sidely (which only displays the attack cooldown bar) without sending packets to the server
        connection.send(ServerboundAttackPacket(entity.id)) // attacks the entity server-sidely
    }

    private fun isReadyToAttack(player: LocalPlayer): Boolean {
        val criticalsMod = AsterClient.SINGLETON.moduleManager.getModule("Criticals") as Criticals

        if (criticalsMod.enabled && !criticalsMod.isReadyToAttack(player)) {
            return false
        }

        return when (attackMode.selectedValue) {
            AttackMode.CPS_1_8 -> {
                attackClock.hasTimePassed((1000.0f / cps.value).toLong())
            }

            AttackMode.Modern -> {
                player.getAttackStrengthScale(0.0f) >= 1.0f
            }
        }
    }

    private fun handleSwing(player: LocalPlayer, connection: ClientPacketListener) {
        val hand = player.usedItemHand

        when (swingMode.selectedValue) {
            SwingMode.Server -> {
                connection.send(ServerboundSwingPacket(hand))
            }
            SwingMode.Client -> {
                player.swing(hand)
            }
            SwingMode.Off -> { /* no-op */ }
        }
    }
}