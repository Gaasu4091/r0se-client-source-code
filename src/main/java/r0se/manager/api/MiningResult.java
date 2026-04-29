/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package r0se.manager.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import r0se.manager.api.MiningFailReason;

public class MiningResult {
    public static final MiningResult SUCCESS = new MiningResult(true, MiningFailReason.NONE, null, null, null);
    private final boolean success;
    private final MiningFailReason failReason;
    private final MiningAction action;
    private final BlockPos pos;
    private final Direction direction;

    private MiningResult(boolean success, MiningFailReason failReason, MiningAction action, BlockPos pos, Direction direction) {
        this.success = success;
        this.failReason = failReason;
        this.action = action;
        this.pos = pos == null ? null : pos.toImmutable();
        this.direction = direction;
    }

    public static MiningResult success(MiningAction action, BlockPos pos, Direction direction) {
        return new MiningResult(true, MiningFailReason.NONE, action, pos, direction);
    }

    public static MiningResult fail(MiningFailReason failReason, MiningAction action, BlockPos pos, Direction direction) {
        return new MiningResult(false, failReason, action, pos, direction);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public MiningFailReason getFailReason() {
        return this.failReason;
    }

    public MiningAction getAction() {
        return this.action;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public static enum MiningAction {
        START,
        CONTINUE,
        FINISH,
        FORCE_PROGRESS,
        RESET_COOLDOWN,
        PACKET_START,
        PACKET_TICK,
        PACKET_ABORT,
        PACKET_STOP,
        PACKET_SEQUENCE,
        RETRY,
        SWING;

    }
}


