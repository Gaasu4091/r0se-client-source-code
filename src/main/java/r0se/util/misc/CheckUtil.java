/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.DeathScreen
 *  net.minecraft.client.gui.screen.ingame.SignEditScreen
 */
package r0se.util.misc;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import r0se.R0SE;

public class CheckUtil {
    public static boolean checkScreen() {
        return R0SE.mc.currentScreen != null && !(R0SE.mc.currentScreen instanceof ChatScreen) && !(R0SE.mc.currentScreen instanceof SignEditScreen) && !(R0SE.mc.currentScreen instanceof DeathScreen);
    }
}


