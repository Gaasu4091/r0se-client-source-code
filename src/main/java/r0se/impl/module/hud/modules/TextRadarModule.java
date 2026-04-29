/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.entity.Entity
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 */
package r0se.impl.module.hud.modules;

import java.awt.Color;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import r0se.R0SE;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.DoubleSetting;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class TextRadarModule
extends HudModuleFeature {
    private final DoubleSetting limit = this.addSetting(new DoubleSetting("Limit", 8.0, 0.0, 64.0).precision(0));
    private final BoolSetting distance = this.addSetting(new BoolSetting("Distance", true));
    private final BoolSetting health = this.addSetting(new BoolSetting("Health", true));
    private final DecimalFormat decimal = new DecimalFormat("0.0");

    public TextRadarModule() {
        super("TextRadar", "Renders nearby players as text", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.world == null || R0SE.mc.player == null) {
            this.clearBounds();
            return;
        }
        int max = Math.max(0, (int)Math.round((Double)this.limit.getValue()));
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Color> colors = new ArrayList<Color>();
        for (AbstractClientPlayerEntity player2 : R0SE.mc.world.getPlayers().stream().filter(player -> player != R0SE.mc.player).sorted(Comparator.comparingDouble(player -> R0SE.mc.player.distanceTo((Entity)player))).toList()) {
            if (max > 0 && lines.size() >= max) break;
            StringBuilder line = new StringBuilder();
            line.append((int)Math.ceil(player2.getHealth() + player2.getAbsorptionAmount())).append(" ").append(player2.getName().getString());
            if (((Boolean)this.distance.getValue()).booleanValue()) {
                line.append(Formatting.GRAY).append(" ").append(this.decimal.format(R0SE.mc.player.distanceTo((Entity)player2))).append("m");
            }
            if (((Boolean)this.health.getValue()).booleanValue()) {
                line.append(Formatting.WHITE).append(" ").append(this.decimal.format(player2.getHealth() + player2.getAbsorptionAmount()));
            }
            lines.add(line.toString());
            colors.add(Managers.SOCIAL.isFriend(player2.getName().getString()) ? Managers.COLORS.getFriend() : (Managers.SOCIAL.isEnemy(player2.getName().getString()) ? Managers.COLORS.getEnemy() : Managers.COLORS.getAccent()));
        }
        Point point = this.resolvePosition(this.safeX(), this.safeY() + this.lineStep() * 4);
        this.renderList(context, point, lines, colors);
    }

    private void renderList(DrawContext context, Point point, List<String> lines, List<Color> colors) {
        boolean bottom;
        if (lines.isEmpty()) {
            this.clearBounds();
            return;
        }
        boolean right = point.x > R0SE.mc.getWindow().getScaledWidth() / 2;
        boolean bl = bottom = point.y > R0SE.mc.getWindow().getScaledHeight() / 2;
        if (bottom) {
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
            int width = R0SE.mc.textRenderer.getWidth(line);
            int x = right ? point.x - width : point.x;
            int y = startY + i * this.lineStep();
            context.drawText(R0SE.mc.textRenderer, line, x, y, colors.get(i).getRGB(), true);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + width);
            Objects.requireNonNull(R0SE.mc.textRenderer);
            maxY = Math.max(maxY, y + 9);
        }
        this.setBounds(minX, minY, maxX - minX, maxY - minY);
    }
}


