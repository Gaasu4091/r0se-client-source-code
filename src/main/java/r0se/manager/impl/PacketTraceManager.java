/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 */
package r0se.manager.impl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.network.PacketOutboundEvent;
import r0se.impl.module.client.AntiCheat;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class PacketTraceManager
implements Manager {
    private final Map<Packet<?>, PacketMeta> metadata = Collections.synchronizedMap(new IdentityHashMap());
    private long counter;
    private Direction lastCancelFace;

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.metadata.clear();
        this.lastCancelFace = null;
    }

    public void mark(Packet<?> packet, String source, boolean quiet) {
        if (packet == null) {
            return;
        }
        this.metadata.put(packet, new PacketMeta(source, Managers.ACTIONS.currentLabel(), quiet, System.currentTimeMillis()));
    }

    @Subscribe(priority=-10000)
    public void onPacketOutbound(PacketOutboundEvent event) {
        Packet<?> packet = event.getPacket();
        PacketMeta meta = this.metadata.remove(packet);
        String source = meta == null ? "external" : meta.source();
        String session = meta == null ? Managers.ACTIONS.currentLabel() : meta.session();
        boolean quiet = meta != null && meta.quiet();
        this.log((event.isCancelled() ? "cancelled" : "out") + " id=" + ++this.counter + " session=" + session + " source=" + source + " quiet=" + quiet + " " + this.describe(packet));
        this.analyzeDig(packet, event.isCancelled(), session, source);
    }

    public void logQuiet(Packet<?> packet, String source) {
        PacketMeta meta = this.metadata.remove(packet);
        String session = meta == null ? Managers.ACTIONS.currentLabel() : meta.session();
        String resolvedSource = meta == null ? source : meta.source();
        this.log("quiet id=" + ++this.counter + " session=" + session + " source=" + (resolvedSource == null || resolvedSource.isBlank() ? "unknown" : resolvedSource) + " quiet=true " + this.describe(packet));
        this.analyzeDig(packet, false, session, resolvedSource);
    }

    private void analyzeDig(Packet<?> packet, boolean cancelled, String session, String source) {
        if (cancelled || !(packet instanceof PlayerActionC2SPacket)) {
            return;
        }
        PlayerActionC2SPacket action = (PlayerActionC2SPacket)packet;
        PlayerActionC2SPacket.Action type = action.getAction();
        if (type == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            if (this.lastCancelFace != null && action.getDirection() == this.lastCancelFace) {
                this.lastCancelFace = null;
            } else if (this.lastCancelFace != null) {
                this.log("hint check=PositionBreakB session=" + session + " source=" + source + " lastCancelFace=" + String.valueOf(this.lastCancelFace) + " nextAction=" + String.valueOf(type) + " nextFace=" + String.valueOf(action.getDirection()) + " suggestion=keep cancel/start face aligned or reduce abort packets");
            }
            return;
        }
        if (this.lastCancelFace != null) {
            this.log("hint check=PositionBreakB session=" + session + " source=" + source + " lastCancelFace=" + String.valueOf(this.lastCancelFace) + " nextAction=" + String.valueOf(type) + " nextFace=" + String.valueOf(action.getDirection()) + " suggestion=send matching START before more dig packets");
        }
        if (type == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            this.lastCancelFace = action.getDirection();
        }
    }

    private String describe(Packet<?> packet) {
        if (packet instanceof PlayerActionC2SPacket) {
            PlayerActionC2SPacket action = (PlayerActionC2SPacket)packet;
            return "dig action=" + String.valueOf(action.getAction()) + " pos=" + this.shortPos(action.getPos()) + " face=" + String.valueOf(action.getDirection());
        }
        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            UpdateSelectedSlotC2SPacket slot = (UpdateSelectedSlotC2SPacket)packet;
            return "slot selected=" + slot.getSelectedSlot();
        }
        if (packet instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket interact = (PlayerInteractBlockC2SPacket)packet;
            return "place pos=" + this.shortPos(interact.getBlockHitResult().getBlockPos()) + " face=" + String.valueOf(interact.getBlockHitResult().getSide()) + " hand=" + String.valueOf(interact.getHand());
        }
        if (packet instanceof HandSwingC2SPacket) {
            HandSwingC2SPacket swing = (HandSwingC2SPacket)packet;
            return "swing hand=" + String.valueOf(swing.getHand());
        }
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket move = (PlayerMoveC2SPacket)packet;
            return "move look=" + move.changesLook() + " pos=" + move.changesPosition() + " yaw=" + (move.changesLook() ? this.fmt(move.getYaw(0.0f)) : "same") + " pitch=" + (move.changesLook() ? this.fmt(move.getPitch(0.0f)) : "same");
        }
        return "packet=" + packet.getClass().getSimpleName();
    }

    private String shortPos(BlockPos pos) {
        return pos == null ? "null" : pos.toShortString();
    }

    private String fmt(float value) {
        return String.format("%.2f", Float.valueOf(value));
    }

    private void log(String message) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config != null && ((Boolean)config.getDebug().getValue()).booleanValue()) {
            Managers.DEBUG.log("PacketTrace", message);
        }
    }

    private record PacketMeta(String source, String session, boolean quiet, long time) {
    }
}



