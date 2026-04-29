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
import java.util.Locale;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import r0se.api.feature.BindMode;
import r0se.api.feature.ToggleableFeature;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class ModuleBindPanel
extends BasePanel {
    public static final int HEIGHT = 13;
    private static final int PADDING = 4;
    private static ToggleableFeature listeningFeature;
    private final ToggleableFeature feature;

    public ModuleBindPanel(ToggleableFeature feature) {
        this.feature = feature;
        this.height = 13;
    }

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        Color textColor = this.getTextColor();
        int textY = this.y + 2;
        int textX = this.x + 4 - 2;
        String label = listeningFeature == this.feature ? ((BindMode)((Object)this.feature.getBindMode().getValue())).toString() : (hovered ? ((BindMode)((Object)this.feature.getBindMode().getValue())).toString() : "Key");
        context.drawText(font, label, textX, textY, ModuleBindPanel.rgba(textColor), true);
        String value = listeningFeature == this.feature ? "..." : ModuleBindPanel.keyName((Integer)this.feature.getKeyBind().getValue());
        int valueX = this.x + this.width - 4 - font.getWidth(value);
        context.drawText(font, value, valueX, textY, ModuleBindPanel.rgba(textColor), true);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (listeningFeature == this.feature) {
            this.feature.getKeyBind().setValue(button);
            listeningFeature = null;
            return true;
        }
        if (button == 0) {
            listeningFeature = this.feature;
            return true;
        }
        if (button == 1) {
            this.cycleMode();
            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(int keyCode) {
        if (listeningFeature != this.feature) {
            return;
        }
        if (keyCode == 261) {
            this.feature.getKeyBind().setValue(-1);
        } else if (keyCode != 256) {
            this.feature.getKeyBind().setValue(keyCode);
        }
        listeningFeature = null;
    }

    @Override
    protected String getName() {
        return "Bind";
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

    private void cycleMode() {
        BindMode[] values = (BindMode[])this.feature.getBindMode().getValues();
        BindMode current = (BindMode)((Object)this.feature.getBindMode().getValue());
        int next = Math.floorMod(current.ordinal() + 1, values.length);
        this.feature.getBindMode().setValue(values[next]);
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
        String glfwName = GLFW.glfwGetKeyName((int)keyCode, (int)0);
        if (glfwName != null) {
            return glfwName.toUpperCase(Locale.ROOT);
        }
        return switch (keyCode) {
            case 344 -> "RSHIFT";
            case 340 -> "LSHIFT";
            case 345 -> "RCTRL";
            case 341 -> "LCTRL";
            case 346 -> "RALT";
            case 342 -> "LALT";
            case 32 -> "SPACE";
            case 258 -> "TAB";
            case 257 -> "ENTER";
            case 259 -> "BACKSPACE";
            case 261 -> "DELETE";
            case 260 -> "INSERT";
            case 268 -> "HOME";
            case 269 -> "END";
            case 266 -> "PGUP";
            case 267 -> "PGDN";
            case 265 -> "UP";
            case 264 -> "DOWN";
            case 263 -> "LEFT";
            case 262 -> "RIGHT";
            case 280 -> "CAPS";
            case 281 -> "SCROLL";
            case 282 -> "NUMLOCK";
            case 283 -> "PRTSC";
            case 284 -> "PAUSE";
            case 348 -> "MENU";
            case 343 -> "LWIN";
            case 347 -> "RWIN";
            case 256 -> "ESC";
            default -> keyCode >= 290 && keyCode <= 314 ? "F" + (keyCode - 290 + 1) : "None";
        };
    }

    private static int rgba(Color color) {
        return (color.getAlpha() & 0xFF) << 24 | (color.getRed() & 0xFF) << 16 | (color.getGreen() & 0xFF) << 8 | color.getBlue() & 0xFF;
    }
}


