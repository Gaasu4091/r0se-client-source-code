/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package r0se.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import r0se.R0SE;
import r0se.api.event.world.AttackBlockEvent;
import r0se.manager.Managers;

@Mixin(value={ClientPlayerInteractionManager.class})
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method={"attackBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void r0se$attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        AttackBlockEvent event = new AttackBlockEvent(pos, direction);
        R0SE.eventHandler.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }
        if (!Managers.MINING.startMining(pos, direction)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"updateBlockBreakingProgress"}, at={@At(value="HEAD")}, cancellable=true)
    private void r0se$updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Managers.MINING.continueMining(pos, direction)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"breakBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void r0se$breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!Managers.MINING.finishMining(pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"cancelBlockBreaking"}, at={@At(value="HEAD")})
    private void r0se$cancelBlockBreaking(CallbackInfo ci) {
        Managers.MINING.cancelMining();
    }
}


