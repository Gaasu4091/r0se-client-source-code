/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 */
package r0se.api.inventory;

import net.minecraft.item.ItemStack;

public record ItemSlot(int slot, ItemStack itemStack) {
    public boolean isValid() {
        return this.slot >= 0 && this.itemStack != null && !this.itemStack.isEmpty();
    }
}


