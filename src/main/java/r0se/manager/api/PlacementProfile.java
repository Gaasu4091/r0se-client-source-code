/*
 * Decompiled with CFR 0.152.
 */
package r0se.manager.api;

import r0se.impl.module.client.AntiCheat;

public enum PlacementProfile {
    VANILLA(AntiCheat.PlacementMode.VANILLA, false, true, 4.5),
    PAPER(AntiCheat.PlacementMode.PAPER, true, true, 4.5);

    private final AntiCheat.PlacementMode mode;
    private final boolean sequencedPacket;
    private final boolean sequencedAirPlace;
    private final double range;

    private PlacementProfile(AntiCheat.PlacementMode mode, boolean sequencedPacket, boolean sequencedAirPlace, double range) {
        this.mode = mode;
        this.sequencedPacket = sequencedPacket;
        this.sequencedAirPlace = sequencedAirPlace;
        this.range = range;
    }

    public static PlacementProfile fromMode(AntiCheat.PlacementMode mode) {
        if (mode == null) {
            return VANILLA;
        }
        for (PlacementProfile profile : PlacementProfile.values()) {
            if (profile.mode != mode) continue;
            return profile;
        }
        return VANILLA;
    }

    public AntiCheat.PlacementMode getMode() {
        return this.mode;
    }

    public boolean shouldUseSequencedPacket(boolean airPlace) {
        return this.sequencedPacket || airPlace && this.sequencedAirPlace;
    }

    public double getRange() {
        return this.range;
    }
}

