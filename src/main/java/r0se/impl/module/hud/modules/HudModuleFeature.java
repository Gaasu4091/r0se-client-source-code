/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.render.RenderOverlayEvent;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.render.Easing;
import r0se.api.render.animation.ValueAnimation;
import r0se.impl.module.hud.HUD;
import r0se.manager.Managers;

public abstract class HudModuleFeature
extends ToggleableFeature {
    private final Map<String, ValueAnimation> lineWidthAnimations = new LinkedHashMap<String, ValueAnimation>();
    private Double relativeX;
    private Double relativeY;
    private Point position;
    private Rectangle editorBounds;

    protected HudModuleFeature(String name, String description, String ... aliases) {
        super(name, description, FeatureCategory.HUD, aliases);
        this.getDrawn().setValue(false);
        this.getNotify().setValue(false);
    }

    @Override
    public void onRegistered() {
        this.enable();
    }

    @Subscribe
    public void onRenderOverlay(RenderOverlayEvent event) {
        this.renderHud(event.getContext(), false);
    }

    public void renderEditorPreview(DrawContext context) {
        if (!this.isEnabled()) {
            this.clearBounds();
            return;
        }
        this.renderHud(context, true);
    }

    protected abstract void renderHud(DrawContext var1, boolean var2);

    protected Point resolvePosition(int defaultX, int defaultY) {
        int width = R0SE.mc.getWindow().getScaledWidth();
        int height = R0SE.mc.getWindow().getScaledHeight();
        if (this.position == null) {
            this.setEditorPosition(defaultX, defaultY);
        } else if (this.relativeX != null && this.relativeY != null) {
            this.position = new Point((int)Math.round(this.relativeX * (double)width), (int)Math.round(this.relativeY * (double)height));
        }
        return this.position;
    }

    public Point getEditorPosition() {
        return this.position == null ? null : new Point(this.position);
    }

    public Rectangle getEditorBounds() {
        return this.editorBounds == null ? null : new Rectangle(this.editorBounds);
    }

    public void setEditorPosition(int x, int y) {
        int width = Math.max(1, R0SE.mc.getWindow().getScaledWidth());
        int height = Math.max(1, R0SE.mc.getWindow().getScaledHeight());
        this.position = new Point(x, y);
        this.relativeX = (double)x / (double)width;
        this.relativeY = (double)y / (double)height;
    }

    public void setEditorBoundsPosition(int left, int top) {
        if (this.editorBounds == null || this.position == null) {
            this.setEditorPosition(left, top);
            return;
        }
        this.setEditorPosition(left + (this.position.x - this.editorBounds.x), top + (this.position.y - this.editorBounds.y));
    }

    protected void setBounds(int x, int y, int width, int height) {
        this.editorBounds = new Rectangle(x, y, Math.max(1, width), Math.max(1, height));
    }

    protected void clearBounds() {
        this.editorBounds = null;
    }

    protected HUD layout() {
        return Managers.MODULES.getFeature(HUD.class);
    }

    protected int safeX() {
        HUD feature = this.layout();
        return feature == null ? 2 : feature.getSafeX();
    }

    protected int safeY() {
        HUD feature = this.layout();
        return feature == null ? 2 : feature.getSafeY();
    }

    protected int lineStep() {
        int n;
        HUD feature = this.layout();
        if (feature == null) {
            Objects.requireNonNull(R0SE.mc.textRenderer);
            n = 9;
        } else {
            n = feature.getLineStep();
        }
        return n;
    }

    protected int bottomOffset(boolean chatOffset) {
        HUD feature = this.layout();
        return feature == null ? 2 : feature.getBottomOffset(chatOffset);
    }

    protected void drawHudText(DrawContext context, String text, int x, int y, Color color) {
        context.drawText(R0SE.mc.textRenderer, text, x, y, color.getRGB(), true);
    }

    protected Rectangle renderTextList(DrawContext context, Point point, List<String> lines, List<Color> colors, boolean reverseWhenBottom, String animationKeyPrefix) {
        boolean bottom;
        if (lines.isEmpty()) {
            this.clearBounds();
            return null;
        }
        boolean right = point.x > R0SE.mc.getWindow().getScaledWidth() / 2;
        boolean bl = bottom = point.y > R0SE.mc.getWindow().getScaledHeight() / 2;
        if (bottom && reverseWhenBottom) {
            lines = new ArrayList<String>(lines);
            colors = new ArrayList<Color>(colors);
            Collections.reverse(lines);
            Collections.reverse(colors);
        }
        int startY = bottom ? point.y - (lines.size() - 1) * this.lineStep() : point.y;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            int textWidth = R0SE.mc.textRenderer.getWidth(line);
            ValueAnimation animation = this.lineWidthAnimations.computeIfAbsent(animationKeyPrefix + ":" + i, ignored -> new ValueAnimation(textWidth, 180.0f, Easing.CUBIC_OUT));
            int animatedWidth = Math.round(animation.get(textWidth));
            int x = right ? point.x - animatedWidth : point.x;
            int y = startY + i * this.lineStep();
            this.drawHudText(context, line, x, y, colors.get(Math.min(i, colors.size() - 1)));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + Math.max(textWidth, animatedWidth));
            Objects.requireNonNull(R0SE.mc.textRenderer);
            maxY = Math.max(maxY, y + 9);
        }
        Rectangle bounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        this.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        return bounds;
    }
}


