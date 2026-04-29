/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectUtil
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class EffectsModule
extends HudModuleFeature {
    public EffectsModule() {
        super("Effects", "Renders active potion effects", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.player == null || R0SE.mc.world == null) {
            this.clearBounds();
            return;
        }
        ArrayList<String> lines = new ArrayList<String>();
        for (StatusEffectInstance effect : R0SE.mc.player.getStatusEffects().stream().sorted(Comparator.comparing(e -> e.getEffectType().value().getName().getString(), String.CASE_INSENSITIVE_ORDER)).toList()) {
            String duration = StatusEffectUtil.getDurationText((StatusEffectInstance)effect, (float)1.0f, (float)R0SE.mc.world.getTickManager().getTickRate()).getString();
            String infinite = Text.translatable((String)"effect.duration.infinite").getString();
            lines.add(effect.getEffectType().value().getName().getString() + " " + (effect.getAmplifier() + 1) + " " + String.valueOf(Formatting.WHITE) + duration.replace(infinite, "**:**"));
        }
        Point point = this.resolvePosition(R0SE.mc.getWindow().getScaledWidth() - this.safeX(), R0SE.mc.getWindow().getScaledHeight() / 2 - 20);
        this.renderList(context, point, lines);
    }

    private void renderList(DrawContext context, Point point, List<String> lines) {
        boolean bottom;
        if (lines.isEmpty()) {
            this.clearBounds();
            return;
        }
        boolean right = point.x > R0SE.mc.getWindow().getScaledWidth() / 2;
        boolean bl = bottom = point.y > R0SE.mc.getWindow().getScaledHeight() / 2;
        if (bottom) {
            Collections.reverse(lines);
        }
        int startY = bottom ? point.y - (lines.size() - 1) * this.lineStep() : point.y;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            int width = R0SE.mc.textRenderer.getWidth(line);
            int x = right ? point.x - width : point.x;
            int y = startY + i * this.lineStep();
            this.drawHudText(context, line, x, y, Managers.COLORS.getAccent());
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + width);
            Objects.requireNonNull(R0SE.mc.textRenderer);
            maxY = Math.max(maxY, y + 9);
        }
        this.setBounds(minX, minY, maxX - minX, maxY - minY);
    }
}


