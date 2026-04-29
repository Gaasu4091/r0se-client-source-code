/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.client.network.SequencedPacketCreator
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package r0se.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.SequencedPacketCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ClientPlayerInteractionManager.class})
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor(value="currentBreakingPos")
    public BlockPos r0se$getCurrentBreakingPos();

    @Accessor(value="currentBreakingProgress")
    public float r0se$getCurrentBreakingProgress();

    @Accessor(value="currentBreakingProgress")
    public void r0se$setCurrentBreakingProgress(float var1);

    @Accessor(value="blockBreakingCooldown")
    public int r0se$getBlockBreakingCooldown();

    @Accessor(value="blockBreakingCooldown")
    public void r0se$setBlockBreakingCooldown(int var1);

    @Invoker(value="sendSequencedPacket")
    public void r0se$sendSequencedPacket(ClientWorld var1, SequencedPacketCreator var2);
}


