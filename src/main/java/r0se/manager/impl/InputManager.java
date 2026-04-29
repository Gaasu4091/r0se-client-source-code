/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  org.lwjgl.glfw.GLFW
 */
package r0se.manager.impl;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.ToggleableFeature;
import r0se.impl.gui.clickgui.ClickGuiScreen;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class InputManager
implements Manager {
    private final Map<ToggleableFeature, Boolean> pressedStates = new HashMap<ToggleableFeature, Boolean>();

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.pressedStates.clear();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (R0SE.mc == null || R0SE.mc.getWindow() == null) {
            return;
        }
        if (R0SE.mc.currentScreen instanceof ChatScreen) {
            this.resetPressedStates();
            return;
        }
        long handle = R0SE.mc.getWindow().getHandle();
        boolean allowFeatureBinds = R0SE.mc.currentScreen == null || R0SE.mc.currentScreen instanceof ClickGuiScreen;
        for (ToggleableFeature feature : Managers.MODULES.getToggleableFeatures()) {
            int key = (Integer)feature.getKeyBind().getValue();
            if (key == -1) {
                this.pressedStates.put(feature, false);
                continue;
            }
            boolean pressed = this.isPressed(handle, key);
            boolean lastPressed = this.pressedStates.getOrDefault(feature, false);
            this.pressedStates.put(feature, pressed);
            if (!allowFeatureBinds) {
                if (!lastPressed) continue;
                feature.onBindReleased();
                continue;
            }
            if (pressed && !lastPressed) {
                feature.onBindPressed();
                continue;
            }
            if (pressed || !lastPressed) continue;
            feature.onBindReleased();
        }
    }

    private void resetPressedStates() {
        for (ToggleableFeature feature : Managers.MODULES.getToggleableFeatures()) {
            if (this.pressedStates.getOrDefault(feature, false).booleanValue()) {
                feature.onBindReleased();
            }
            this.pressedStates.put(feature, false);
        }
    }

    private boolean isPressed(long handle, int key) {
        if (key >= 0 && key <= 7) {
            return GLFW.glfwGetMouseButton((long)handle, (int)key) == 1;
        }
        return GLFW.glfwGetKey((long)handle, (int)key) == 1;
    }
}


