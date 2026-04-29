/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.hit.BlockHitResult
 */
package r0se.manager.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;

public class PlacementContext {
    private final BlockPos targetPos;
    private final BlockPos clickedPos;
    private final Direction clickedFace;
    private final Vec3d hitPos;
    private final boolean replaceClicked;
    private final boolean airPlace;

    public PlacementContext(BlockPos targetPos, BlockPos clickedPos, Direction clickedFace, Vec3d hitPos, boolean replaceClicked) {
        this(targetPos, clickedPos, clickedFace, hitPos, replaceClicked, false);
    }

    public PlacementContext(BlockPos targetPos, BlockPos clickedPos, Direction clickedFace, Vec3d hitPos, boolean replaceClicked, boolean airPlace) {
        this.targetPos = targetPos;
        this.clickedPos = clickedPos;
        this.clickedFace = clickedFace;
        this.hitPos = hitPos;
        this.replaceClicked = replaceClicked;
        this.airPlace = airPlace;
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    public BlockPos getClickedPos() {
        return this.clickedPos;
    }

    public Direction getClickedFace() {
        return this.clickedFace;
    }

    public Vec3d getHitPos() {
        return this.hitPos;
    }

    public boolean isReplaceClicked() {
        return this.replaceClicked;
    }

    public boolean isAirPlace() {
        return this.airPlace;
    }

    public BlockHitResult toHitResult() {
        return new BlockHitResult(this.hitPos, this.clickedFace, this.clickedPos, this.replaceClicked);
    }
}


