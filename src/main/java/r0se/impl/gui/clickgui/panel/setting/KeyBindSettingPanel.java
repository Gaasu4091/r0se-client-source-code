/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  org.lwjgl.glfw.GLFW
 */
package r0se.impl.gui.clickgui.panel.setting;

import java.awt.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import r0se.api.settings.KeyBindSetting;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class KeyBindSettingPanel
extends BasePanel {
    public static final int HEIGHT = 13;
    private static final int PADDING = 4;
    private static KeyBindSetting waitingForKeyBind;
    private final KeyBindSetting setting;

    public KeyBindSettingPanel(KeyBindSetting setting) {
        this.setting = setting;
        this.height = 13;
    }

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        Color textColor = this.getTextColor();
        int textY = this.y + 2;
        int textX = this.x + 4 - 2;
        context.drawText(font, this.getName(), textX, textY, KeyBindSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
        String value = waitingForKeyBind == this.setting ? "Listening..." : KeyBindSettingPanel.keyName((Integer)this.setting.getValue());
        int valueX = this.x + this.width - 4 - font.getWidth(value);
        context.drawText(font, value, valueX, textY, KeyBindSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (waitingForKeyBind == this.setting) {
            this.setting.setValue(button);
            waitingForKeyBind = null;
            return true;
        }
        if (button == 0) {
            waitingForKeyBind = this.setting;
            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (waitingForKeyBind != this.setting) {
            return;
        }
        if (keyCode == 261 || keyCode == 256) {
            this.setting.setValue(-1);
        } else {
            this.setting.setValue(keyCode);
        }
        waitingForKeyBind = null;
    }

    @Override
    protected String getName() {
        return this.setting.getName();
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    protected Colors getColorFeature() {
        return Managers.MODULES.getFeature(Colors.class);
    }

    @Override
    protected ClickGui getClickGuiFeature() {
        return Managers.MODULES.getFeature(ClickGui.class);
    }

    @Override
    protected Color getTextColor() {
        return this.getColorFeature().getStyledTextColor(255);
    }

    private static String keyName(int keyCode) {
        if (keyCode == -1) {
            return "None";
        }
        if (keyCode >= 0 && keyCode <= 7) {
            return "Mouse" + (keyCode - 0 + 1);
        }
        if (keyCode < 32 || keyCode > 348) {
            return "None";
        }
        String name = GLFW.glfwGetKeyName((int)keyCode, (int)0);
        if (name != null) {
            return name.toUpperCase();
        }
        return switch (keyCode) {
            case 344 -> "RSHIFT";
            case 340 -> "LSHIFT";
            case 345 -> "RCTRL";
            case 341 -> "LCTRL";
            case 346 -> "RALT";
            case 342 -> "LALT";
            case 32 -> "SPACE";
            default -> "KEY_" + keyCode;
        };
    }

    private static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
}


