/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.screen.PlayerScreenHandler
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 */
package r0se.manager.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.network.PacketOutboundEvent;
import r0se.api.event.world.TickEvent;
import r0se.api.inventory.InventoryUtil;
import r0se.api.inventory.ItemSlot;
import r0se.api.inventory.SilentSwapType;
import r0se.api.inventory.SwapMode;
import r0se.api.inventory.SwapSession;
import r0se.api.inventory.SwapState;
import r0se.impl.module.client.AntiCheat;
import r0se.impl.module.client.Inventory;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class InventoryManager
implements Manager {
    private static final long STALE_SWAP_TIMEOUT_MS = 500L;
    private final SwapState currentSwap = new SwapState();
    private int serverSlot;

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
        if (R0SE.mc.player != null) {
            this.serverSlot = R0SE.mc.player.getInventory().selectedSlot;
        }
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.endSwap();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (R0SE.mc.player == null) {
            this.currentSwap.reset();
            return;
        }
        int selectedSlot = R0SE.mc.player.getInventory().selectedSlot;
        if (!PlayerInventory.isValidHotbarIndex((int)this.serverSlot)) {
            this.serverSlot = selectedSlot;
        }
    }

    public int getSelectedSlot() {
        return R0SE.mc.player == null ? 0 : R0SE.mc.player.getInventory().selectedSlot;
    }

    public int getServerSlot() {
        return this.serverSlot;
    }

    public void setServerSlot(int slot) {
        if (PlayerInventory.isValidHotbarIndex((int)slot)) {
            this.serverSlot = slot;
        }
    }

    public boolean isSilentSwapping() {
        return R0SE.mc.player != null && PlayerInventory.isValidHotbarIndex((int)this.serverSlot) && this.serverSlot != R0SE.mc.player.getInventory().selectedSlot;
    }

    public boolean hasSwap() {
        return this.currentSwap.isActive();
    }

    public SwapState getCurrentSwap() {
        return this.currentSwap;
    }

    public SilentSwapType getSilentSwapType() {
        Inventory config = Managers.MODULES.getFeature(Inventory.class);
        return config == null ? SilentSwapType.HOTBAR : (SilentSwapType)((Object)config.getSilentSwapType().getValue());
    }

    public SwapMode getSwapMode() {
        Inventory config = Managers.MODULES.getFeature(Inventory.class);
        return config == null ? SwapMode.SILENT : (SwapMode)((Object)config.getSwapMode().getValue());
    }

    public void syncSelectedSlot() {
        if (R0SE.mc.player == null || R0SE.mc.getNetworkHandler() == null) {
            return;
        }
        int selectedSlot = R0SE.mc.player.getInventory().selectedSlot;
        if (selectedSlot != this.serverSlot) {
            R0SE.mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(selectedSlot));
            this.serverSlot = selectedSlot;
        }
    }

    public void setSelectedSlot(int slot) {
        if (R0SE.mc.player == null || !PlayerInventory.isValidHotbarIndex((int)slot)) {
            return;
        }
        if (slot != R0SE.mc.player.getInventory().selectedSlot) {
            R0SE.mc.player.getInventory().selectedSlot = slot;
        }
        if (slot != this.serverSlot && R0SE.mc.getNetworkHandler() != null) {
            R0SE.mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot));
            this.serverSlot = slot;
        }
    }

    public void setSlotSilent(int slot) {
        if (R0SE.mc.player == null || R0SE.mc.getNetworkHandler() == null || !PlayerInventory.isValidHotbarIndex((int)slot)) {
            return;
        }
        if (slot != this.serverSlot) {
            R0SE.mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot));
            this.serverSlot = slot;
        }
    }

    public void setSlotAlt(int slot) {
        if (R0SE.mc.player == null || !PlayerInventory.isValidHotbarIndex((int)slot)) {
            return;
        }
        PlayerScreenHandler handler = R0SE.mc.player.playerScreenHandler;
        int packetSlot = InventoryUtil.getPacketSlotIndex((ScreenHandler)handler, slot);
        this.swapSlot((ScreenHandler)handler, packetSlot, this.getSafeServerSlot());
        this.serverSlot = slot;
    }

    public boolean startSwap(int slot) {
        return this.startSwap(slot, this.getSwapMode());
    }

    public boolean startSwap(int slot, SilentSwapType type) {
        return this.startSwap(slot, SwapMode.SILENT, type);
    }

    public boolean startSwap(int slot, SwapMode mode) {
        SilentSwapType type = slot >= PlayerInventory.getHotbarSize() ? SilentSwapType.INVENTORY : this.getSilentSwapType();
        return this.startSwap(slot, mode, type);
    }

    public boolean startSwap(int slot, SwapMode mode, SilentSwapType type) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null) {
            this.debug("start failed reason=invalid_state slot=" + slot + " mode=" + String.valueOf((Object)mode) + " type=" + String.valueOf((Object)type));
            return false;
        }
        this.clearStaleSwap("start");
        if (mode == SwapMode.NONE) {
            boolean holding = this.isHoldingSlot(slot);
            this.debug("start none slot=" + slot + " holding=" + holding);
            return holding;
        }
        if (this.currentSwap.isActive()) {
            boolean reusable = this.currentSwap.getSlotTo() == slot && this.currentSwap.getMode() == mode && this.currentSwap.getType() == type;
            this.debug("start active slot=" + slot + " mode=" + String.valueOf((Object)mode) + " reusable=" + reusable + " activeSlot=" + this.currentSwap.getSlotTo() + " activeMode=" + String.valueOf((Object)this.currentSwap.getMode()));
            return reusable;
        }
        int selectedSlot = R0SE.mc.player.getInventory().selectedSlot;
        if (PlayerInventory.isValidHotbarIndex((int)slot) && slot == selectedSlot && slot == this.serverSlot) {
            this.debug("start skipped reason=already_holding slot=" + slot + " mode=" + String.valueOf((Object)mode) + " type=" + String.valueOf((Object)type) + " server=" + this.serverSlot);
            return true;
        }
        if (mode == SwapMode.NORMAL) {
            if (!PlayerInventory.isValidHotbarIndex((int)slot)) {
                this.debug("start failed reason=normal_requires_hotbar slot=" + slot);
                return false;
            }
            this.currentSwap.begin(selectedSlot, slot, SilentSwapType.HOTBAR, mode);
            this.setSelectedSlot(slot);
            this.debug("start slot=" + slot + " from=" + selectedSlot + " mode=" + String.valueOf((Object)mode) + " type=HOTBAR server=" + this.serverSlot);
            return true;
        }
        if (mode == SwapMode.SILENT_ALT) {
            if (!PlayerInventory.isValidHotbarIndex((int)slot)) {
                this.debug("start failed reason=alt_requires_hotbar slot=" + slot);
                return false;
            }
            this.currentSwap.begin(this.getSafeServerSlot(), slot, SilentSwapType.HOTBAR, mode);
            this.setSlotAlt(slot);
            this.debug("start slot=" + slot + " from=" + this.currentSwap.getSlotFrom() + " mode=" + String.valueOf((Object)mode) + " type=HOTBAR server=" + this.serverSlot);
            return true;
        }
        if (type == SilentSwapType.HOTBAR) {
            if (!PlayerInventory.isValidHotbarIndex((int)slot) || R0SE.mc.getNetworkHandler() == null) {
                this.debug("start failed reason=silent_hotbar_invalid slot=" + slot);
                return false;
            }
            this.currentSwap.begin(selectedSlot, slot, type, mode);
            this.setSlotSilent(slot);
            this.debug("start slot=" + slot + " from=" + selectedSlot + " mode=" + String.valueOf((Object)mode) + " type=" + String.valueOf((Object)type) + " server=" + this.serverSlot);
            return true;
        }
        if (slot < 0 || slot >= 36) {
            this.debug("start failed reason=inventory_slot_invalid slot=" + slot);
            return false;
        }
        this.currentSwap.begin(selectedSlot, slot, type, mode);
        this.swap(slot, selectedSlot);
        this.debug("start slot=" + slot + " from=" + selectedSlot + " mode=" + String.valueOf((Object)mode) + " type=" + String.valueOf((Object)type) + " server=" + this.serverSlot);
        return true;
    }

    public SwapSession swapTo(int slot) {
        return this.swapTo(slot, SwapMode.SILENT);
    }

    public SwapSession swapTo(int slot, SwapMode mode) {
        SilentSwapType type = slot >= PlayerInventory.getHotbarSize() ? SilentSwapType.INVENTORY : this.getSilentSwapType();
        return this.swapTo(slot, mode, type);
    }

    public SwapSession swapTo(int slot, SwapMode mode, SilentSwapType type) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null) {
            return SwapSession.invalid(slot, mode, type, "invalid_state");
        }
        this.clearStaleSwap("swapTo");
        if (mode == SwapMode.NONE) {
            boolean holding = this.isHoldingSlot(slot);
            return holding ? SwapSession.valid(slot, mode, type, false, null) : SwapSession.invalid(slot, mode, type, "not_holding_slot");
        }
        if (this.currentSwap.isActive()) {
            boolean reusable = this.currentSwap.getSlotTo() == slot && this.currentSwap.getMode() == mode && this.currentSwap.getType() == type;
            return reusable ? SwapSession.valid(slot, mode, type, false, null) : SwapSession.invalid(slot, mode, type, "swap_already_active");
        }
        boolean started = this.startSwap(slot, mode, type);
        return started ? SwapSession.valid(slot, mode, type, true, this::endSwap) : SwapSession.invalid(slot, mode, type, "start_failed");
    }

    @Subscribe(priority=9000)
    public void onPacketOutbound(PacketOutboundEvent event) {
        Packet<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof UpdateSelectedSlotC2SPacket)) {
            return;
        }
        UpdateSelectedSlotC2SPacket packet = (UpdateSelectedSlotC2SPacket)class_25962;
        int packetSlot = packet.getSelectedSlot();
        if (!PlayerInventory.isValidHotbarIndex((int)packetSlot) || packetSlot == this.serverSlot) {
            event.cancel();
            this.debug("slot_packet_cancel slot=" + packetSlot + " server=" + this.serverSlot);
            return;
        }
        this.serverSlot = packetSlot;
        this.debug("slot_packet_accept slot=" + packetSlot);
    }

    private void clearStaleSwap(String source) {
        if (!this.currentSwap.isActive()) {
            return;
        }
        long age = System.currentTimeMillis() - this.currentSwap.getStartedAt();
        if (age < 500L) {
            return;
        }
        this.debug("stale_clear source=" + source + " ageMs=" + age + " from=" + this.currentSwap.getSlotFrom() + " to=" + this.currentSwap.getSlotTo() + " mode=" + String.valueOf((Object)this.currentSwap.getMode()) + " type=" + String.valueOf((Object)this.currentSwap.getType()));
        this.endSwap();
    }

    public void endSwap() {
        if (!this.currentSwap.isActive() || R0SE.mc.player == null) {
            this.currentSwap.reset();
            return;
        }
        int fromSlot = this.currentSwap.getSlotFrom();
        int toSlot = this.currentSwap.getSlotTo();
        SilentSwapType type = this.currentSwap.getType();
        SwapMode mode = this.currentSwap.getMode();
        this.currentSwap.reset();
        this.debug("end from=" + fromSlot + " to=" + toSlot + " mode=" + String.valueOf((Object)mode) + " type=" + String.valueOf((Object)type));
        if (mode == SwapMode.NORMAL) {
            this.setSelectedSlot(fromSlot);
            return;
        }
        if (mode == SwapMode.SILENT_ALT) {
            if (PlayerInventory.isValidHotbarIndex((int)fromSlot)) {
                this.setSlotAlt(fromSlot);
            }
            return;
        }
        if (type == SilentSwapType.HOTBAR) {
            if (PlayerInventory.isValidHotbarIndex((int)fromSlot) && R0SE.mc.getNetworkHandler() != null && fromSlot != this.serverSlot) {
                R0SE.mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(fromSlot));
                this.serverSlot = fromSlot;
            }
            return;
        }
        if (fromSlot >= 0 && toSlot >= 0) {
            this.swap(toSlot, fromSlot);
        }
    }

    public ItemStack getHeldStack() {
        if (R0SE.mc.player == null) {
            return ItemStack.EMPTY;
        }
        if (this.currentSwap.isActive()) {
            return R0SE.mc.player.getInventory().getStack(this.currentSwap.getSlotTo());
        }
        return R0SE.mc.player.getMainHandStack();
    }

    public ItemStack getServerStack() {
        if (R0SE.mc.player == null) {
            return ItemStack.EMPTY;
        }
        int slot = this.getSafeServerSlot();
        return PlayerInventory.isValidHotbarIndex((int)slot) ? R0SE.mc.player.getInventory().getStack(slot) : R0SE.mc.player.getMainHandStack();
    }

    public ItemSlot getItem(Item item) {
        return InventoryUtil.getItem(item, this.getSilentSwapType());
    }

    public int getItemSlot(Item item) {
        return InventoryUtil.getItemSlot(item, this.getSilentSwapType());
    }

    public int count(Item item) {
        return InventoryUtil.count(item);
    }

    public void pickupSlot(ScreenHandler handler, int slot) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null || handler == null) {
            return;
        }
        R0SE.mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, (PlayerEntity)R0SE.mc.player);
    }

    public void swapSlot(ScreenHandler handler, int slot1, int slot2) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null || handler == null) {
            return;
        }
        R0SE.mc.interactionManager.clickSlot(handler.syncId, slot1, slot2, SlotActionType.SWAP, (PlayerEntity)R0SE.mc.player);
    }

    public void swap(int fromSlot, int toSlot) {
        if (R0SE.mc.player == null) {
            return;
        }
        PlayerScreenHandler handler = R0SE.mc.player.playerScreenHandler;
        int packetSlot = InventoryUtil.getPacketSlotIndex((ScreenHandler)handler, fromSlot);
        this.swapSlot((ScreenHandler)handler, packetSlot, toSlot);
    }

    private boolean isHoldingSlot(int slot) {
        if (R0SE.mc.player == null) {
            return false;
        }
        if (PlayerInventory.isValidHotbarIndex((int)slot)) {
            return this.getSafeServerSlot() == slot;
        }
        return this.currentSwap.isActive() && this.currentSwap.getSlotTo() == slot;
    }

    private int getSafeServerSlot() {
        if (PlayerInventory.isValidHotbarIndex((int)this.serverSlot)) {
            return this.serverSlot;
        }
        return this.getSelectedSlot();
    }

    private void debug(String message) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config == null || !((Boolean)config.getDebug().getValue()).booleanValue()) {
            return;
        }
        Managers.DEBUG.log("InventoryDebug", message);
    }
}


