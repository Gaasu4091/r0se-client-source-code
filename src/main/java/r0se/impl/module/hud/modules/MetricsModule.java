/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.PlayerListEntry
 */
package r0se.impl.module.hud.modules;

import java.awt.Color;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import r0se.R0SE;
import r0se.api.settings.BoolSetting;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class MetricsModule
extends HudModuleFeature {
    private final BoolSetting ping = this.addSetting(new BoolSetting("Ping", true));
    private final BoolSetting fps = this.addSetting(new BoolSetting("FPS", true));
    private final BoolSetting tps = this.addSetting(new BoolSetting("TPS", true));
    private final BoolSetting speed = this.addSetting(new BoolSetting("Speed", true));
    private final BoolSetting serverBrand = this.addSetting(new BoolSetting("ServerBrand", true));
    private final DecimalFormat speedDecimal = new DecimalFormat("0.00");
    private final DecimalFormat decimal = new DecimalFormat("0.00");

    public MetricsModule() {
        super("Metrics", "Renders client metrics", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.player == null) {
            this.clearBounds();
            return;
        }
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<Color> colors = new ArrayList<Color>();
        Color accent = Managers.COLORS.getAccent();
        if (((Boolean)this.serverBrand.getValue()).booleanValue()) {
            lines.add("ServerBrand " + String.valueOf(Formatting.WHITE) + this.getServerBrand());
            colors.add(accent);
        }
        if (((Boolean)this.speed.getValue()).booleanValue()) {
            lines.add("Speed " + String.valueOf(Formatting.WHITE) + this.speedDecimal.format(this.getSpeed() * 3.6) + "km/h");
            colors.add(accent);
        }
        if (((Boolean)this.tps.getValue()).booleanValue()) {
            lines.add("TPS " + String.valueOf(Formatting.WHITE) + this.decimal.format(R0SE.mc.world == null ? 20.0 : (double)R0SE.mc.world.getTickManager().getTickRate()));
            colors.add(accent);
        }
        if (((Boolean)this.ping.getValue()).booleanValue()) {
            lines.add("Ping " + String.valueOf(Formatting.WHITE) + this.getPing() + "ms");
            colors.add(accent);
        }
        if (((Boolean)this.fps.getValue()).booleanValue()) {
            lines.add("FPS " + String.valueOf(Formatting.WHITE) + R0SE.mc.getCurrentFps());
            colors.add(accent);
        }
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (int i2 = 0; i2 < lines.size(); ++i2) {
            order.add(i2);
        }
        order.sort(Comparator.comparingInt(i -> R0SE.mc.textRenderer.getWidth(lines.get((int)i))));
        ArrayList<String> sortedLines = new ArrayList<String>();
        ArrayList<Color> sortedColors = new ArrayList<Color>();
        Iterator iterator = order.iterator();
        while (iterator.hasNext()) {
            int idx = (Integer)iterator.next();
            sortedLines.add(lines.get(idx));
            sortedColors.add(colors.get(idx));
        }
        Point point = this.resolvePosition(R0SE.mc.getWindow().getScaledWidth() - this.safeX(), R0SE.mc.getWindow().getScaledHeight() - this.safeY() - this.lineStep() * 4);
        this.renderTextList(context, point, sortedLines, sortedColors, false, "metrics");
    }

    private int getPing() {
        if (R0SE.mc.getNetworkHandler() == null || R0SE.mc.player == null) {
            return 0;
        }
        PlayerListEntry entry = R0SE.mc.getNetworkHandler().getPlayerListEntry(R0SE.mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    private String getServerBrand() {
        if (R0SE.mc.player == null || R0SE.mc.player.networkHandler == null) {
            return "Unknown";
        }
        String brand = R0SE.mc.player.networkHandler.getBrand();
        return brand == null ? "Unknown" : brand;
    }

    private double getSpeed() {
        double dx = R0SE.mc.player.getX() - R0SE.mc.player.prevX;
        double dz = R0SE.mc.player.getZ() - R0SE.mc.player.prevZ;
        return Math.sqrt(dx * dx + dz * dz) * 20.0;
    }
}


