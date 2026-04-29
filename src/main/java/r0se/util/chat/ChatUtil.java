/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.network.message.MessageSignatureData
 *  org.jetbrains.annotations.Nullable
 */
package r0se.util.chat;

import net.minecraft.text.Text;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;
import r0se.manager.Managers;

public final class ChatUtil {
    private ChatUtil() {
    }

    public static void send(Text text) {
        Managers.CHAT.send(text);
    }

    public static void info(String title, String message) {
        Managers.CHAT.info(title, message);
    }

    public static void error(String title, String message) {
        Managers.CHAT.error(title, message);
    }

    public static void send(String key, Text text) {
        Managers.CHAT.send(key, text);
    }

    public static boolean remove(String key) {
        return Managers.CHAT.remove(key);
    }

    public static int removeByContent(String content, boolean exact) {
        return Managers.CHAT.removeByContent(content, exact);
    }

    public static void removeBySignature(@Nullable MessageSignatureData signature) {
        Managers.CHAT.removeBySignature(signature);
    }
}


