/*
 * Decompiled with CFR 0.152.
 */
package r0se.manager.api;

import java.util.function.Predicate;
import r0se.manager.api.MiningContext;

public class MiningQueue<T extends MiningContext> {
    private T primary;
    private T secondary;
    private T pending;
    private int secondaryDelay;

    public T getPrimary() {
        return this.primary;
    }

    public T getSecondary() {
        return this.secondary;
    }

    public T getPending() {
        return this.pending;
    }

    public int getSecondaryDelay() {
        return this.secondaryDelay;
    }

    public boolean clear() {
        boolean changed = this.primary != null || this.secondary != null || this.pending != null || this.secondaryDelay != 0;
        this.primary = null;
        this.secondary = null;
        this.pending = null;
        this.secondaryDelay = 0;
        return changed;
    }

    public boolean clearSecondary() {
        boolean changed = this.secondary != null || this.pending != null || this.secondaryDelay != 0;
        this.secondary = null;
        this.pending = null;
        this.secondaryDelay = 0;
        return changed;
    }

    public boolean tickDelay() {
        if (this.secondaryDelay > 0) {
            --this.secondaryDelay;
            return true;
        }
        return false;
    }

    public boolean has(BlockPosMatcher matcher) {
        return this.matches(this.primary, matcher) || this.matches(this.secondary, matcher) || this.matches(this.pending, matcher);
    }

    public MiningQueueSlot queue(T context, boolean allowSecondary) {
        if (!allowSecondary) {
            this.primary = context;
            this.secondary = null;
            this.pending = null;
            this.secondaryDelay = 0;
            return MiningQueueSlot.PRIMARY;
        }
        if (this.primary == null) {
            this.primary = context;
            return MiningQueueSlot.PRIMARY;
        }
        this.secondary = this.primary;
        this.primary = context;
        this.pending = null;
        this.secondaryDelay = 1;
        return MiningQueueSlot.PRIMARY;
    }

    public MiningQueueSlot processPending(boolean allowSecondary, Predicate<T> primaryBusy) {
        if (this.pending == null) {
            return MiningQueueSlot.NONE;
        }
        if (this.primary == null) {
            this.primary = this.pending;
            this.pending = null;
            return MiningQueueSlot.PRIMARY;
        }
        if (!allowSecondary) {
            if (primaryBusy != null && primaryBusy.test(this.primary)) {
                return MiningQueueSlot.NONE;
            }
            this.primary = this.pending;
            this.pending = null;
            return MiningQueueSlot.PRIMARY;
        }
        if (this.secondary == null) {
            this.secondary = this.pending;
            this.pending = null;
            this.secondaryDelay = 1;
            return MiningQueueSlot.SECONDARY;
        }
        return MiningQueueSlot.NONE;
    }

    public boolean promoteSecondaryIfNeeded() {
        if (this.primary == null && this.secondary != null) {
            this.primary = this.secondary;
            this.secondary = null;
            return true;
        }
        return false;
    }

    public MiningQueueSlot remove(T context) {
        if (context == this.primary) {
            this.primary = null;
            return MiningQueueSlot.PRIMARY;
        }
        if (context == this.secondary) {
            this.secondary = null;
            return MiningQueueSlot.SECONDARY;
        }
        if (context == this.pending) {
            this.pending = null;
            return MiningQueueSlot.PENDING;
        }
        return MiningQueueSlot.NONE;
    }

    public MiningQueueSlot onBroken(T context, Runnable primaryPromotedCallback) {
        if (context == this.primary) {
            this.primary = null;
            this.promoteSecondaryIfNeeded();
            if (primaryPromotedCallback != null) {
                primaryPromotedCallback.run();
            }
            return MiningQueueSlot.PRIMARY;
        }
        if (context == this.secondary) {
            this.secondary = null;
            return MiningQueueSlot.SECONDARY;
        }
        if (context == this.pending) {
            this.pending = null;
            return MiningQueueSlot.PENDING;
        }
        return MiningQueueSlot.NONE;
    }

    public boolean isPrimary(T context) {
        return context == this.primary;
    }

    public boolean isSecondary(T context) {
        return context == this.secondary;
    }

    private boolean matches(T context, BlockPosMatcher matcher) {
        return context != null && matcher != null && matcher.matches((MiningContext)context);
    }

    public String snapshot() {
        return "primary=" + this.describe(this.primary) + " secondary=" + this.describe(this.secondary) + " pending=" + this.describe(this.pending) + " secondaryDelay=" + this.secondaryDelay;
    }

    private String describe(T context) {
        if (context == null) {
            return "none";
        }
        return ((MiningContext)context).getPos().toShortString() + "/" + String.valueOf(((MiningContext)context).getDirection()) + "/started=" + ((MiningContext)context).isStarted() + "/progress=" + String.format("%.2f", Float.valueOf(((MiningContext)context).getProgress()));
    }

    @FunctionalInterface
    public static interface BlockPosMatcher {
        public boolean matches(MiningContext var1);
    }

    public static enum MiningQueueSlot {
        NONE,
        PRIMARY,
        SECONDARY,
        PENDING;

    }
}


