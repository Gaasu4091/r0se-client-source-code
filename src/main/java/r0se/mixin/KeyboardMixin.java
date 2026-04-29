/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Keyboard
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.game.KeyEvent;

@Mixin(value={Keyboard.class})
public class KeyboardMixin {
    @Inject(method={"onKey"}, at={@At(value="HEAD")})
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        R0SE.eventHandler.post(new KeyEvent(key, action));
    }
}


