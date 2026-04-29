/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.world.World
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.api.settings.BoolSetting;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class PositionModule
extends HudModuleFeature {
    private final BoolSetting coordinates = this.addSetting(new BoolSetting("Coordinates", true));
    private final BoolSetting netherCoords = this.addSetting(new BoolSetting("NetherCoords", true));

    public PositionModule() {
        super("Position", "Renders coordinates", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.player == null) {
            this.clearBounds();
            return;
        }
        ArrayList<String> lines = new ArrayList<String>();
        if (((Boolean)this.coordinates.getValue()).booleanValue()) {
            StringBuilder text = new StringBuilder();
            text.append("XYZ ").append(Formatting.WHITE).append(R0SE.mc.player.getBlockX()).append(Formatting.GRAY).append(", ").append(Formatting.WHITE).append(R0SE.mc.player.getBlockY()).append(Formatting.GRAY).append(", ").append(Formatting.WHITE).append(R0SE.mc.player.getBlockZ());
            if (((Boolean)this.netherCoords.getValue()).booleanValue()) {
                text.append(Formatting.GRAY).append(" [").append(Formatting.WHITE).append(this.getNetherCoordinate(R0SE.mc.player.getBlockX())).append(Formatting.GRAY).append(", ").append(Formatting.WHITE).append(this.getNetherCoordinate(R0SE.mc.player.getBlockZ())).append(Formatting.GRAY).append("]");
            }
            lines.add(text.toString());
        }
        this.renderList(context, this.resolvePosition(this.safeX(), R0SE.mc.getWindow().getScaledHeight() - this.safeY() - this.lineStep() - 2), lines);
    }

    private int getNetherCoordinate(int coordinate) {
        return R0SE.mc.player.getWorld().getRegistryKey() == World.NETHER ? coordinate * 8 : coordinate / 8;
    }

    protected void renderList(DrawContext context, Point point, List<String> lines) {
        if (lines.isEmpty()) {
            this.clearBounds();
            return;
        }
        List<Color> colors = Collections.nCopies(lines.size(), Managers.COLORS.getAccent());
        this.renderTextList(context, point, lines, colors, true, "position");
    }
}


