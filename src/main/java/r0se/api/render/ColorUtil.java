/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.render;

import java.awt.Color;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static int withAlpha(int color, int alpha) {
        return (ColorUtil.clamp(alpha) & 0xFF) << 24 | color & 0xFFFFFF;
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), ColorUtil.clamp(alpha));
    }

    public static Color scaleAlpha(Color color, float factor) {
        return ColorUtil.withAlpha(color, (int)((float)color.getAlpha() * Math.max(0.0f, Math.min(1.0f, factor))));
    }

    public static int lerp(int start, int end, float delta) {
        int sa = start >> 24 & 0xFF;
        int sr = start >> 16 & 0xFF;
        int sg = start >> 8 & 0xFF;
        int sb = start & 0xFF;
        int ea = end >> 24 & 0xFF;
        int er = end >> 16 & 0xFF;
        int eg = end >> 8 & 0xFF;
        int eb = end & 0xFF;
        int a = (int)((float)sa + (float)(ea - sa) * delta);
        int r = (int)((float)sr + (float)(er - sr) * delta);
        int g = (int)((float)sg + (float)(eg - sg) * delta);
        int b = (int)((float)sb + (float)(eb - sb) * delta);
        return ColorUtil.rgba(r, g, b, a);
    }

    public static Color lerp(Color start, Color end, float delta) {
        float clamped = Math.max(0.0f, Math.min(1.0f, delta));
        int red = (int)((float)start.getRed() + (float)(end.getRed() - start.getRed()) * clamped);
        int green = (int)((float)start.getGreen() + (float)(end.getGreen() - start.getGreen()) * clamped);
        int blue = (int)((float)start.getBlue() + (float)(end.getBlue() - start.getBlue()) * clamped);
        int alpha = (int)((float)start.getAlpha() + (float)(end.getAlpha() - start.getAlpha()) * clamped);
        return new Color(red, green, blue, alpha);
    }
}

