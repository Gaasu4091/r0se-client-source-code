/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.client.network.ClientPlayerEntity
 */
package r0se.manager.impl;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.input.PlayerInputEvent;
import r0se.api.event.network.PlayerUpdateEvent;
import r0se.api.event.network.RotationUpdateEvent;
import r0se.api.event.rotation.ClientRotationEvent;
import r0se.api.rotation.MovementCorrection;
import r0se.api.rotation.Rotation;
import r0se.api.rotation.RotationActionResult;
import r0se.api.rotation.RotationHandler;
import r0se.api.rotation.RotationRequest;
import r0se.impl.module.client.AntiCheat;
import r0se.impl.module.client.Rotations;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class RotationManager
implements Manager {
    private RotationRequest clientRotationRequest;
    private final RotationHandler handler = new RotationHandler();
    private final MovementCorrection movementCorrection = new MovementCorrection();
    private final Rotation serverRotation = new Rotation(0.0f, 0.0f);
    private long tickCounter;

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.clearClientRotation();
        this.handler.clearCachedRotation();
    }

    public Rotation getClientRotation() {
        this.expireStaleRequest();
        return this.clientRotationRequest == null ? null : this.clientRotationRequest.getRotation();
    }

    public RotationRequest getClientRotationRequest() {
        this.expireStaleRequest();
        return this.clientRotationRequest;
    }

    public boolean requestRotation(Rotation rotation, int priority, int keepTicks, String source) {
        if (rotation == null) {
            return false;
        }
        this.expireStaleRequest();
        long expireTick = this.tickCounter + Math.max((long)keepTicks, 1L);
        RotationRequest request = new RotationRequest(rotation, source, priority, this.tickCounter, expireTick);
        if (this.clientRotationRequest != null && this.clientRotationRequest.getPriority() > priority) {
            this.debug("reject source=" + source + " priority=" + priority + " activeSource=" + this.clientRotationRequest.getSource() + " activePriority=" + this.clientRotationRequest.getPriority());
            return false;
        }
        this.clientRotationRequest = request;
        this.debug("accept source=" + source + " priority=" + priority + " keepTicks=" + keepTicks + " yaw=" + String.format("%.2f", Float.valueOf(rotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(rotation.getPitch())));
        return true;
    }

    public void setClientRotation(Rotation clientRotation) {
        this.requestRotation(clientRotation, 0, 1, "legacy");
    }

    public void setRotation(Rotation rotation) {
        this.requestRotation(rotation, 0, 1, "r0se-set");
    }

    public void setRotationSilent(float yaw, float pitch) {
        this.setRotationSilent(new Rotation(yaw, pitch));
    }

    public void setRotationSilent(Rotation rotation) {
        if (rotation == null || R0SE.mc.player == null) {
            return;
        }
        this.requestRotation(rotation, Integer.MAX_VALUE, 1, "r0se-silent");
        this.packetRotate(rotation, true);
    }

    public void setRotationSilentSync() {
        if (R0SE.mc.player == null) {
            return;
        }
        Rotation rotation = new Rotation(R0SE.mc.player.getYaw(), R0SE.mc.player.getPitch());
        this.requestRotation(rotation, Integer.MAX_VALUE, 1, "r0se-silent-sync");
        this.packetRotate(rotation, true);
    }

    public RotationHandler getHandler() {
        return this.handler;
    }

    public MovementCorrection getMovementCorrection() {
        return this.movementCorrection;
    }

    public Rotation getServerRotation() {
        return this.serverRotation;
    }

    public long getTickCounter() {
        return this.tickCounter;
    }

    public void clearClientRotation() {
        this.clientRotationRequest = null;
    }

    public boolean packetRotate(Rotation rotation) {
        return this.packetRotate(rotation, false);
    }

    public boolean packetRotate(Rotation rotation, boolean force) {
        if (rotation == null || R0SE.mc.player == null || R0SE.mc.getNetworkHandler() == null) {
            this.debug("packetRotate failed reason=invalid_state");
            return false;
        }
        if (!force && this.isFacing(rotation.getYaw(), rotation.getPitch())) {
            this.debug("packetRotate skipped reason=already_facing yaw=" + String.format("%.2f", Float.valueOf(rotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(rotation.getPitch())));
            return true;
        }
        ClientPlayerEntity player = R0SE.mc.player;
        R0SE.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), rotation.getYaw(), rotation.getPitch(), player.isOnGround()));
        this.serverRotation.setYaw(rotation.getYaw());
        this.serverRotation.setPitch(rotation.getPitch());
        this.debug("packetRotate yaw=" + String.format("%.2f", Float.valueOf(rotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(rotation.getPitch())));
        return true;
    }

    public RotationActionResult prepareActionRotation(Rotation rotation, int priority, int keepTicks, String source) {
        return this.prepareActionRotation(rotation, priority, keepTicks, source, false, null, -1L);
    }

    public RotationActionResult prepareActionRotation(Rotation rotation, int priority, int keepTicks, String source, boolean waitForNextTick, Rotation pendingRotation, long pendingTick) {
        boolean requested;
        String safeSource;
        if (rotation == null || R0SE.mc.player == null) {
            this.debug("actionRotate rejected source=" + source + " reason=invalid_state");
            return RotationActionResult.rejected(rotation, this.tickCounter);
        }
        String string = safeSource = source == null || source.isBlank() ? "unknown" : source;
        if (waitForNextTick && pendingRotation != null && this.tickCounter > pendingTick) {
            boolean serverFacing = this.isServerFacing(pendingRotation);
            boolean acceptedBeforeTick = this.wasRequestAcceptedBeforeThisTick(pendingRotation, safeSource);
            if (serverFacing || acceptedBeforeTick) {
                this.debug("actionRotate ready source=" + safeSource + " waitTicks=" + (this.tickCounter - pendingTick) + " serverFacing=" + serverFacing + " acceptedBeforeTick=" + acceptedBeforeTick + " delta=" + this.describeDelta(pendingRotation) + " yaw=" + String.format("%.2f", Float.valueOf(pendingRotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(pendingRotation.getPitch())));
                return RotationActionResult.ready(pendingRotation, this.tickCounter);
            }
            this.debug("actionRotate wait source=" + safeSource + " waitTicks=" + (this.tickCounter - pendingTick) + " serverFacing=false acceptedBeforeTick=false delta=" + this.describeDelta(pendingRotation) + " pendingYaw=" + String.format("%.2f", Float.valueOf(pendingRotation.getYaw())) + " pendingPitch=" + String.format("%.2f", Float.valueOf(pendingRotation.getPitch())));
        }
        if (!(requested = this.requestRotation(rotation, priority, keepTicks, safeSource))) {
            this.debug("actionRotate rejected source=" + safeSource + " priority=" + priority + " mode=" + String.valueOf((Object)this.getRotationMode()));
            return RotationActionResult.rejected(rotation, this.tickCounter);
        }
        boolean applied = this.applyActionRotation(rotation);
        this.debug("actionRotate apply source=" + safeSource + " applied=" + applied + " wait=" + waitForNextTick + " mode=" + String.valueOf((Object)this.getRotationMode()) + " tick=" + this.tickCounter + " delta=" + this.describeDelta(rotation) + " yaw=" + String.format("%.2f", Float.valueOf(rotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(rotation.getPitch())));
        if (!applied) {
            return RotationActionResult.rejected(rotation, this.tickCounter);
        }
        return waitForNextTick ? RotationActionResult.waiting(rotation, this.tickCounter) : RotationActionResult.ready(rotation, this.tickCounter);
    }

    private boolean applyActionRotation(Rotation rotation) {
        if (rotation == null || R0SE.mc.player == null) {
            return false;
        }
        if (this.getRotationMode() == AntiCheat.RotationMode.MOTION) {
            rotation.apply((Entity)R0SE.mc.player);
            return true;
        }
        return this.packetRotate(rotation);
    }

    public boolean hasClientRotation() {
        this.expireStaleRequest();
        return this.clientRotationRequest != null;
    }

    public boolean isFacing(float yaw, float pitch) {
        return this.isFacingYaw(yaw) && this.isFacingPitch(pitch);
    }

    public boolean isServerFacing(Rotation rotation) {
        return rotation != null && this.isFacing(rotation.getYaw(), rotation.getPitch());
    }

    public boolean wasRequestAcceptedBeforeThisTick(Rotation rotation, String source) {
        RotationRequest request = this.getClientRotationRequest();
        if (request == null || rotation == null) {
            return false;
        }
        if (source != null && !source.equals(request.getSource())) {
            return false;
        }
        return request.getCreatedTick() < this.tickCounter && Math.abs(MathHelper.wrapDegrees((float)(request.getRotation().getYaw() - rotation.getYaw()))) <= 0.1f && Math.abs(request.getRotation().getPitch() - rotation.getPitch()) <= 0.1f;
    }

    public boolean isFacingYaw(float yaw) {
        float deltaYaw = MathHelper.wrapDegrees((float)(this.serverRotation.getYaw() - yaw));
        return Math.abs(deltaYaw) <= 0.1f;
    }

    public boolean isFacingPitch(float pitch) {
        float clampedPitch = MathHelper.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        return Math.abs(this.serverRotation.getPitch() - clampedPitch) <= 0.1f;
    }

    public AntiCheat.RotationMode getRotationMode() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config == null ? AntiCheat.RotationMode.SILENT : (AntiCheat.RotationMode)((Object)config.getRotations().getValue());
    }

    @Subscribe
    public void onRotationUpdate(RotationUpdateEvent event) {
        Rotations config = Managers.MODULES.getFeature(Rotations.class);
        Rotation updated = new Rotation(event.getYaw(), event.getPitch());
        this.serverRotation.setYaw(updated.getYaw());
        this.serverRotation.setPitch(updated.getPitch());
    }

    @Subscribe(priority=100)
    public void onPlayerUpdatePre(PlayerUpdateEvent.Pre event) {
        ++this.tickCounter;
        this.expireStaleRequest();
        if (R0SE.mc.player == null) {
            return;
        }
        Rotation playerRotation = new Rotation((Entity)R0SE.mc.player);
        ClientRotationEvent rotationEvent = new ClientRotationEvent(playerRotation);
        R0SE.eventHandler.post(rotationEvent);
        if (rotationEvent.isCancelled()) {
            this.requestRotation(rotationEvent.getRotation(), 100, 1, "event");
        } else if (this.hasClientRotation()) {
            this.handler.resetRotations(playerRotation, 1.0f);
        }
        if (this.hasClientRotation() && this.getRotationMode() == AntiCheat.RotationMode.MOTION) {
            this.handler.applyRotations(R0SE.mc.player);
        }
    }

    @Subscribe(priority=100)
    public void onPlayerUpdatePrePacket(PlayerUpdateEvent.PrePacket event) {
        ClientPlayerEntity player = R0SE.mc.player;
        if (player != null && this.hasClientRotation() && this.getRotationMode() == AntiCheat.RotationMode.SILENT) {
            this.handler.applyRotations(player);
        }
    }

    @Subscribe(priority=-100)
    public void onPlayerUpdatePost(PlayerUpdateEvent.Post event) {
        ClientPlayerEntity player = R0SE.mc.player;
        if (player == null) {
            return;
        }
        if (this.getRotationMode() == AntiCheat.RotationMode.SILENT) {
            this.handler.revertRotations(player);
        } else {
            this.handler.clearCachedRotation();
        }
        this.expireStaleRequest();
    }

    @Subscribe
    public void onPlayerInput(PlayerInputEvent event) {
        Rotations config = Managers.MODULES.getFeature(Rotations.class);
        AntiCheat antiCheat = Managers.MODULES.getFeature(AntiCheat.class);
        if (config == null || config.getMoveFix().getValue() == Rotations.MoveFix.OFF || !this.hasClientRotation() || R0SE.mc.player == null) {
            return;
        }
        if (antiCheat != null && !((Boolean)antiCheat.getMovementSync().getValue()).booleanValue()) {
            return;
        }
        float deltaYaw = R0SE.mc.player.getYaw() - this.getClientRotation().getYaw();
        Vec2f corrected = this.movementCorrection.correctMovement(deltaYaw, event.getMovementForward(), event.getMovementSideways());
        event.cancel();
        event.setMovementForward(corrected.y);
        event.setMovementSideways(corrected.x);
    }

    private void expireStaleRequest() {
        if (this.clientRotationRequest != null && this.clientRotationRequest.isExpired(this.tickCounter)) {
            this.debug("expire source=" + this.clientRotationRequest.getSource() + " priority=" + this.clientRotationRequest.getPriority());
            this.clientRotationRequest = null;
        }
    }

    private void debug(String message) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config == null || !((Boolean)config.getDebug().getValue()).booleanValue()) {
            return;
        }
        Managers.DEBUG.log("RotationDebug", message);
    }

    private String describeDelta(Rotation rotation) {
        if (rotation == null) {
            return "none";
        }
        float yawDelta = MathHelper.wrapDegrees((float)(this.serverRotation.getYaw() - rotation.getYaw()));
        float pitchDelta = this.serverRotation.getPitch() - MathHelper.clamp((float)rotation.getPitch(), (float)-90.0f, (float)90.0f);
        return String.format("yaw=%.2f,pitch=%.2f", Float.valueOf(yawDelta), Float.valueOf(pitchDelta));
    }
}



