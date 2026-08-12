package cx.kloinn.aster.module

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.resource.GraphicsResourceAllocator
import cx.kloinn.aster.module.setting.Setting
import cx.kloinn.aster.module.setting.impl.BoolSetting
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonInfo
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.network.protocol.Packet
import org.joml.Matrix4fc
import org.joml.Vector4f

abstract class Module {
    abstract val name: String
    abstract val keybinds: ArrayList<Int>
    abstract var enabled: Boolean
    abstract val category: ModuleManager.Category

    private val renderInModuleList = BoolSetting("Show In Module List", true)

    protected abstract val settings: ArrayList<Setting>

    @JvmName("_getSettings") // This is done to prevent a name clash with `settings` (because kotlin will generate a getter for it named `getSettings()` and we have a function also named `getSettings()`)
    fun getSettings(): ArrayList<Setting> {
        if (!settings.contains(renderInModuleList)) {
            settings.add(renderInModuleList)
        }

        return settings
    }

    internal open fun onInit() {}
    internal open fun onEnable() {}
    internal open fun onDisable() {}

    internal open fun onKeyInput(action: Int, event: KeyEvent): Boolean { return false }
    internal open fun onMouseButtonInput(action: Int, event: MouseButtonInfo) {}
    internal open fun onCursorInput(x: Int, y: Int) {}

    internal open fun onMove() {}

    internal open fun onWorldRender(
        resourceAllocator: GraphicsResourceAllocator, deltaTracker: DeltaTracker,
        renderOutline: Boolean, cameraState: CameraRenderState,
        modelViewMatrix: Matrix4fc, terrainFog: GpuBufferSlice,
        fogColor: Vector4f, shouldRenderSky: Boolean
    ) {}
    internal open fun onHudRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {}
    internal open fun onScoreboardRender(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker): Boolean { return false }

    // Two special events coming straight from my heart :heart:
    internal open fun onLevelEndExtraction(context: LevelExtractionContext) {}
    internal open fun onScreenAfterInit(client: Minecraft, screen: Screen, width: Int, height: Int) {}

    internal open fun onTick() {}

    internal open fun onPacketSend(packet: Packet<*>): Boolean { return false }
    internal open fun onPacketReceive(packet: Packet<*>): Boolean { return false }
}