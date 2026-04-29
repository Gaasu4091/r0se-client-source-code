/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.math.Direction
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class DirectionModule
extends HudModuleFeature {
    public DirectionModule() {
        super("Direction", "Renders movement direction", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.player == null) {
            this.clearBounds();
            return;
        }
        String facing = DirectionModule.capitalize(R0SE.mc.player.getMovementDirection().getName());
        String axis = switch (R0SE.mc.player.getMovementDirection()) {
            case Direction.NORTH -> "-Z";
            case Direction.SOUTH -> "+Z";
            case Direction.EAST -> "+X";
            case Direction.WEST -> "-X";
            default -> "N/A";
        };
        List<String> lines = List.of(String.valueOf(Formatting.GRAY) + facing + String.valueOf(Formatting.WHITE) + " [" + axis + "]");
        this.renderList(context, this.resolvePosition(this.safeX(), R0SE.mc.getWindow().getScaledHeight() - this.safeY() - this.lineStep() * 2 - 2), lines);
    }

    private void renderList(DrawContext context, Point point, List<String> lines) {
        boolean bottom;
        boolean right = point.x > R0SE.mc.getWindow().getScaledWidth() / 2;
        boolean bl = bottom = point.y > R0SE.mc.getWindow().getScaledHeight() / 2;
        if (bottom) {
            lines = new ArrayList<String>(lines);
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

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.substring(0, 1).toUpperCase(Locale.ROOT) + input.substring(1).toLowerCase(Locale.ROOT);
    }
}


