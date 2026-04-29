/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.hud.ChatHudLine
 *  net.minecraft.client.gui.hud.ChatHud
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package r0se.mixin;

import java.util.List;
import java.util.Objects;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import r0se.util.chat.ChatHudExt;

@Mixin(value={ChatHud.class})
public abstract class ChatHudMixin
implements ChatHudExt {
    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Shadow
    protected abstract void refresh();

    @Override
    public boolean r0se$removeMessage(Text text) {
        if (text == null) {
            return false;
        }
        boolean removed = this.messages.removeIf(line -> line != null && ChatHudMixin.sameText(line.content(), text));
        if (removed) {
            this.refresh();
        }
        return removed;
    }

    @Override
    public int r0se$removeMessagesByString(String content, boolean exact) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        int before = this.messages.size();
        this.messages.removeIf(line -> {
            if (line == null || line.content() == null) {
                return false;
            }
            String current = line.content().getString();
            return exact ? content.equals(current) : current.contains(content);
        });
        int removed = before - this.messages.size();
        if (removed > 0) {
            this.refresh();
        }
        return removed;
    }

    private static boolean sameText(Text first, Text second) {
        return first == second || Objects.equals(first, second) || Objects.equals(first.getString(), second.getString());
    }
}


