/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.input.KeyboardInput
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.input.PlayerInputEvent;
import r0se.mixin.InputAccessor;

@Mixin(value={KeyboardInput.class})
public abstract class KeyboardInputMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")}, cancellable=true)
    private void r0se$playerInput(CallbackInfo ci) {
        InputAccessor accessor = (InputAccessor)((Object)this);
        PlayerInputEvent event = new PlayerInputEvent(accessor.r0se$getMovementForward(), accessor.r0se$getMovementSideways());
        R0SE.eventHandler.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            accessor.r0se$setMovementForward(event.getMovementForward());
            accessor.r0se$setMovementSideways(event.getMovementSideways());
        }
    }
}


