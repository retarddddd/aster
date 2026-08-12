package cx.kloinn.aster.mixin;

import cx.kloinn.aster.client.AsterClient;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class NetworkMixins {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void send(final Packet<?> packet, final @Nullable ChannelFutureListener listener, CallbackInfo ci) {
        var moduleManager = AsterClient.SINGLETON.getModuleManager();
        var commandManager = AsterClient.SINGLETON.getCommandManager();

        if (commandManager.onPacket(packet)) {
            ci.cancel();
            return;
        }

        for (var module : moduleManager.getModules()) {
            if (!module.getEnabled()) continue;

            if (module.onPacketSend$org_aconite_aster_client(packet)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void read(final ChannelHandlerContext ctx, final Packet<?> packet, CallbackInfo ci) {
        var moduleManager = AsterClient.SINGLETON.getModuleManager();

        for (var module : moduleManager.getModules()) {
            if (!module.getEnabled()) continue;

            if (module.onPacketReceive$org_aconite_aster_client(packet)) {
                ci.cancel();
            }
        }
    }
}

