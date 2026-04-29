/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket
 *  net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.network.packet.s2c.play.BundleS2CPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.network.RotationUpdateEvent;
import r0se.impl.module.world.SpeedMine;
import r0se.manager.Managers;

@Mixin(value={ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method={"onPlayerPositionLook"}, at={@At(value="HEAD")})
    private void r0se$rotationUpdatePre(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        R0SE.eventHandler.post(new RotationUpdateEvent.Pre());
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V", shift=At.Shift.BEFORE, ordinal=0)})
    private void r0se$rotationUpdatePrePacket(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        R0SE.eventHandler.post(new RotationUpdateEvent.PrePacket());
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="TAIL")})
    private void r0se$rotationUpdatePost(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (R0SE.mc.player != null) {
            R0SE.eventHandler.post(new RotationUpdateEvent(R0SE.mc.player.getYaw(), R0SE.mc.player.getPitch()));
        }
    }

    @Inject(method={"onUpdateSelectedSlot"}, at={@At(value="TAIL")})
    private void r0se$inventoryUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet, CallbackInfo ci) {
        Managers.INVENTORY.setServerSlot(packet.getSlot());
    }

    @Inject(method={"onBlockUpdate"}, at={@At(value="TAIL")})
    private void r0se$miningBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        SpeedMine speedMine = Managers.MODULES.getFeature(SpeedMine.class);
        if (packet.getState().isAir()) {
            Managers.BLOCKS.clear(packet.getPos(), "block_update_air");
            Managers.MINING.confirmServerAir(packet.getPos(), "block_update");
            if (speedMine != null) {
                speedMine.onServerAirConfirmed(packet.getPos());
            }
        } else if (speedMine != null) {
            speedMine.onServerSolidConfirmed(packet.getPos());
        }
    }

    @Inject(method={"onBlockBreakingProgress"}, at={@At(value="TAIL")})
    private void r0se$blockBreakingProgress(BlockBreakingProgressS2CPacket packet, CallbackInfo ci) {
        Managers.BLOCKS.onBreakProgress(packet.getEntityId(), packet.getPos());
    }

    @Inject(method={"onBundle"}, at={@At(value="TAIL")})
    private void r0se$miningBundleUpdate(BundleS2CPacket packet, CallbackInfo ci) {
        for (Packet bundled : packet.getPackets()) {
            if (!(bundled instanceof BlockUpdateS2CPacket)) continue;
            BlockUpdateS2CPacket blockUpdate = (BlockUpdateS2CPacket)bundled;
            SpeedMine speedMine = Managers.MODULES.getFeature(SpeedMine.class);
            if (blockUpdate.getState().isAir()) {
                Managers.BLOCKS.clear(blockUpdate.getPos(), "bundle_block_update_air");
                Managers.MINING.confirmServerAir(blockUpdate.getPos(), "bundle_block_update");
                if (speedMine == null) continue;
                speedMine.onServerAirConfirmed(blockUpdate.getPos());
                continue;
            }
            if (speedMine == null) continue;
            speedMine.onServerSolidConfirmed(blockUpdate.getPos());
        }
    }
}


