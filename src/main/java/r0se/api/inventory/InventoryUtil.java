/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.PlayerScreenHandler
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.collection.DefaultedList
 *  net.minecraft.client.MinecraftClient
 */
package r0se.api.inventory;

import java.util.function.Predicate;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.client.MinecraftClient;
import r0se.api.inventory.ItemSlot;
import r0se.api.inventory.SilentSwapType;

public final class InventoryUtil {
    public static final int INVALID_SLOT = -1;
    public static final int OFFHAND_PACKET_SLOT = 45;

    private InventoryUtil() {
    }

    public static ItemSlot getItem(Item item, SilentSwapType type) {
        return type == SilentSwapType.INVENTORY ? InventoryUtil.getInventoryItem(item) : InventoryUtil.getHotbarItem(item);
    }

    public static int getItemSlot(Item item, SilentSwapType type) {
        return InventoryUtil.getItem(item, type).slot();
    }

    public static ItemSlot getInventoryItem(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return new ItemSlot(-1, ItemStack.EMPTY);
        }
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() != item) continue;
            return new ItemSlot(i, stack);
        }
        return new ItemSlot(-1, ItemStack.EMPTY);
    }

    public static ItemSlot getHotbarItem(Item item) {
        return InventoryUtil.getHotbarItem((ItemStack stack) -> stack.getItem() == item);
    }

    public static ItemSlot getHotbarItem(Predicate<ItemStack> predicate) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return new ItemSlot(-1, ItemStack.EMPTY);
        }
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty() || !predicate.test(stack)) continue;
            return new ItemSlot(i, stack);
        }
        return new ItemSlot(-1, ItemStack.EMPTY);
    }

    public static int find(Predicate<ItemStack> predicate) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return -1;
        }
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.main.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty() || !predicate.test(stack)) continue;
            return i;
        }
        return -1;
    }

    public static int count(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return 0;
        }
        int count = 0;
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.main.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() != item) continue;
            count += stack.getCount();
        }
        ItemStack offhand = (ItemStack)inventory.offHand.getFirst();
        if (offhand.getItem() == item) {
            count += offhand.getCount();
        }
        return count;
    }

    public static int getPacketSlotIndex(ScreenHandler handler, int slot) {
        if (handler instanceof PlayerScreenHandler) {
            if (slot == 40) {
                return 45;
            }
            if (slot < PlayerInventory.getHotbarSize()) {
                return slot + 36;
            }
            return slot;
        }
        DefaultedList slots = handler.slots;
        for (int id = 0; id < slots.size(); ++id) {
            Slot screenSlot = (Slot)slots.get(id);
            if (screenSlot.getIndex() != slot) continue;
            return id;
        }
        return slot;
    }
}


