/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.block.BlockState
 */
package r0se.manager.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import r0se.R0SE;
import r0se.api.inventory.SwapSession;
import r0se.api.rotation.Rotation;

public class MiningContext {
    private final BlockPos pos;
    private final Direction direction;
    private final float targetDamage;
    private final long createdTick;
    private BlockState trackedState;
    private float progress;
    private float previousProgress;
    private boolean started;
    private boolean instantRemine;
    private long lastMineTick;
    private long lastAirTick;
    private long confirmedAirAt;
    private long lastSeenTick;
    private long attemptedBreakAt;
    private SwapSession swapSession = SwapSession.NONE;
    private Rotation pendingStopRotation;
    private long pendingStopRotationTick = -1L;
    private int brokenCount;

    public MiningContext(BlockPos pos, Direction direction, float targetDamage, long createdTick) {
        this.pos = pos.toImmutable();
        this.direction = direction;
        this.targetDamage = targetDamage;
        this.createdTick = createdTick;
        this.lastSeenTick = createdTick;
        this.trackedState = R0SE.mc.world == null ? null : R0SE.mc.world.getBlockState(pos);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public float getTargetDamage() {
        return this.targetDamage;
    }

    public long getCreatedTick() {
        return this.createdTick;
    }

    public BlockState getTrackedState() {
        return this.trackedState;
    }

    public void setTrackedState(BlockState trackedState) {
        this.trackedState = trackedState;
    }

    public float getProgress() {
        return this.progress;
    }

    public float getPreviousProgress() {
        return this.previousProgress;
    }

    public boolean isStarted() {
        return this.started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isInstantRemine() {
        return this.instantRemine;
    }

    public void setInstantRemine(boolean instantRemine) {
        this.instantRemine = instantRemine;
    }

    public long getLastMineTick() {
        return this.lastMineTick;
    }

    public void setLastMineTick(long lastMineTick) {
        this.lastMineTick = lastMineTick;
    }

    public long getLastAirTick() {
        return this.lastAirTick;
    }

    public void setLastAirTick(long lastAirTick) {
        this.lastAirTick = lastAirTick;
    }

    public long getConfirmedAirAt() {
        return this.confirmedAirAt;
    }

    public void setConfirmedAirAt(long confirmedAirAt) {
        this.confirmedAirAt = confirmedAirAt;
    }

    public long getLastSeenTick() {
        return this.lastSeenTick;
    }

    public void setLastSeenTick(long lastSeenTick) {
        this.lastSeenTick = lastSeenTick;
    }

    public long getAttemptedBreakAt() {
        return this.attemptedBreakAt;
    }

    public void setAttemptedBreakAt(long attemptedBreakAt) {
        this.attemptedBreakAt = attemptedBreakAt;
    }

    public SwapSession getSwapSession() {
        return this.swapSession;
    }

    public void setSwapSession(SwapSession swapSession) {
        this.swapSession = swapSession == null ? SwapSession.NONE : swapSession;
    }

    public int getBrokenCount() {
        return this.brokenCount;
    }

    public Rotation getPendingStopRotation() {
        return this.pendingStopRotation;
    }

    public long getPendingStopRotationTick() {
        return this.pendingStopRotationTick;
    }

    public void setPendingStopRotation(Rotation pendingStopRotation, long tick) {
        this.pendingStopRotation = pendingStopRotation;
        this.pendingStopRotationTick = tick;
    }

    public void clearPendingStopRotation() {
        this.pendingStopRotation = null;
        this.pendingStopRotationTick = -1L;
    }

    public void increment(float delta) {
        this.previousProgress = this.progress;
        this.progress += delta;
    }

    public void resetForRetry() {
        this.started = false;
        this.instantRemine = false;
        this.attemptedBreakAt = 0L;
        this.confirmedAirAt = 0L;
        this.clearPendingStopRotation();
        this.progress = 0.0f;
        this.previousProgress = 0.0f;
    }

    public void markBroken() {
        ++this.brokenCount;
        this.started = false;
        this.attemptedBreakAt = 0L;
        this.confirmedAirAt = 0L;
        this.clearPendingStopRotation();
        this.progress = 0.0f;
        this.previousProgress = 0.0f;
    }
}


