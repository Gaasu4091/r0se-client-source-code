/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.manager.Managers;

@Mixin(value={ChatScreen.class})
public abstract class ChatScreenMixin {
    @Inject(method={"sendMessage"}, at={@At(value="HEAD")}, cancellable=true)
    private void r0se$handleCommand(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (Managers.COMMANDS.handleChatMessage(chatText)) {
            ci.cancel();
        }
    }
}


