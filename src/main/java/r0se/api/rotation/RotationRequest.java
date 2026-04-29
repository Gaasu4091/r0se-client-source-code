/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.rotation;

import r0se.api.rotation.Rotation;

public class RotationRequest {
    private final Rotation rotation;
    private final String source;
    private final int priority;
    private final long createdTick;
    private final long expireTick;

    public RotationRequest(Rotation rotation, String source, int priority, long expireTick) {
        this(rotation, source, priority, 0L, expireTick);
    }

    public RotationRequest(Rotation rotation, String source, int priority, long createdTick, long expireTick) {
        this.rotation = rotation;
        this.source = source;
        this.priority = priority;
        this.createdTick = createdTick;
        this.expireTick = expireTick;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public String getSource() {
        return this.source;
    }

    public int getPriority() {
        return this.priority;
    }

    public long getCreatedTick() {
        return this.createdTick;
    }

    public long getExpireTick() {
        return this.expireTick;
    }

    public boolean isExpired(long currentTick) {
        return currentTick > this.expireTick;
    }
}

