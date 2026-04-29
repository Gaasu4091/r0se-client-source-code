/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Point;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.impl.module.hud.modules.HudModuleFeature;

public class TotemsModule
extends HudModuleFeature {
    public TotemsModule() {
        super("Totems", "Renders the totem counter", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        String overlay;
        if (R0SE.mc == null || R0SE.mc.player == null) {
            this.clearBounds();
            return;
        }
        int count = R0SE.mc.player.getInventory().count(Items.TOTEM_OF_UNDYING);
        if (count <= 0 && !editor) {
            this.clearBounds();
            return;
        }
        Point point = this.resolvePosition(R0SE.mc.getWindow().getScaledWidth() / 2 - 9, R0SE.mc.getWindow().getScaledHeight() - 55);
        ItemStack stack = new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING);
        context.drawItem(stack, point.x, point.y);
        overlay = count > 1 ? String.valueOf(count) : (editor ? "1" : null);
        if (overlay != null) {
            int textX = point.x + 17 - R0SE.mc.textRenderer.getWidth(overlay);
            int textY = point.y + 9;
            int n = textX + R0SE.mc.textRenderer.getWidth(overlay) + 1;
            Objects.requireNonNull(R0SE.mc.textRenderer);
            context.fill(textX - 1, textY - 1, n, textY + 9 - 1, 0x70000000);
            context.drawText(R0SE.mc.textRenderer, overlay, textX, textY, -1, true);
        }
        this.setBounds(point.x, point.y, 18, 18);
    }
}


