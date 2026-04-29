/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.world.TickEvent;

@Mixin(value={MinecraftClient.class})
public abstract class MinecraftClientMixin {
    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void tick(CallbackInfo ci) {
        TickEvent event = new TickEvent();
        R0SE.eventHandler.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}


