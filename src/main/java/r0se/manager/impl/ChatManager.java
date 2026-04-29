/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.hud.ChatHud
 *  net.minecraft.text.MutableText
 *  net.minecraft.network.message.MessageSignatureData
 *  org.jetbrains.annotations.Nullable
 */
package r0se.manager.impl;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.feature.ModuleToggleEvent;
import r0se.api.feature.ToggleableFeature;
import r0se.manager.Manager;
import r0se.util.chat.ChatHudExt;

public class ChatManager
implements Manager {
    private final Map<String, Text> keyedMessages = new HashMap<String, Text>();

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.keyedMessages.clear();
    }

    @Subscribe
    public void onModuleToggle(ModuleToggleEvent event) {
        ToggleableFeature feature = event.getFeature();
        if (feature == null || !feature.shouldNotify()) {
            return;
        }
        boolean enabled = event.isEnabled();
        MutableText message = Text.literal((String)"[Module] ").formatted(Formatting.DARK_AQUA).append((Text)Text.literal((String)feature.getName()).formatted(Formatting.AQUA)).append((Text)Text.literal((String)(enabled ? " enabled" : " disabled")).formatted(enabled ? Formatting.GREEN : Formatting.RED));
        this.send("module:" + feature.getIdentifier(), this.withPrefix((Text)message));
    }

    public void send(Text text) {
        ChatHud chatHud = this.getChatHud();
        if (chatHud == null || text == null) {
            return;
        }
        chatHud.addMessage(text);
    }

    public void info(String title, String message) {
        MutableText body = Text.literal((String)("[" + title + "] ")).formatted(Formatting.DARK_AQUA).append((Text)Text.literal((String)message).formatted(Formatting.WHITE));
        this.send(this.withPrefix((Text)body));
    }

    public void error(String title, String message) {
        MutableText body = Text.literal((String)("[" + title + "] ")).formatted(Formatting.RED).append((Text)Text.literal((String)message).formatted(Formatting.WHITE));
        this.send(this.withPrefix((Text)body));
    }

    public void send(String key, Text text) {
        if (key == null || key.isBlank() || text == null) {
            return;
        }
        this.remove(key);
        this.keyedMessages.put(key, text);
        this.send(text);
    }

    public boolean remove(String key) {
        ChatHudExt ext;
        if (key == null || key.isBlank()) {
            return false;
        }
        Text text = this.keyedMessages.remove(key);
        if (text == null) {
            return false;
        }
        ChatHud chatHud = this.getChatHud();
        return chatHud instanceof ChatHudExt && (ext = (ChatHudExt)chatHud).r0se$removeMessage(text);
    }

    public int removeByContent(String content, boolean exact) {
        int n;
        if (content == null || content.isBlank()) {
            return 0;
        }
        ChatHud chatHud = this.getChatHud();
        if (chatHud instanceof ChatHudExt) {
            ChatHudExt ext = (ChatHudExt)chatHud;
            n = ext.r0se$removeMessagesByString(content, exact);
        } else {
            n = 0;
        }
        return n;
    }

    public void removeBySignature(@Nullable MessageSignatureData signature) {
        ChatHud chatHud = this.getChatHud();
        if (chatHud == null || signature == null) {
            return;
        }
        chatHud.removeMessage(signature);
    }

    private Text withPrefix(Text text) {
        return Text.empty().append((Text)Text.literal((String)"[").formatted(Formatting.DARK_GRAY)).append((Text)Text.literal((String)"ROSE").formatted(Formatting.LIGHT_PURPLE)).append((Text)Text.literal((String)"] ").formatted(Formatting.DARK_GRAY)).append((Text)text.copy());
    }

    private ChatHud getChatHud() {
        if (R0SE.mc == null || R0SE.mc.inGameHud == null) {
            return null;
        }
        return R0SE.mc.inGameHud.getChatHud();
    }
}


