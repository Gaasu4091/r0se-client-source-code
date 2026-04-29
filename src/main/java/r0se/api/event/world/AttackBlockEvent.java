/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package r0se.api.event.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import r0se.api.event.Event;

public class AttackBlockEvent
extends Event {
    private final BlockPos pos;
    private final Direction direction;

    public AttackBlockEvent(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }
}


