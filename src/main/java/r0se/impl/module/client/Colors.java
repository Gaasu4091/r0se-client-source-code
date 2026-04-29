/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.module.client;

import java.awt.Color;
import r0se.api.feature.ConcurrentFeature;
import r0se.api.feature.FeatureCategory;
import r0se.api.settings.ColorSetting;

public class Colors
extends ConcurrentFeature {
    public final ColorSetting globalColor = this.addSetting(new ColorSetting("Global", new Color(83, 90, 196, 255)));
    public final ColorSetting friendColor = this.addSetting(new ColorSetting("Friend", new Color(85, 255, 255, 255)));
    public final ColorSetting enemyColor = this.addSetting(new ColorSetting("Enemy", new Color(255, 85, 85, 255)));

    public Colors() {
        super("Colors", "Customizes the client color scheme", FeatureCategory.CLIENT, "colors", "colour");
    }

    public Color getStyledGlobalColor() {
        return (Color)this.globalColor.getValue();
    }

    public Color getStyledGlobalColor(int alpha) {
        Color base = this.getStyledGlobalColor();
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), Colors.clamp(alpha));
    }

    public Color getStyledSecondColor() {
        Color base = this.getStyledGlobalColor();
        return Colors.darken(base, 0.32f);
    }

    public Color getStyledSecondColor(int alpha) {
        Color base = this.getStyledSecondColor();
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), Colors.clamp(alpha));
    }

    public Color getStyledTextColor(int alpha) {
        return new Color(255, 255, 255, Colors.clamp(alpha));
    }

    public Color getStyledTextSecondColor(int alpha) {
        return new Color(190, 190, 190, Colors.clamp(alpha));
    }

    private static Color darken(Color color, float factor) {
        return new Color(Math.max(0, Math.round((float)color.getRed() * (1.0f - factor))), Math.max(0, Math.round((float)color.getGreen() * (1.0f - factor))), Math.max(0, Math.round((float)color.getBlue() * (1.0f - factor))), color.getAlpha());
    }

    private static int clamp(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }
}

