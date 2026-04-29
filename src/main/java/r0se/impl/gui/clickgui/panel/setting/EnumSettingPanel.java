/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.clickgui.panel.setting;

import java.awt.Color;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.api.settings.EnumSetting;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class EnumSettingPanel<E extends Enum<E>>
extends BasePanel {
    public static final int HEIGHT = 13;
    private static final int PADDING = 4;
    private final EnumSetting<E> setting;
    private float enabledFactor;

    public EnumSettingPanel(EnumSetting<E> setting) {
        this.setting = setting;
        this.height = 13;
        this.enabledFactor = 1.0f;
    }

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        this.enabledFactor = GuiAnimation.approachToggle(this.enabledFactor, 1.0f);
        Colors colorFeature = this.getColorFeature();
        Color base = hovered ? colorFeature.getStyledSecondColor(88) : colorFeature.getStyledSecondColor(64);
        context.fill(this.x, this.y, this.x + this.width, this.y + this.height, EnumSettingPanel.rgba(base.getRed(), base.getGreen(), base.getBlue(), base.getAlpha()));
        if (this.enabledFactor > 0.001f) {
            Color active = colorFeature.getStyledGlobalColor();
            int filledWidth = Math.max(1, Math.round((float)this.width * this.enabledFactor));
            context.fill(this.x, this.y, this.x + filledWidth, this.y + this.height, EnumSettingPanel.rgba(active.getRed(), active.getGreen(), active.getBlue(), active.getAlpha()));
        }
        Color textColor = this.getTextColor();
        Objects.requireNonNull(font);
        int textY = this.y + (this.height - 9) / 2 + (hovered ? 0 : 1);
        int textX = this.x + 4 - 2;
        context.drawText(font, this.getName(), textX, textY, EnumSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
        String value = this.formatEnum(this.setting.getValue());
        int valueX = this.x + this.width - 4 - font.getWidth(value);
        context.drawText(font, value, valueX, textY, EnumSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
    }

    @Override
    public void onLeftClick() {
        this.cycle(false);
    }

    @Override
    public void onRightClick() {
        this.cycle(true);
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

    private void cycle(boolean reverse) {
        E[] values = this.setting.getValues();
        if (values.length == 0) {
            return;
        }
        E current = this.setting.getValue();
        int index = current.ordinal();
        int next = Math.floorMod(index + (reverse ? -1 : 1), values.length);
        this.setting.setValue(values[next]);
    }

    private String formatEnum(E value) {
        String raw = value.toString().toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        boolean uppercaseNext = true;
        for (int i = 0; i < raw.length(); ++i) {
            char c = raw.charAt(i);
            if (c == '_' || c == '-' || c == ' ') {
                uppercaseNext = true;
                continue;
            }
            builder.append(uppercaseNext ? Character.toUpperCase(c) : c);
            uppercaseNext = false;
        }
        return builder.toString();
    }

    private static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
}


