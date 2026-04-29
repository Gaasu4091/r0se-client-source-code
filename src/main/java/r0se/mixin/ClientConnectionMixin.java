/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.ClientConnection
 *  net.minecraft.network.packet.Packet
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.network.PacketOutboundEvent;
import r0se.manager.Managers;

@Mixin(value={ClientConnection.class})
public abstract class ClientConnectionMixin {
    @Inject(method={"send"}, at={@At(value="HEAD")}, cancellable=true)
    private void r0se$sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (Managers.NETWORK.consumeQuietPacket(packet)) {
            return;
        }
        PacketOutboundEvent event = new PacketOutboundEvent(packet);
        R0SE.eventHandler.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}


