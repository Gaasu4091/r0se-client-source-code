/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.SharedConstants
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Point;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.api.settings.BoolSetting;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class WatermarkModule
extends HudModuleFeature {
    private final BoolSetting version = this.addSetting(new BoolSetting("Version", true));
    private final BoolSetting minecraftVersion = this.addSetting(new BoolSetting("MinecraftVersion", false));
    private final BoolSetting revision = this.addSetting(new BoolSetting("Revision", true));

    public WatermarkModule() {
        super("Watermark", "Renders the client watermark", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.textRenderer == null) {
            this.clearBounds();
            return;
        }
        StringBuilder text = new StringBuilder(R0SE.CLIENT_NAME);
        if (((Boolean)this.version.getValue()).booleanValue()) {
            text.append(Formatting.WHITE).append(" ").append(R0SE.CLIENT_VERSION);
            if (((Boolean)this.minecraftVersion.getValue()).booleanValue()) {
                text.append("-mc").append(SharedConstants.getGameVersion().getName());
            }
            if (((Boolean)this.revision.getValue()).booleanValue()) {
                text.append("+").append(R0SE.GIT_HASH, 0, Math.min(7, R0SE.GIT_HASH.length()));
            }
        }
        String draw = text.toString();
        Point point = this.resolvePosition(this.safeX(), this.safeY());
        context.drawText(R0SE.mc.textRenderer, draw, point.x, point.y, Managers.COLORS.getAccent().getRGB(), true);
        int n = point.x;
        int n2 = point.y;
        int n3 = R0SE.mc.textRenderer.getWidth(draw);
        Objects.requireNonNull(R0SE.mc.textRenderer);
        this.setBounds(n, n2, n3, 9);
    }
}


