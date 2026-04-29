/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Hand
 *  net.minecraft.entity.Entity
 *  net.minecraft.world.BlockView
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.block.ShapeContext
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 */
package r0se.manager.impl;

import java.util.function.Predicate;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import r0se.R0SE;
import r0se.api.inventory.SwapState;
import r0se.api.rotation.Rotation;
import r0se.api.rotation.RotationActionResult;
import r0se.impl.module.client.AntiCheat;
import r0se.manager.Manager;
import r0se.manager.Managers;
import r0se.manager.api.MiningContext;
import r0se.manager.api.MiningFailReason;
import r0se.manager.api.MiningProfile;
import r0se.manager.api.MiningQueue;
import r0se.manager.api.MiningResult;
import r0se.mixin.ClientPlayerInteractionManagerAccessor;

public class MiningManager
implements Manager {
    private static final double DEFAULT_BREAK_RANGE = 4.5;
    private final MiningQueue<MiningContext> queue = new MiningQueue();
    private MiningState state = MiningState.IDLE;
    private BlockPos currentPos;
    private Direction currentDirection;
    private BlockPos lastCancelledPos;
    private BlockPos lastFinishedPos;
    private boolean lastBlockInstant;
    private long lastPacketFinishAt;

    public boolean isMining() {
        return this.state == MiningState.STARTING || this.state == MiningState.CONTINUING;
    }

    public MiningState getState() {
        return this.state;
    }

    public MiningContext getQueuedPrimary() {
        return this.queue.getPrimary();
    }

    public MiningContext getQueuedSecondary() {
        return this.queue.getSecondary();
    }

    public MiningContext getQueuedPending() {
        return this.queue.getPending();
    }

    public int getQueuedSecondaryDelay() {
        return this.queue.getSecondaryDelay();
    }

    public boolean clearMiningQueue(String source) {
        boolean changed = this.queue.clear();
        this.logQueueState("clear", source, changed, MiningQueue.MiningQueueSlot.NONE);
        return changed;
    }

    public boolean clearSecondaryMiningQueue(String source) {
        boolean changed = this.queue.clearSecondary();
        this.logQueueState("clear_secondary", source, changed, MiningQueue.MiningQueueSlot.NONE);
        return changed;
    }

    public boolean tickMiningQueueDelay(String source) {
        boolean changed = this.queue.tickDelay();
        this.logQueueState("tick_delay", source, changed, MiningQueue.MiningQueueSlot.NONE);
        return changed;
    }

    public boolean hasQueuedContext(MiningQueue.BlockPosMatcher matcher) {
        return this.queue.has(matcher);
    }

    public MiningQueue.MiningQueueSlot queueMiningContext(MiningContext context, boolean allowSecondary, String source) {
        if (context == null) {
            this.logQueueState("queue_null", source, false, MiningQueue.MiningQueueSlot.NONE);
            return MiningQueue.MiningQueueSlot.NONE;
        }
        MiningQueue.MiningQueueSlot slot = this.queue.queue(context, allowSecondary);
        this.logQueueState("queue", source, true, slot);
        return slot;
    }

    public MiningQueue.MiningQueueSlot processQueuedPending(boolean allowSecondary, Predicate<MiningContext> primaryBusy, String source) {
        MiningQueue.MiningQueueSlot slot = this.queue.processPending(allowSecondary, primaryBusy);
        this.logQueueState("process_pending", source, slot != MiningQueue.MiningQueueSlot.NONE, slot);
        return slot;
    }

    public boolean promoteQueuedSecondaryIfNeeded(String source) {
        boolean changed = this.queue.promoteSecondaryIfNeeded();
        this.logQueueState("promote_secondary", source, changed, MiningQueue.MiningQueueSlot.NONE);
        return changed;
    }

    public MiningQueue.MiningQueueSlot removeQueuedContext(MiningContext context, String source) {
        MiningQueue.MiningQueueSlot slot = this.queue.remove(context);
        this.logQueueState("remove", source, slot != MiningQueue.MiningQueueSlot.NONE, slot);
        return slot;
    }

    public MiningQueue.MiningQueueSlot markQueuedContextBroken(MiningContext context, Runnable primaryPromotedCallback, String source) {
        MiningQueue.MiningQueueSlot slot = this.queue.onBroken(context, primaryPromotedCallback);
        this.logQueueState("broken", source, slot != MiningQueue.MiningQueueSlot.NONE, slot);
        return slot;
    }

    public void confirmServerAir(BlockPos pos, String source) {
        if (pos == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (MiningContext context : new MiningContext[]{this.queue.getPrimary(), this.queue.getSecondary(), this.queue.getPending()}) {
            if (context == null || !pos.equals((Object)context.getPos())) continue;
            context.setConfirmedAirAt(now);
            this.debug("server_air_confirm source=" + this.safeSource(source) + " pos=" + this.shortPos(pos) + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)) + " attempted=" + context.getAttemptedBreakAt());
        }
    }

    public boolean isQueuedPrimary(MiningContext context) {
        return this.queue.isPrimary(context);
    }

    public boolean isQueuedSecondary(MiningContext context) {
        return this.queue.isSecondary(context);
    }

    public String miningQueueSnapshot() {
        return this.queue.snapshot();
    }

    public BlockPos getCurrentPos() {
        return this.currentPos;
    }

    public Direction getCurrentDirection() {
        return this.currentDirection;
    }

    public BlockPos getLastCancelledPos() {
        return this.lastCancelledPos;
    }

    public BlockPos getLastFinishedPos() {
        return this.lastFinishedPos;
    }

    public boolean startMining(BlockPos pos, Direction direction) {
        MiningFailReason failReason = this.getMineFailReason(pos, direction);
        if (failReason != MiningFailReason.NONE) {
            this.debug("reject start pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " reason=" + String.valueOf((Object)failReason) + " mode=" + String.valueOf((Object)this.getMiningMode()) + " state=" + String.valueOf((Object)this.state));
            return false;
        }
        if (this.getMiningMode() == AntiCheat.MiningMode.GRIM_STRICT && this.isMining() && this.currentPos != null && !this.currentPos.equals((Object)pos)) {
            this.debug("strict start mismatch current=" + this.shortPos(this.currentPos) + " new=" + this.shortPos(pos));
            return false;
        }
        this.currentPos = pos.toImmutable();
        this.currentDirection = direction;
        this.state = MiningState.STARTING;
        this.lastCancelledPos = null;
        this.lastFinishedPos = null;
        this.lastBlockInstant = this.isInstantBreak(pos);
        this.debug("start pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " mode=" + String.valueOf((Object)this.getMiningMode()) + " instant=" + this.lastBlockInstant);
        return true;
    }

    public boolean continueMining(BlockPos pos, Direction direction) {
        MiningFailReason failReason = this.getMineFailReason(pos, direction);
        if (failReason != MiningFailReason.NONE) {
            this.debug("reject continue pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " reason=" + String.valueOf((Object)failReason) + " mode=" + String.valueOf((Object)this.getMiningMode()) + " state=" + String.valueOf((Object)this.state));
            return false;
        }
        if (this.getMiningMode() == AntiCheat.MiningMode.GRIM_STRICT) {
            if (!this.isMining() || this.currentPos == null) {
                this.debug("strict continue without active start pos=" + this.shortPos(pos));
                return false;
            }
            if (!this.currentPos.equals((Object)pos)) {
                this.debug("strict continue mismatch pos=" + this.shortPos(this.currentPos) + " vs " + this.shortPos(pos));
                return false;
            }
            if (this.currentDirection != null && this.currentDirection != direction) {
                this.debug("strict continue mismatch dir=" + String.valueOf(this.currentDirection) + " vs " + String.valueOf(direction));
                return false;
            }
        }
        this.currentPos = pos.toImmutable();
        this.currentDirection = direction;
        this.state = MiningState.CONTINUING;
        return true;
    }

    public boolean finishMining(BlockPos pos) {
        if (pos == null || R0SE.mc.player == null || R0SE.mc.world == null) {
            return false;
        }
        AntiCheat.MiningMode mode = this.getMiningMode();
        if (mode == AntiCheat.MiningMode.GRIM_STRICT) {
            boolean allowsInstant;
            boolean matchesCurrent = this.currentPos != null && this.currentPos.equals((Object)pos);
            boolean matchesCancelled = this.lastCancelledPos != null && this.lastCancelledPos.equals((Object)pos);
            boolean bl = allowsInstant = this.lastBlockInstant && this.state == MiningState.STARTING;
            if (!(matchesCurrent || matchesCancelled || allowsInstant)) {
                this.debug("strict finish mismatch current=" + this.shortPos(this.currentPos) + " cancelled=" + this.shortPos(this.lastCancelledPos) + " pos=" + this.shortPos(pos));
                return false;
            }
        }
        this.lastFinishedPos = pos.toImmutable();
        this.currentPos = null;
        this.currentDirection = null;
        this.lastCancelledPos = null;
        this.lastBlockInstant = false;
        this.state = MiningState.FINISHED;
        this.debug("finish pos=" + this.shortPos(pos) + " mode=" + String.valueOf((Object)mode));
        return true;
    }

    public void cancelMining() {
        if (this.isMining()) {
            this.debug("cancel pos=" + this.shortPos(this.currentPos) + " dir=" + String.valueOf(this.currentDirection) + " state=" + String.valueOf((Object)this.state));
        }
        this.lastCancelledPos = this.currentPos == null ? null : this.currentPos.toImmutable();
        this.currentPos = null;
        this.currentDirection = null;
        this.lastBlockInstant = false;
        this.state = MiningState.CANCELLED;
    }

    public boolean isValidMine(BlockPos pos, Direction direction) {
        return this.getMineFailReason(pos, direction) == MiningFailReason.NONE;
    }

    public MiningResult startBlockBreak(BlockPos pos, Direction direction, String source) {
        MiningFailReason failReason = this.getMineFailReason(pos, direction);
        if (failReason != MiningFailReason.NONE) {
            this.debugAction("start", source, pos, direction, false, failReason);
            return MiningResult.fail(failReason, MiningResult.MiningAction.START, pos, direction);
        }
        if (R0SE.mc.interactionManager == null) {
            this.debugAction("start", source, pos, direction, false, MiningFailReason.NO_INTERACTION_MANAGER);
            return MiningResult.fail(MiningFailReason.NO_INTERACTION_MANAGER, MiningResult.MiningAction.START, pos, direction);
        }
        boolean accepted = R0SE.mc.interactionManager.attackBlock(pos, direction);
        MiningFailReason resultReason = accepted ? MiningFailReason.NONE : MiningFailReason.VANILLA_REJECTED;
        this.debugAction("start", source, pos, direction, accepted, resultReason);
        return accepted ? MiningResult.success(MiningResult.MiningAction.START, pos, direction) : MiningResult.fail(resultReason, MiningResult.MiningAction.START, pos, direction);
    }

    public MiningResult continueBlockBreak(BlockPos pos, Direction direction, String source) {
        MiningFailReason failReason = this.getMineFailReason(pos, direction);
        if (failReason != MiningFailReason.NONE) {
            this.debugAction("continue", source, pos, direction, false, failReason);
            return MiningResult.fail(failReason, MiningResult.MiningAction.CONTINUE, pos, direction);
        }
        if (R0SE.mc.interactionManager == null) {
            this.debugAction("continue", source, pos, direction, false, MiningFailReason.NO_INTERACTION_MANAGER);
            return MiningResult.fail(MiningFailReason.NO_INTERACTION_MANAGER, MiningResult.MiningAction.CONTINUE, pos, direction);
        }
        boolean accepted = R0SE.mc.interactionManager.updateBlockBreakingProgress(pos, direction);
        MiningFailReason resultReason = accepted ? MiningFailReason.NONE : MiningFailReason.VANILLA_REJECTED;
        this.debugAction("continue", source, pos, direction, accepted, resultReason);
        return accepted ? MiningResult.success(MiningResult.MiningAction.CONTINUE, pos, direction) : MiningResult.fail(resultReason, MiningResult.MiningAction.CONTINUE, pos, direction);
    }

    public MiningResult finishBlockBreak(BlockPos pos, Direction direction, String source) {
        if (pos == null) {
            this.debugAction("finish", source, pos, direction, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.FINISH, pos, direction);
        }
        if (R0SE.mc.player == null) {
            this.debugAction("finish", source, pos, direction, false, MiningFailReason.NO_PLAYER);
            return MiningResult.fail(MiningFailReason.NO_PLAYER, MiningResult.MiningAction.FINISH, pos, direction);
        }
        if (R0SE.mc.world == null) {
            this.debugAction("finish", source, pos, direction, false, MiningFailReason.NO_WORLD);
            return MiningResult.fail(MiningFailReason.NO_WORLD, MiningResult.MiningAction.FINISH, pos, direction);
        }
        if (R0SE.mc.interactionManager == null) {
            this.debugAction("finish", source, pos, direction, false, MiningFailReason.NO_INTERACTION_MANAGER);
            return MiningResult.fail(MiningFailReason.NO_INTERACTION_MANAGER, MiningResult.MiningAction.FINISH, pos, direction);
        }
        boolean accepted = R0SE.mc.interactionManager.breakBlock(pos);
        MiningFailReason resultReason = accepted ? MiningFailReason.NONE : MiningFailReason.VANILLA_REJECTED;
        this.debugAction("finish", source, pos, direction, accepted, resultReason);
        return accepted ? MiningResult.success(MiningResult.MiningAction.FINISH, pos, direction) : MiningResult.fail(resultReason, MiningResult.MiningAction.FINISH, pos, direction);
    }

    public MiningResult forceProgress(BlockPos pos, float progress, String source) {
        ClientPlayerInteractionManager ReloadReason = R0SE.mc.interactionManager;
        if (!(ReloadReason instanceof ClientPlayerInteractionManagerAccessor)) {
            this.debugAction("force_progress", source, pos, null, false, MiningFailReason.ACCESSOR_MISSING);
            return MiningResult.fail(MiningFailReason.ACCESSOR_MISSING, MiningResult.MiningAction.FORCE_PROGRESS, pos, null);
        }
        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor)ReloadReason;
        if (pos == null || !this.isCurrentlyBreaking(pos)) {
            this.debugAction("force_progress", source, pos, null, false, MiningFailReason.STATE_REJECTED);
            return MiningResult.fail(MiningFailReason.STATE_REJECTED, MiningResult.MiningAction.FORCE_PROGRESS, pos, null);
        }
        float before = accessor.r0se$getCurrentBreakingProgress();
        float next = Math.max(before, Math.min(progress, 1.0f));
        accessor.r0se$setCurrentBreakingProgress(next);
        this.debug("action=force_progress source=" + this.safeSource(source) + " pos=" + this.shortPos(pos) + " before=" + String.format("%.2f", Float.valueOf(before)) + " after=" + String.format("%.2f", Float.valueOf(next)));
        return MiningResult.success(MiningResult.MiningAction.FORCE_PROGRESS, pos, null);
    }

    public MiningResult resetCooldown(String source) {
        ClientPlayerInteractionManager ReloadReason = R0SE.mc.interactionManager;
        if (!(ReloadReason instanceof ClientPlayerInteractionManagerAccessor)) {
            this.debugAction("reset_cooldown", source, null, null, false, MiningFailReason.ACCESSOR_MISSING);
            return MiningResult.fail(MiningFailReason.ACCESSOR_MISSING, MiningResult.MiningAction.RESET_COOLDOWN, null, null);
        }
        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor)ReloadReason;
        int before = accessor.r0se$getBlockBreakingCooldown();
        accessor.r0se$setBlockBreakingCooldown(0);
        this.debug("action=reset_cooldown source=" + this.safeSource(source) + " before=" + before + " after=0");
        return MiningResult.success(MiningResult.MiningAction.RESET_COOLDOWN, null, null);
    }

    public MiningResult sendPacketMineStart(BlockPos pos, Direction direction, String source) {
        MiningFailReason failReason = this.getMineFailReason(pos, direction);
        if (failReason != MiningFailReason.NONE) {
            this.debugAction("packet_start_sequence", source, pos, direction, false, failReason);
            return MiningResult.fail(failReason, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
        }
        MiningProfile profile = this.getMiningProfile();
        long remainingDelay = this.getPacketStartDelayRemaining(profile);
        if (remainingDelay > 0L) {
            this.debug("packet_start_sequence source=" + this.safeSource(source) + " success=false reason=" + String.valueOf((Object)MiningFailReason.DELAY_WAITING) + " pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " profile=" + profile.name() + " remainingDelayMs=" + remainingDelay + " requiredDelayMs=" + profile.getFinishToStartDelayMs() + " inventory=" + this.describeInventoryState());
            return MiningResult.fail(MiningFailReason.DELAY_WAITING, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
        }
        boolean success = this.sendDigSequence(profile.getStartSequence(), pos, direction, source);
        this.debug("packet_start_sequence source=" + this.safeSource(source) + " success=" + success + " pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " profile=" + profile.name() + " sequence=" + profile.describeStartSequence() + " inventory=" + this.describeInventoryState());
        return success ? MiningResult.success(MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction) : MiningResult.fail(MiningFailReason.VANILLA_REJECTED, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
    }

    public MiningResult sendPacketMineStop(BlockPos pos, Direction direction, boolean swing, String source) {
        MiningFailReason failReason = this.getMineFailReason(pos, direction);
        if (failReason != MiningFailReason.NONE && failReason != MiningFailReason.STRICT_DIRECTION_FAILED) {
            this.debugAction("packet_stop_sequence", source, pos, direction, false, failReason);
            return MiningResult.fail(failReason, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
        }
        MiningProfile profile = this.getMiningProfile();
        boolean success = this.sendDigSequence(profile.getStopSequence(), pos, direction, source);
        if (success && profile.shouldPacketSwing(swing)) {
            success = this.sendSwingPacket(source).isSuccess();
        }
        if (success) {
            this.lastPacketFinishAt = System.currentTimeMillis();
        }
        this.debug("packet_stop_sequence source=" + this.safeSource(source) + " success=" + success + " pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " profile=" + profile.name() + " sequence=" + profile.describeStopSequence() + " swing=" + profile.shouldPacketSwing(swing) + " nextStartDelayMs=" + profile.getFinishToStartDelayMs() + " inventory=" + this.describeInventoryState());
        return success ? MiningResult.success(MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction) : MiningResult.fail(MiningFailReason.VANILLA_REJECTED, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
    }

    public MiningResult tryStartPacketMine(MiningContext context, String source) {
        if (context == null) {
            this.debugAction("packet_start_pipeline", source, null, null, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.PACKET_START, null, null);
        }
        if (context.isStarted()) {
            this.debug("packet_start_pipeline source=" + this.safeSource(source) + " skipped=already_started pos=" + this.shortPos(context.getPos()) + " dir=" + String.valueOf(context.getDirection()) + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)));
            return MiningResult.success(MiningResult.MiningAction.PACKET_START, context.getPos(), context.getDirection());
        }
        MiningResult result = this.sendPacketMineStart(context.getPos(), context.getDirection(), source);
        context.setStarted(result.isSuccess());
        this.debug("packet_start_pipeline source=" + this.safeSource(source) + " success=" + result.isSuccess() + " reason=" + String.valueOf((Object)result.getFailReason()) + " pos=" + this.shortPos(context.getPos()) + " dir=" + String.valueOf(context.getDirection()) + " profile=" + this.getMiningProfile().name() + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)) + " inventory=" + this.describeInventoryState());
        return result.isSuccess() ? MiningResult.success(MiningResult.MiningAction.PACKET_START, context.getPos(), context.getDirection()) : MiningResult.fail(result.getFailReason(), MiningResult.MiningAction.PACKET_START, context.getPos(), context.getDirection());
    }

    public MiningResult tickPacketMine(MiningContext context, long tick, String source) {
        if (context == null) {
            this.debugAction("packet_tick_pipeline", source, null, null, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.PACKET_TICK, null, null);
        }
        if (!context.isStarted()) {
            this.debugAction("packet_tick_pipeline", source, context.getPos(), context.getDirection(), false, MiningFailReason.STATE_REJECTED);
            return MiningResult.fail(MiningFailReason.STATE_REJECTED, MiningResult.MiningAction.PACKET_TICK, context.getPos(), context.getDirection());
        }
        context.setLastMineTick(tick);
        this.debug("packet_tick_pipeline source=" + this.safeSource(source) + " pos=" + this.shortPos(context.getPos()) + " dir=" + String.valueOf(context.getDirection()) + " tick=" + tick + " progress=" + String.format("%.2f", Float.valueOf(context.getProgress())) + " profile=" + this.getMiningProfile().name() + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)));
        return MiningResult.success(MiningResult.MiningAction.PACKET_TICK, context.getPos(), context.getDirection());
    }

    public MiningResult tryStopPacketMine(MiningContext context, boolean rotate, boolean swing, String source) {
        MiningResult rotationResult;
        Direction direction;
        if (context == null) {
            this.debugAction("packet_stop_pipeline", source, null, null, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.PACKET_SEQUENCE, null, null);
        }
        BlockPos pos = context.getPos();
        MiningFailReason failReason = this.getMineFailReason(pos, direction = context.getDirection());
        if (failReason != MiningFailReason.NONE && failReason != MiningFailReason.STRICT_DIRECTION_FAILED) {
            this.debugAction("packet_stop_pipeline", source, pos, direction, false, failReason);
            return MiningResult.fail(failReason, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
        }
        if (rotate && !(rotationResult = this.prepareStopRotation(context, source)).isSuccess()) {
            return rotationResult;
        }
        context.clearPendingStopRotation();
        MiningResult result = this.sendPacketMineStop(pos, direction, swing, source);
        this.debug("packet_stop_pipeline source=" + this.safeSource(source) + " success=" + result.isSuccess() + " reason=" + String.valueOf((Object)result.getFailReason()) + " pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " profile=" + this.getMiningProfile().name() + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)) + " swing=" + swing + " rotate=" + rotate + " inventory=" + this.describeInventoryState());
        return result;
    }

    public MiningResult preparePacketStopRotation(MiningContext context, String source) {
        if (context == null) {
            this.debugAction("packet_stop_rotation", source, null, null, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.PACKET_SEQUENCE, null, null);
        }
        return this.prepareStopRotation(context, source);
    }

    public MiningResult sendPacketMineStopOnly(BlockPos pos, Direction direction, String source) {
        return this.sendDigPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, source);
    }

    public MiningResult sendPacketMineAbortOnly(BlockPos pos, Direction direction, String source) {
        return this.sendDigPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction, source);
    }

    public MiningResult markBreakAttempt(MiningContext context, long timeMs, String source) {
        if (context == null) {
            this.debugAction("mark_break_attempt", source, null, null, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.RETRY, null, null);
        }
        context.setAttemptedBreakAt(timeMs);
        this.debug("mark_break_attempt source=" + this.safeSource(source) + " pos=" + this.shortPos(context.getPos()) + " dir=" + String.valueOf(context.getDirection()) + " time=" + timeMs + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)));
        return MiningResult.success(MiningResult.MiningAction.RETRY, context.getPos(), context.getDirection());
    }

    public MiningResult resetForRetryIfReady(MiningContext context, long retryDelayMs, String source) {
        if (context == null) {
            this.debugAction("retry_pipeline", source, null, null, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, MiningResult.MiningAction.RETRY, null, null);
        }
        long attemptedAt = context.getAttemptedBreakAt();
        if (attemptedAt <= 0L) {
            return MiningResult.fail(MiningFailReason.STATE_REJECTED, MiningResult.MiningAction.RETRY, context.getPos(), context.getDirection());
        }
        long elapsed = System.currentTimeMillis() - attemptedAt;
        if (elapsed < retryDelayMs) {
            this.debug("retry_pipeline source=" + this.safeSource(source) + " ready=false pos=" + this.shortPos(context.getPos()) + " elapsed=" + elapsed + " required=" + retryDelayMs + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)));
            return MiningResult.fail(MiningFailReason.STATE_REJECTED, MiningResult.MiningAction.RETRY, context.getPos(), context.getDirection());
        }
        context.resetForRetry();
        this.debug("retry_pipeline source=" + this.safeSource(source) + " ready=true pos=" + this.shortPos(context.getPos()) + " elapsed=" + elapsed + " required=" + retryDelayMs + " slot=" + String.valueOf((Object)this.describeQueueSlot(context)));
        return MiningResult.success(MiningResult.MiningAction.RETRY, context.getPos(), context.getDirection());
    }

    private boolean sendDigSequence(PlayerActionC2SPacket.Action[] sequence, BlockPos pos, Direction direction, String source) {
        if (sequence == null || sequence.length == 0) {
            this.debugAction("dig_sequence_empty", source, pos, direction, false, MiningFailReason.INVALID_ACTION);
            return false;
        }
        for (PlayerActionC2SPacket.Action action : sequence) {
            if (this.sendDigPacket(action, pos, direction, source).isSuccess()) continue;
            return false;
        }
        return true;
    }

    public MiningResult sendDigPacket(PlayerActionC2SPacket.Action action, BlockPos pos, Direction direction, String source) {
        if (action == null) {
            this.debugAction("dig_packet_null", source, pos, direction, false, MiningFailReason.INVALID_ACTION);
            return MiningResult.fail(MiningFailReason.INVALID_ACTION, MiningResult.MiningAction.PACKET_SEQUENCE, pos, direction);
        }
        if (pos == null) {
            this.debugAction("dig_packet_" + this.actionName(action), source, pos, direction, false, MiningFailReason.INVALID_TARGET);
            return MiningResult.fail(MiningFailReason.INVALID_TARGET, this.toMiningAction(action), pos, direction);
        }
        if (direction == null) {
            this.debugAction("dig_packet_" + this.actionName(action), source, pos, direction, false, MiningFailReason.INVALID_FACE);
            return MiningResult.fail(MiningFailReason.INVALID_FACE, this.toMiningAction(action), pos, direction);
        }
        if (R0SE.mc.getNetworkHandler() == null) {
            this.debugAction("dig_packet_" + this.actionName(action), source, pos, direction, false, MiningFailReason.NO_NETWORK_HANDLER);
            return MiningResult.fail(MiningFailReason.NO_NETWORK_HANDLER, this.toMiningAction(action), pos, direction);
        }
        R0SE.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(action, pos, direction));
        this.debug("dig_packet action=" + String.valueOf(action) + " source=" + this.safeSource(source) + " pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " mode=" + String.valueOf((Object)this.getMiningMode()));
        return MiningResult.success(this.toMiningAction(action), pos, direction);
    }

    public MiningResult sendSwingPacket(String source) {
        if (R0SE.mc.getNetworkHandler() == null) {
            this.debugAction("swing_packet", source, null, null, false, MiningFailReason.NO_NETWORK_HANDLER);
            return MiningResult.fail(MiningFailReason.NO_NETWORK_HANDLER, MiningResult.MiningAction.SWING, null, null);
        }
        R0SE.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
        this.debug("swing_packet source=" + this.safeSource(source) + " hand=" + String.valueOf(Hand.MAIN_HAND));
        return MiningResult.success(MiningResult.MiningAction.SWING, null, null);
    }

    private MiningResult prepareStopRotation(MiningContext context, String source) {
        boolean waitForNextTick;
        Rotation rotation = this.getRotation(context.getPos(), context.getDirection());
        RotationActionResult result = Managers.ROTATION.prepareActionRotation(rotation, 80, (waitForNextTick = this.needsStrictRotationWait()) ? 2 : 1, this.safeSource(source), waitForNextTick, context.getPendingStopRotation(), context.getPendingStopRotationTick());
        if (result.isRejected()) {
            this.debug("packet_stop_rotation_rejected source=" + this.safeSource(source) + " pos=" + this.shortPos(context.getPos()) + " profile=" + this.getMiningProfile().name() + " mode=" + String.valueOf((Object)Managers.ROTATION.getRotationMode()));
            return MiningResult.fail(MiningFailReason.ROTATION_REJECTED, MiningResult.MiningAction.PACKET_SEQUENCE, context.getPos(), context.getDirection());
        }
        if (result.isWaiting()) {
            context.setPendingStopRotation(result.getRotation(), result.getTick());
            this.debug("packet_stop_rotation_wait source=" + this.safeSource(source) + " pos=" + this.shortPos(context.getPos()) + " tick=" + result.getTick() + " profile=" + this.getMiningProfile().name() + " mode=" + String.valueOf((Object)Managers.ROTATION.getRotationMode()));
            return MiningResult.fail(MiningFailReason.ROTATION_WAITING, MiningResult.MiningAction.PACKET_SEQUENCE, context.getPos(), context.getDirection());
        }
        context.clearPendingStopRotation();
        this.debug("packet_stop_rotation_ready source=" + this.safeSource(source) + " pos=" + this.shortPos(context.getPos()) + " profile=" + this.getMiningProfile().name() + " mode=" + String.valueOf((Object)Managers.ROTATION.getRotationMode()));
        return MiningResult.success(MiningResult.MiningAction.PACKET_SEQUENCE, context.getPos(), context.getDirection());
    }

    private boolean needsStrictRotationWait() {
        return this.getMiningProfile().getStrictStopWaitTicks() > 0 && Managers.ROTATION.getRotationMode() == AntiCheat.RotationMode.SILENT;
    }

    private Rotation getRotation(BlockPos pos, Direction direction) {
        if (pos == null || direction == null || R0SE.mc.player == null) {
            return null;
        }
        Vec3d hit = pos.toCenterPos().add(Vec3d.of((Vec3i)direction.getVector()).multiply(0.5));
        Vec3d eyes = R0SE.mc.player.getEyePos();
        Vec3d delta = hit.subtract(eyes);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, horizontal)));
        return new Rotation(yaw, pitch);
    }

    public boolean isCurrentlyBreaking(BlockPos pos) {
        ClientPlayerInteractionManager ReloadReason = R0SE.mc.interactionManager;
        if (!(ReloadReason instanceof ClientPlayerInteractionManagerAccessor)) {
            return false;
        }
        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor)ReloadReason;
        BlockPos current = accessor.r0se$getCurrentBreakingPos();
        return current != null && current.equals((Object)pos);
    }

    public MiningFailReason getMineFailReason(BlockPos pos, Direction direction) {
        if (pos == null || direction == null || R0SE.mc.player == null || R0SE.mc.world == null) {
            if (R0SE.mc.player == null) {
                return MiningFailReason.NO_PLAYER;
            }
            if (R0SE.mc.world == null) {
                return MiningFailReason.NO_WORLD;
            }
            if (pos == null) {
                return MiningFailReason.INVALID_TARGET;
            }
            return MiningFailReason.INVALID_FACE;
        }
        AntiCheat.MiningMode mode = this.getMiningMode();
        if (mode == AntiCheat.MiningMode.VANILLA) {
            return MiningFailReason.NONE;
        }
        Box bounds = this.getBlockBounds(pos);
        if (!this.isWithinRange(bounds)) {
            return MiningFailReason.RANGE_FAILED;
        }
        if (mode == AntiCheat.MiningMode.GRIM_STRICT) {
            return this.isFaceVisible(bounds, direction, 0.0) ? MiningFailReason.NONE : MiningFailReason.STRICT_DIRECTION_FAILED;
        }
        return this.isFaceVisible(bounds, direction, 0.25) ? MiningFailReason.NONE : MiningFailReason.STRICT_DIRECTION_FAILED;
    }

    private boolean isWithinRange(Box bounds) {
        double closestZ;
        double closestY;
        double closestX;
        Vec3d eyePos = R0SE.mc.player.getEyePos();
        return eyePos.squaredDistanceTo(closestX = this.clamp(eyePos.x, bounds.minX, bounds.maxX), closestY = this.clamp(eyePos.y, bounds.minY, bounds.maxY), closestZ = this.clamp(eyePos.z, bounds.minZ, bounds.maxZ)) <= 20.25;
    }

    private boolean isFaceVisible(Box bounds, Direction face, double epsilon) {
        Vec3d eyePos = R0SE.mc.player.getEyePos();
        Box expanded = bounds.expand(epsilon);
        if (expanded.contains(eyePos)) {
            return true;
        }
        return switch (face) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> {
                if (eyePos.z <= expanded.minZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (eyePos.z >= expanded.maxZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (eyePos.x <= expanded.minX) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (eyePos.x >= expanded.maxX) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (eyePos.y >= expanded.maxY) {
                    yield true;
                }
                yield false;
            }
            case Direction.DOWN -> eyePos.y <= expanded.minY;
        };
    }

    private Box getBlockBounds(BlockPos pos) {
        VoxelShape shape = R0SE.mc.world.getBlockState(pos).getCollisionShape((BlockView)R0SE.mc.world, pos, ShapeContext.of((Entity)R0SE.mc.player));
        if (shape.isEmpty()) {
            return new Box(pos);
        }
        return shape.getBoundingBox().offset(pos);
    }

    private boolean isInstantBreak(BlockPos pos) {
        return R0SE.mc.world.getBlockState(pos).isAir();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private AntiCheat.MiningMode getMiningMode() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config == null ? AntiCheat.MiningMode.VANILLA : (AntiCheat.MiningMode)((Object)config.getMining().getValue());
    }

    private MiningProfile getMiningProfile() {
        return MiningProfile.fromMode(this.getMiningMode());
    }

    private long getPacketStartDelayRemaining(MiningProfile profile) {
        if (profile == null || profile.getFinishToStartDelayMs() <= 0L || this.lastPacketFinishAt <= 0L) {
            return 0L;
        }
        long elapsed = System.currentTimeMillis() - this.lastPacketFinishAt;
        long remaining = profile.getFinishToStartDelayMs() - elapsed;
        return Math.max(0L, remaining);
    }

    private void debug(String message) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config == null || !((Boolean)config.getDebug().getValue()).booleanValue()) {
            return;
        }
        Managers.DEBUG.log("MiningDebug", message);
    }

    private void debugAction(String action, String source, BlockPos pos, Direction direction, boolean success, MiningFailReason failReason) {
        this.debug("action=" + action + " source=" + this.safeSource(source) + " success=" + success + " reason=" + String.valueOf((Object)failReason) + " pos=" + this.shortPos(pos) + " dir=" + String.valueOf(direction) + " mode=" + String.valueOf((Object)this.getMiningMode()) + " state=" + String.valueOf((Object)this.state) + " inventory=" + this.describeInventoryState());
    }

    private void logQueueState(String action, String source, boolean changed, MiningQueue.MiningQueueSlot slot) {
        if (!changed) {
            return;
        }
        this.debug("queue action=" + action + " source=" + this.safeSource(source) + " slot=" + String.valueOf((Object)slot) + " " + this.queue.snapshot());
    }

    private MiningQueue.MiningQueueSlot describeQueueSlot(MiningContext context) {
        if (this.queue.isPrimary(context)) {
            return MiningQueue.MiningQueueSlot.PRIMARY;
        }
        if (this.queue.isSecondary(context)) {
            return MiningQueue.MiningQueueSlot.SECONDARY;
        }
        if (this.queue.getPending() == context) {
            return MiningQueue.MiningQueueSlot.PENDING;
        }
        return MiningQueue.MiningQueueSlot.NONE;
    }

    private String describeInventoryState() {
        if (R0SE.mc.player == null) {
            return "no_player";
        }
        SwapState swap = Managers.INVENTORY.getCurrentSwap();
        StringBuilder builder = new StringBuilder();
        builder.append("clientSlot=").append(R0SE.mc.player.getInventory().selectedSlot).append(",serverSlot=").append(Managers.INVENTORY.getServerSlot());
        if (swap == null || !swap.isActive()) {
            builder.append(",swap=inactive");
            return builder.toString();
        }
        builder.append(",swap=active").append(",mode=").append((Object)swap.getMode()).append(",type=").append((Object)swap.getType()).append(",from=").append(swap.getSlotFrom()).append(",to=").append(swap.getSlotTo()).append(",ageMs=").append(System.currentTimeMillis() - swap.getStartedAt());
        return builder.toString();
    }

    private String safeSource(String source) {
        return source == null || source.isBlank() ? "unknown" : source;
    }

    private MiningResult.MiningAction toMiningAction(PlayerActionC2SPacket.Action action) {
        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            return MiningResult.MiningAction.PACKET_START;
        }
        if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            return MiningResult.MiningAction.PACKET_ABORT;
        }
        if (action == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            return MiningResult.MiningAction.PACKET_STOP;
        }
        return MiningResult.MiningAction.PACKET_SEQUENCE;
    }

    private String actionName(PlayerActionC2SPacket.Action action) {
        return action == null ? "null" : action.name().toLowerCase();
    }

    private String shortPos(BlockPos pos) {
        return pos == null ? "null" : pos.toShortString();
    }

    public static enum MiningState {
        IDLE,
        STARTING,
        CONTINUING,
        CANCELLED,
        FINISHED;

    }
}



