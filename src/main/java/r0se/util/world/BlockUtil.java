/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.PersistentProjectileEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Box
 */
package r0se.util.world;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import r0se.R0SE;

public final class BlockUtil {
    private BlockUtil() {
    }

    public static Set<BlockPos> getSurround(PlayerEntity player) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<BlockPos>();
        if (player == null) {
            return positions;
        }
        Box bb = player.getBoundingBox();
        int minX = (int)Math.floor(bb.minX);
        int maxX = (int)Math.ceil(bb.maxX);
        int minZ = (int)Math.floor(bb.minZ);
        int maxZ = (int)Math.ceil(bb.maxZ);
        int y = (int)Math.floor(player.getY());
        for (int x = minX; x < maxX; ++x) {
            for (int z = minZ; z < maxZ; ++z) {
                BlockPos base = new BlockPos(x, y, z);
                positions.add(base.north());
                positions.add(base.south());
                positions.add(base.east());
                positions.add(base.west());
            }
        }
        return positions;
    }

    public static boolean isReplaceable(BlockPos pos) {
        return R0SE.mc.world != null && R0SE.mc.world.getBlockState(pos).isReplaceable();
    }

    public static boolean hasEntityCollision(BlockPos pos) {
        if (R0SE.mc.world == null) {
            return false;
        }
        Box box = new Box(pos);
        for (Entity entity : R0SE.mc.world.getOtherEntities(null, box)) {
            if (!entity.isAlive() || entity instanceof EndCrystalEntity || entity instanceof ItemEntity || entity instanceof PersistentProjectileEntity || !entity.getBoundingBox().intersects(box)) continue;
            return true;
        }
        return false;
    }

    public static boolean canPlace(BlockPos pos) {
        return BlockUtil.isReplaceable(pos) && !BlockUtil.hasEntityCollision(pos);
    }

    public static boolean hasNeighborSupport(BlockPos pos) {
        if (R0SE.mc.world == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (R0SE.mc.world.getBlockState(pos.offset(direction)).isReplaceable()) continue;
            return true;
        }
        return false;
    }
}


