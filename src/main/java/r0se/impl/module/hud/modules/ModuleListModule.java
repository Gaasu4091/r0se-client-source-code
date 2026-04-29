/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.api.feature.ToggleableFeature;
import r0se.api.render.Easing;
import r0se.api.render.animation.ValueAnimation;
import r0se.api.settings.BoolSetting;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class ModuleListModule
extends HudModuleFeature {
    private final BoolSetting metadata = this.addSetting(new BoolSetting("MetaData", true));
    private final Map<String, ValueAnimation> widthAnimations = new LinkedHashMap<String, ValueAnimation>();

    public ModuleListModule() {
        super("ModuleList", "Renders enabled modules", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.textRenderer == null) {
            this.clearBounds();
            return;
        }
        TextRenderer renderer = R0SE.mc.textRenderer;
        List<ToggleableFeature> features = Managers.MODULES.getToggleableFeatures().stream().filter(feature -> feature != this).filter(ToggleableFeature::isEnabled).filter(ToggleableFeature::isDrawn).sorted((left, right) -> Integer.compare(renderer.getWidth(this.getText((ToggleableFeature)right)), renderer.getWidth(this.getText((ToggleableFeature)left)))).toList();
        Point point = this.resolvePosition(R0SE.mc.getWindow().getScaledWidth() - this.safeX(), this.safeY());
        boolean right2 = point.x > R0SE.mc.getWindow().getScaledWidth() / 2;
        boolean bottom = point.y > R0SE.mc.getWindow().getScaledHeight() / 2;
        ArrayList<ToggleableFeature> ordered = new ArrayList<ToggleableFeature>(features);
        if (bottom) {
            Collections.reverse(ordered);
        }
        int startY = bottom ? point.y - Math.max(0, ordered.size() - 1) * this.lineStep() : point.y;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int index = 0;
        for (ToggleableFeature feature2 : ordered) {
            String text = this.getText(feature2);
            int textWidth = renderer.getWidth(text);
            ValueAnimation animation = this.widthAnimations.computeIfAbsent(feature2.getIdentifier(), ignored -> new ValueAnimation(0.0f, 220.0f, Easing.CUBIC_OUT));
            int animatedWidth = Math.round(animation.get((float)textWidth + 2.0f));
            int x = right2 ? point.x - animatedWidth : point.x;
            int y = startY + index * this.lineStep();
            context.drawText(renderer, text, x, y, Managers.COLORS.getAccent().getRGB(), true);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + Math.max(textWidth, animatedWidth));
            Objects.requireNonNull(renderer);
            maxY = Math.max(maxY, y + 9);
            ++index;
        }
        this.widthAnimations.entrySet().removeIf(entry -> Managers.MODULES.getFeature((String)entry.getKey()) == null);
        if (ordered.isEmpty()) {
            this.clearBounds();
        } else {
            this.setBounds(minX, minY, maxX - minX, maxY - minY);
        }
    }

    private String getText(ToggleableFeature feature) {
        return feature.getName() + (String)((Boolean)this.metadata.getValue() != false && !feature.getMetaData().isEmpty() ? String.valueOf(Formatting.GRAY) + " [" + String.valueOf(Formatting.WHITE) + feature.getMetaData() + String.valueOf(Formatting.GRAY) + "]" : "");
    }
}


