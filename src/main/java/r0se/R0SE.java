/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.ModInitializer
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.Screen
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package r0se;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import r0se.api.event.EventHandler;
import r0se.api.event.Subscribe;
import r0se.api.event.game.KeyEvent;
import r0se.impl.gui.clickgui.ClickGuiScreen;
import r0se.impl.module.client.ClickGui;
import r0se.manager.Managers;

public class R0SE
implements ModInitializer,
ClientModInitializer {
    public static String CLIENT_ID = "r0se-client";
    public static String CLIENT_STATUS = "Unknown";
    public static String CLIENT_VERSION = "0.1.0";
    public static String CLIENT_NAME = "R0SE";
    public static String GIT_HASH = "81abb8c3ca252f90e1ff785d8157b78755fcbeea";
    public static String LOG_PREFIX = "[" + CLIENT_NAME + "]";
    public static MinecraftClient mc;
    public static final Logger LOGGER;
    public static final EventHandler eventHandler;

    public void onInitialize() {
        this.setClientStatus();
        LOGGER.info(LOG_PREFIX + "  Welcome to " + CLIENT_NAME);
        LOGGER.info(LOG_PREFIX + "  " + CLIENT_NAME + " v" + CLIENT_VERSION + " - " + GIT_HASH + " (" + CLIENT_STATUS + ")");
    }

    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        Managers.initialize();
        eventHandler.subscribe(this);
        Runtime.getRuntime().addShutdownHook(new Thread(Managers::shutdown, "r0se-shutdown"));
    }

    private void setClientStatus() {
        try {
            String[] parts = CLIENT_VERSION.split("\\.");
            if (parts.length < 3) {
                return;
            }
            int patch = Integer.parseInt(parts[2]);
            CLIENT_STATUS = patch == 0 ? "Release" : "Beta";
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        if (R0SE.mc.world == null) {
            return;
        }
        if (event.isPressed()) {
            Screen current = R0SE.mc.currentScreen;
            if (event.getKey() == 256 && current instanceof ClickGuiScreen) {
                Managers.MODULES.getFeature(ClickGui.class).disable();
            }
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger((String)CLIENT_NAME);
        eventHandler = new EventHandler();
    }
}


