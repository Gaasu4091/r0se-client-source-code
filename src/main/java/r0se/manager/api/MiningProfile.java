/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 */
package r0se.manager.api;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import r0se.impl.module.client.AntiCheat;

public enum MiningProfile {
    VANILLA(AntiCheat.MiningMode.VANILLA, new PlayerActionC2SPacket.Action[]{PlayerActionC2SPacket.Action.START_DESTROY_BLOCK}, new PlayerActionC2SPacket.Action[]{PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK}, 0, 0L, true),
    GRIM(AntiCheat.MiningMode.GRIM, new PlayerActionC2SPacket.Action[]{PlayerActionC2SPacket.Action.START_DESTROY_BLOCK}, new PlayerActionC2SPacket.Action[]{PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK}, 0, 280L, true),
    GRIM_STRICT(AntiCheat.MiningMode.GRIM_STRICT, new PlayerActionC2SPacket.Action[]{PlayerActionC2SPacket.Action.START_DESTROY_BLOCK}, new PlayerActionC2SPacket.Action[]{PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK}, 1, 280L, true);

    private final AntiCheat.MiningMode mode;
    private final PlayerActionC2SPacket.Action[] startSequence;
    private final PlayerActionC2SPacket.Action[] stopSequence;
    private final int strictStopWaitTicks;
    private final long finishToStartDelayMs;
    private final boolean packetSwingAllowed;

    private MiningProfile(AntiCheat.MiningMode mode, PlayerActionC2SPacket.Action[] startSequence, PlayerActionC2SPacket.Action[] stopSequence, int strictStopWaitTicks, long finishToStartDelayMs, boolean packetSwingAllowed) {
        this.mode = mode;
        this.startSequence = startSequence;
        this.stopSequence = stopSequence;
        this.strictStopWaitTicks = strictStopWaitTicks;
        this.finishToStartDelayMs = finishToStartDelayMs;
        this.packetSwingAllowed = packetSwingAllowed;
    }

    public static MiningProfile fromMode(AntiCheat.MiningMode mode) {
        if (mode == null) {
            return VANILLA;
        }
        for (MiningProfile profile : MiningProfile.values()) {
            if (profile.mode != mode) continue;
            return profile;
        }
        return VANILLA;
    }

    public AntiCheat.MiningMode getMode() {
        return this.mode;
    }

    public PlayerActionC2SPacket.Action[] getStartSequence() {
        return this.startSequence;
    }

    public PlayerActionC2SPacket.Action[] getStopSequence() {
        return this.stopSequence;
    }

    public int getStrictStopWaitTicks() {
        return this.strictStopWaitTicks;
    }

    public long getFinishToStartDelayMs() {
        return this.finishToStartDelayMs;
    }

    public boolean shouldPacketSwing(boolean requested) {
        return requested && this.packetSwingAllowed;
    }

    public String describeStartSequence() {
        return this.describe(this.startSequence);
    }

    public String describeStopSequence() {
        return this.describe(this.stopSequence);
    }

    private String describe(PlayerActionC2SPacket.Action[] sequence) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sequence.length; ++i) {
            if (i > 0) {
                builder.append("->");
            }
            builder.append(sequence[i].name());
        }
        return builder.toString();
    }
}



