/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.network.PlayerUpdateEvent;

@Mixin(value={ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin {
    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift=At.Shift.BEFORE)})
    private void r0se$playerUpdatePre(CallbackInfo ci) {
        R0SE.eventHandler.post(new PlayerUpdateEvent.Pre());
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")})
    private void r0se$playerUpdatePrePacket(CallbackInfo ci) {
        R0SE.eventHandler.post(new PlayerUpdateEvent.PrePacket());
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="TAIL")})
    private void r0se$playerUpdatePost(CallbackInfo ci) {
        R0SE.eventHandler.post(new PlayerUpdateEvent.Post());
    }
}


