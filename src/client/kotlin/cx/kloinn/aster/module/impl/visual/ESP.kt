package cx.kloinn.aster.module.impl.visual

import cx.kloinn.aster.client.pos
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.utils.Color
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState
import net.minecraft.client.renderer.entity.state.ArrowRenderState
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState
import net.minecraft.client.renderer.entity.state.BoatRenderState
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState
import net.minecraft.client.renderer.entity.state.FireworkRocketRenderState
import net.minecraft.client.renderer.entity.state.FishingHookRenderState
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState
import net.minecraft.client.renderer.entity.state.LlamaSpitRenderState
import net.minecraft.client.renderer.entity.state.MinecartRenderState
import net.minecraft.client.renderer.entity.state.PaintingRenderState
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState
import net.minecraft.client.renderer.entity.state.TntRenderState
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState
import net.minecraft.core.BlockPos
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.Gizmos
import net.minecraft.gizmos.SimpleGizmoCollector
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ShulkerBoxBlock
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

class ESP : Module() {
    override val name = "ESP"
    override var enabled: Boolean = false
    override val keybinds: ArrayList<Int> = arrayListOf()
    override val category = ModuleManager.Category.Visual

    private enum class EspRenderStyle {
        Dynamic,
        Static
    }

    private val blockEsp = BoolSetting("Block ESP", true)
    private val blockEspStyle = EnumSetting("Style", EspRenderStyle::class, EspRenderStyle.Dynamic, blockEsp::value)
    private val blockEspFillIn = BoolSetting("Fill In", true, blockEsp::value)
    private val chests = BoolSetting("Chests", true, blockEsp::value)
    private val trappedChests = BoolSetting("Trapped Chests", true, blockEsp::value)
    private val enderChests = BoolSetting("Ender Chests", true, blockEsp::value)
    private val barrels = BoolSetting("Barrels", true, blockEsp::value)
    private val shulkers = BoolSetting("Shulkers", true, blockEsp::value)
    private val hoppers = BoolSetting("Hoppers", true, blockEsp::value)
    private val dispensers = BoolSetting("Droppers", true, blockEsp::value)
    private val droppers = BoolSetting("Dispensers", true, blockEsp::value)

    private val entityEsp = BoolSetting("Entity ESP", true)
    private val entityEspStyle = EnumSetting("Style", EspRenderStyle::class, EspRenderStyle.Dynamic, entityEsp::value)
    private val items = BoolSetting("Items", true, entityEsp::value)
    private val players = BoolSetting("Players", true, entityEsp::value)
    private val mobs = BoolSetting("Mobs", true, entityEsp::value)
    private val internal = BoolSetting("Internal", true, entityEsp::value)

    override val settings: ArrayList<Setting> = arrayListOf(
        // Block ESP
        blockEsp, blockEspStyle, blockEspFillIn,
        chests, trappedChests, enderChests, shulkers,
        barrels, hoppers, droppers, dispensers,

        // Entity ESP
        entityEsp, entityEspStyle,
        items, players, mobs, internal
    )

    private var processDelay = 0

    private data class CachedBlock(val block: Block, val blockPos: BlockPos)
    private val blockEspCachedBlocks: ArrayList<CachedBlock> = arrayListOf()

    private data class QueueEntry(val chunkX: Int, val chunkZ: Int)
    private val scanQueue = ConcurrentLinkedQueue<QueueEntry>()

    override fun onLevelEndExtraction(context: LevelExtractionContext) {
        if (!enabled) return

        val collector = SimpleGizmoCollector()

        if (blockEsp.value) {
            renderBlockEsp()
        }

        if (entityEsp.value) {
            renderEntityEsp(context)
        }

        context.levelRenderer().addMainThreadGizmos(collector.drainGizmos())
    }

    override fun onPacketReceive(packet: Packet<*>): Boolean {
        if (Minecraft.getInstance().level == null) return false

        if (packet is ClientboundLevelChunkWithLightPacket) {
            val chunkX = packet.x shl 4 // packet.x << 4
            val chunkZ = packet.z shl 4 // packet.y << 4

            val queue = QueueEntry(chunkX, chunkZ)
            scanQueue.add(queue)
        }

        if (packet is ClientboundSectionBlocksUpdatePacket) {
            packet.runUpdates { blockPos, blockState ->
                val block = blockState.block

                if (isNeededBlock(block)) {
                    if (block !== Blocks.AIR && this.blockEspCachedBlocks.any { it.blockPos == blockPos }) {
                        return@runUpdates
                    }

                    blockEspCachedBlocks.add(CachedBlock(block, blockPos.immutable()))
                }
            }
        }

        if (packet is ClientboundBlockUpdatePacket) {
            val block = packet.blockState.block
            val blockPos = packet.pos

            if (block !== Blocks.AIR && this.blockEspCachedBlocks.any { it.blockPos == blockPos }) {
                return false
            }

            if (isNeededBlock(block)) {
                this.blockEspCachedBlocks.add(CachedBlock(block, blockPos))
            } else {
                this.blockEspCachedBlocks.removeIf { it.blockPos == blockPos }
            }
        }

        return false
    }

    override fun onTick() {
        val lp = Minecraft.getInstance().player

        if (lp == null) {
            this.blockEspCachedBlocks.clear()
            this.scanQueue.clear()
            return
        }

        val world = Minecraft.getInstance().level ?: return

        val iterator = scanQueue
            .sortedBy { lp.position().distanceTo(Vec3(it.chunkX.toDouble(), lp.pos.y, it.chunkZ.toDouble())) }
            .iterator()

        var chunksProcessed = 0

        for (queueElement in iterator) {
            // essentially we only process 16 chunks every 16 ticks
            this.processDelay++

            if (this.processDelay < 16) {
                return
            }

            chunksProcessed++

            if (chunksProcessed > 16) {
                this.processDelay = 0
                return
            }

            val chunkX = queueElement.chunkX
            val chunkZ = queueElement.chunkZ

            for (x in chunkX until (chunkX + 16)) {
                for (z in chunkZ until (chunkZ + 16)) {
                    for (y in -world.height until world.height) {
                        val blockState = world.getBlockState(BlockPos(x, y, z))
                        val block = blockState.block

                        if (isNeededBlock(block)) {
                            val blockPos = BlockPos(x, y, z)

                            blockEspCachedBlocks.add(CachedBlock(block, blockPos))
                        }
                    }
                }
            }

            scanQueue.remove(queueElement)
        }
    }

    // ESP MODES
    private fun renderBlockEsp() {
        val localPlayer = Minecraft.getInstance().player ?: return

        this.blockEspCachedBlocks.removeIf {
            return@removeIf abs(it.blockPos.x - localPlayer.pos.x) > 1000 ||
                    abs(it.blockPos.y - localPlayer.pos.y) > 1000 ||
                    abs(it.blockPos.z - localPlayer.pos.z) > 1000
        }

        this.blockEspCachedBlocks.removeIf {
            val world = localPlayer.level()
            val uncachedBlock = world.getBlockState(it.blockPos)

            return@removeIf !isNeededBlock(uncachedBlock.block)
        }

        for (cachedBlock in this.blockEspCachedBlocks) {
            if (!shouldRenderBlock(cachedBlock.block)) {
                continue
            }

            val aabb = AABB(cachedBlock.blockPos)
            val color = getBlockEspColor(cachedBlock.block)

            renderBox(aabb, color, blockEspFillIn.value)
        }
    }

    private fun renderEntityEsp(context: LevelExtractionContext) {
        for (entity in context.levelState().entityRenderStates) {
            val shouldRender = shouldRenderEntityEsp(entity)

            if (shouldRender) {
                val style = GizmoStyle.stroke(getEntityEspColor(entity), 1.5f)

                val aabb = AABB(
                    Vec3(entity.x - (entity.boundingBoxWidth / 2), entity.y, entity.z - (entity.boundingBoxWidth / 2)),
                    Vec3(
                        entity.x + (entity.boundingBoxWidth / 2),
                        entity.y + entity.boundingBoxHeight,
                        entity.z + (entity.boundingBoxWidth / 2)
                    )
                )

                Gizmos.cuboid(aabb, style).setAlwaysOnTop()
            }
        }
    }

    // UTILS
    private fun getEntityEspColor(entity: EntityRenderState): Int {
        when (entityEspStyle.selectedValue) {
            EspRenderStyle.Dynamic -> {
                if (players.value && entity is AvatarRenderState) {
                    return Color.toArgb(0x152194)
                }

                if (items.value && (entity is ThrownItemRenderState || entity is ItemClusterRenderState)) {
                    return Color.toArgb(0xCCD91C)
                }

                if (internal.value && isEntityInternal(entity)) {
                    return Color.toArgb(0x3D3D37)
                }

                // everything else is a mob
                if (mobs.value && !isEntityInternal(entity)) {
                    return Color.toArgb(0x27F527)
                }

                throw IllegalStateException("Unreachable")
            }

            EspRenderStyle.Static -> {
                return Color.toArgb(0x27F527)
            }
        }
    }

    private fun shouldRenderEntityEsp(entity: EntityRenderState): Boolean {
        if (entity is AvatarRenderState) {
            return players.value
        }

        if (entity is ThrownItemRenderState || entity is ItemClusterRenderState) {
            return items.value
        }

        if (isEntityInternal(entity)) {
            return internal.value
        }

        // everything else is a mob
        if (!isEntityInternal(entity)) {
            return mobs.value
        }

        return false
    }

    private fun isEntityInternal(entity: EntityRenderState): Boolean {
        return entity is ArmorStandRenderState ||
                entity is ArrowRenderState ||
                entity is BlockDisplayEntityRenderState ||
                entity is BoatRenderState ||
                entity is DisplayEntityRenderState ||
                entity is EndCrystalRenderState ||
                entity is EvokerFangsRenderState ||
                entity is ExperienceOrbRenderState ||
                entity is FallingBlockRenderState ||
                entity is FireworkRocketRenderState ||
                entity is FishingHookRenderState ||
                entity is ItemFrameRenderState ||
                entity is LightningBoltRenderState ||
                entity is LlamaSpitRenderState ||
                entity is MinecartRenderState ||
                entity is PaintingRenderState ||
                entity is ShulkerBulletRenderState ||
                entity is ThrownTridentRenderState ||
                entity is TntRenderState ||
                entity is WitherSkullRenderState
    }

    private fun getBlockEspColor(block: Block): Int {
        when (blockEspStyle.selectedValue) {
            EspRenderStyle.Dynamic -> {
                when (block) {
                    Blocks.ENDER_CHEST -> {
                        return Color.toArgb(0x184035)
                    }
                    Blocks.TRAPPED_CHEST -> {
                        return Color.toArgb(0xDE0000)
                    }
                    Blocks.CHEST -> {
                        return Color.toArgb(0xF57627)
                    }
                    Blocks.BARREL -> {
                        return Color.toArgb(0x8A4604)
                    }
                }

                // special case for my special little block :heart:
                if (block is ShulkerBoxBlock) {
                    return Color.toArgb(0xCF27F5)
                }

                // these are the same color, so we might as well put them here.
                if (block == Blocks.DROPPER || block == Blocks.DISPENSER || block == Blocks.HOPPER) {
                    return Color.toArgb(0x3D3D3D)
                }
            }

            EspRenderStyle.Static -> {
                return Color.toArgb(0xFF2B00)
            }
        }

        throw IllegalStateException("Unreachable")
    }

    private fun shouldRenderBlock(block: Block): Boolean {
        if (chests.value && (block == Blocks.CHEST /*  || block == Blocks.COPPER_CHEST */)) return true
        if (trappedChests.value && block == Blocks.TRAPPED_CHEST) return true
        if (enderChests.value && block == Blocks.ENDER_CHEST) return true
        if (barrels.value && block == Blocks.BARREL) return true
        if (shulkers.value && (block == Blocks.SHULKER_BOX || block == Blocks.DYED_SHULKER_BOX || block is ShulkerBoxBlock)) return true
        if (hoppers.value && block == Blocks.HOPPER) return true
        if (droppers.value && block == Blocks.DROPPER) return true
        if (dispensers.value && block == Blocks.DISPENSER) return true

        return false
    }

    private fun isNeededBlock(block: Block): Boolean {
        return block == Blocks.CHEST ||
                block == Blocks.COPPER_CHEST ||
                block == Blocks.TRAPPED_CHEST ||
                block == Blocks.ENDER_CHEST ||
                block == Blocks.BARREL ||
                block == Blocks.SHULKER_BOX ||
                block == Blocks.DYED_SHULKER_BOX ||
                block is ShulkerBoxBlock ||
                block == Blocks.HOPPER ||
                block == Blocks.DROPPER ||
                block == Blocks.DISPENSER
    }

    // This somewhat fixes depth issues and creates a very cool-looking effect.
    // This renders two boxes: one at 25% transparency, which the player sees through walls,
    // and one at 100% transparency, which the player sees directly without any obstructions.
    private fun renderBox(aabb: AABB, color: Int, fillIn: Boolean = false) {
        // transparent layer
        val moreTransparentFillColor = (color and 0x00FFFFFF) or (0x20 shl 24)
        val moreTransparentColor = (color and 0x00FFFFFF) or (0x80 shl 24)

        val styleTransparent = if (fillIn) {
            GizmoStyle.strokeAndFill(moreTransparentColor, 2f, moreTransparentFillColor)
        } else {
            GizmoStyle.stroke(moreTransparentColor, 2f)
        }
        Gizmos.cuboid(aabb, styleTransparent).setAlwaysOnTop()

        // normal layer
        val fillColor = (color and 0x00FFFFFF) or (0x40 shl 24)

        val style = if (fillIn) {
            GizmoStyle.strokeAndFill(color, 2f, fillColor)
        } else {
            GizmoStyle.stroke(color, 2f)
        }
        Gizmos.cuboid(aabb, style)
    }
}
