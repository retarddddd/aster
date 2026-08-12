package cx.kloinn.aster.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import cx.kloinn.aster.client.AsterClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRenderMixin {
    @Shadow
    private GameRenderer gameRenderer;

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addAlwaysOnTopPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            ),
            index = 2
    )
    private GpuBufferSlice addAlwaysOnTopPass(GpuBufferSlice terrainFog) {
        var esp = AsterClient.SINGLETON.getModuleManager().getModule("ESP");

        if (!esp.getEnabled()) {
            return terrainFog;
        }

        var fogRenderer = ((GameRendererAccessor) this.gameRenderer).getFogRenderer();
        return fogRenderer.getBuffer(FogRenderer.FogMode.NONE);
    }

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    public void render(
            final GraphicsResourceAllocator resourceAllocator, final DeltaTracker deltaTracker,
            final boolean renderOutline, final CameraRenderState cameraState,
            final Matrix4fc modelViewMatrix, final GpuBufferSlice terrainFog,
            final Vector4f fogColor, final boolean shouldRenderSky,
            CallbackInfo ci
    ) {
        var moduleManager = AsterClient.SINGLETON.getModuleManager();

        for (var module : moduleManager.getModules()) {
            if (!module.getEnabled()) continue;

            module.onWorldRender$org_aconite_aster_client(
                    resourceAllocator, deltaTracker, renderOutline,
                    cameraState, modelViewMatrix, terrainFog,
                    fogColor, shouldRenderSky
            );
        }
    }
}