/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.BlockView
 *  net.minecraft.util.math.Direction
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.block.BlockState
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 */
package r0se.impl.module.client;

import net.minecraft.world.BlockView;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.network.PacketOutboundEvent;
import r0se.api.feature.ConcurrentFeature;
import r0se.api.feature.FeatureCategory;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.EnumSetting;
import r0se.manager.Managers;

public class AntiCheat
extends ConcurrentFeature {
    private final EnumSetting<StrictDirectionMode> strictDirection = this.addSetting(new EnumSetting<StrictDirectionMode>("StrictDirection", StrictDirectionMode.GRIM));
    private final EnumSetting<MiningMode> mining = this.addSetting(new EnumSetting<MiningMode>("Mining", MiningMode.GRIM_STRICT));
    private final EnumSetting<PlacementMode> placements = this.addSetting(new EnumSetting<PlacementMode>("Placements", PlacementMode.PAPER));
    private final EnumSetting<RotationMode> rotations = this.addSetting(new EnumSetting<RotationMode>("Rotations", RotationMode.SILENT));
    private final BoolSetting mineSync = this.addSetting(new BoolSetting("MineSync", false));
    private final BoolSetting placeWait = this.addSetting(new BoolSetting("PlaceWait", false));
    private final BoolSetting movementSync = this.addSetting(new BoolSetting("MovementSync", false));
    private final BoolSetting swing = this.addSetting(new BoolSetting("Swing", true));
    private final BoolSetting multiTask = this.addSetting(new BoolSetting("MultiTask", false));
    private final BoolSetting debug = this.addSetting(new BoolSetting("Debug", false));

    public AntiCheat() {
        super("AntiCheat", "Controls interaction and anticheat-facing behavior.", FeatureCategory.CLIENT, "interactions", "interact", "ac");
    }

    public EnumSetting<StrictDirectionMode> getStrictDirection() {
        return this.strictDirection;
    }

    public EnumSetting<MiningMode> getMining() {
        return this.mining;
    }

    public EnumSetting<PlacementMode> getPlacements() {
        return this.placements;
    }

    public EnumSetting<RotationMode> getRotations() {
        return this.rotations;
    }

    public BoolSetting getMineSync() {
        return this.mineSync;
    }

    public BoolSetting getPlaceWait() {
        return this.placeWait;
    }

    public BoolSetting getMovementSync() {
        return this.movementSync;
    }

    public BoolSetting getSwing() {
        return this.swing;
    }

    public BoolSetting getMultiTask() {
        return this.multiTask;
    }

    public BoolSetting getDebug() {
        return this.debug;
    }

    @Subscribe(priority=10000)
    public void onPacketOutbound(PacketOutboundEvent event) {
        if (!((Boolean)this.mineSync.getValue()).booleanValue() || R0SE.mc.player == null || R0SE.mc.world == null) {
            return;
        }
        if (this.mining.getValue() == MiningMode.VANILLA) {
            return;
        }
        Packet<?> class_25962 = event.getPacket();
        if (!(class_25962 instanceof PlayerActionC2SPacket)) {
            return;
        }
        PlayerActionC2SPacket packet = (PlayerActionC2SPacket)class_25962;
        PlayerActionC2SPacket.Action action = packet.getAction();
        if (action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && action != PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK && action != PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            return;
        }
        BlockState state = R0SE.mc.world.getBlockState(packet.getPos());
        if (state.getHardness((BlockView)R0SE.mc.world, packet.getPos()) == -1.0f) {
            event.cancel();
            this.logDebug("mine-sync cancel unbreakable pos=" + packet.getPos().toShortString());
            return;
        }
        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            Managers.NETWORK.sendQuietPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), Direction.UP), "mine-sync-stop");
            Managers.NETWORK.sendQuietPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, packet.getPos(), Direction.UP), "mine-sync-start");
            event.cancel();
            this.logDebug("mine-sync rewrite start pos=" + packet.getPos().toShortString());
        }
    }

    private void logDebug(String message) {
        if (((Boolean)this.debug.getValue()).booleanValue()) {
            Managers.DEBUG.log("AntiCheatDebug", message);
        }
    }

    public static enum StrictDirectionMode {
        OFF,
        GRIM;

    }

    public static enum MiningMode {
        VANILLA,
        GRIM,
        GRIM_STRICT;

    }

    public static enum PlacementMode {
        VANILLA,
        PAPER;

    }

    public static enum RotationMode {
        MOTION,
        SILENT;

    }
}



