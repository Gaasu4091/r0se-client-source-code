/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.inventory;

import r0se.api.inventory.SilentSwapType;
import r0se.api.inventory.SwapMode;

public final class SwapSession
implements AutoCloseable {
    public static final SwapSession NONE = new SwapSession(false, false, -1, SwapMode.NONE, SilentSwapType.HOTBAR, "none", null);
    private final boolean valid;
    private final boolean owner;
    private final int slot;
    private final SwapMode mode;
    private final SilentSwapType type;
    private final String failReason;
    private final Runnable closeAction;
    private boolean closed;

    private SwapSession(boolean valid, boolean owner, int slot, SwapMode mode, SilentSwapType type, String failReason, Runnable closeAction) {
        this.valid = valid;
        this.owner = owner;
        this.slot = slot;
        this.mode = mode;
        this.type = type;
        this.failReason = failReason;
        this.closeAction = closeAction;
    }

    public static SwapSession valid(int slot, SwapMode mode, SilentSwapType type, boolean owner, Runnable closeAction) {
        return new SwapSession(true, owner, slot, mode, type, "", closeAction);
    }

    public static SwapSession invalid(int slot, SwapMode mode, SilentSwapType type, String failReason) {
        return new SwapSession(false, false, slot, mode, type, failReason, null);
    }

    public boolean isValid() {
        return this.valid;
    }

    public boolean ownsSwap() {
        return this.owner;
    }

    public int getSlot() {
        return this.slot;
    }

    public SwapMode getMode() {
        return this.mode;
    }

    public SilentSwapType getType() {
        return this.type;
    }

    public String getFailReason() {
        return this.failReason;
    }

    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.valid && this.owner && this.closeAction != null) {
            this.closeAction.run();
        }
    }
}

