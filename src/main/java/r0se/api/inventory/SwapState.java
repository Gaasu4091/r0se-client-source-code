/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.inventory;

import r0se.api.inventory.SilentSwapType;
import r0se.api.inventory.SwapMode;

public final class SwapState {
    private boolean active;
    private int slotFrom = -1;
    private int slotTo = -1;
    private SilentSwapType type = SilentSwapType.HOTBAR;
    private SwapMode mode = SwapMode.SILENT;
    private long startedAt;

    public boolean isActive() {
        return this.active;
    }

    public void begin(int slotFrom, int slotTo, SilentSwapType type) {
        this.begin(slotFrom, slotTo, type, SwapMode.SILENT);
    }

    public void begin(int slotFrom, int slotTo, SilentSwapType type, SwapMode mode) {
        this.active = true;
        this.slotFrom = slotFrom;
        this.slotTo = slotTo;
        this.type = type;
        this.mode = mode;
        this.startedAt = System.currentTimeMillis();
    }

    public void reset() {
        this.active = false;
        this.slotFrom = -1;
        this.slotTo = -1;
        this.type = SilentSwapType.HOTBAR;
        this.mode = SwapMode.SILENT;
        this.startedAt = 0L;
    }

    public int getSlotFrom() {
        return this.slotFrom;
    }

    public int getSlotTo() {
        return this.slotTo;
    }

    public SilentSwapType getType() {
        return this.type;
    }

    public SwapMode getMode() {
        return this.mode;
    }

    public long getStartedAt() {
        return this.startedAt;
    }
}

