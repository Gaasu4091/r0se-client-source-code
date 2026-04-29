/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Type
 */
package r0se.util.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import r0se.util.world.BlockUtil;

public final class ProtectionUtil {
    private ProtectionUtil() {
    }

    public static List<BlockPos> getSurroundPlacements(PlayerEntity player, boolean allowAirPlace) {
        if (player == null) {
            return List.of();
        }
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        BlockPos base = player.getBlockPos();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos surround = base.offset(direction);
            if (!BlockUtil.canPlace(surround) || blocks.contains(surround)) continue;
            blocks.add(surround);
        }
        return blocks;
    }
}



