/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package r0se.manager.api;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import r0se.manager.api.PlacementContext;
import r0se.manager.api.PlacementFailReason;

public final class PlacementPlan {
    private final boolean valid;
    private final BlockPos targetPos;
    private final BlockPos supportPos;
    private final PlacementContext targetContext;
    private final PlacementContext supportContext;
    private final PlacementFailReason failReason;

    private PlacementPlan(boolean valid, BlockPos targetPos, BlockPos supportPos, PlacementContext targetContext, PlacementContext supportContext, PlacementFailReason failReason) {
        this.valid = valid;
        this.targetPos = targetPos;
        this.supportPos = supportPos;
        this.targetContext = targetContext;
        this.supportContext = supportContext;
        this.failReason = failReason == null ? PlacementFailReason.NONE : failReason;
    }

    public static PlacementPlan direct(BlockPos targetPos, PlacementContext targetContext) {
        return new PlacementPlan(true, targetPos, null, targetContext, null, PlacementFailReason.NONE);
    }

    public static PlacementPlan withSupport(BlockPos targetPos, BlockPos supportPos, PlacementContext targetContext, PlacementContext supportContext) {
        return new PlacementPlan(true, targetPos, supportPos, targetContext, supportContext, PlacementFailReason.NONE);
    }

    public static PlacementPlan fail(BlockPos targetPos, PlacementFailReason failReason) {
        return new PlacementPlan(false, targetPos, null, null, null, failReason);
    }

    public boolean isValid() {
        return this.valid;
    }

    public boolean hasSupport() {
        return this.supportPos != null && this.supportContext != null;
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    public BlockPos getSupportPos() {
        return this.supportPos;
    }

    public PlacementContext getTargetContext() {
        return this.targetContext;
    }

    public PlacementContext getSupportContext() {
        return this.supportContext;
    }

    public PlacementFailReason getFailReason() {
        return this.failReason;
    }

    public List<BlockPos> getRenderPositions() {
        return this.hasSupport() ? List.of(this.supportPos, this.targetPos) : List.of(this.targetPos);
    }
}


