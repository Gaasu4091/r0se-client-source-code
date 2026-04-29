/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.client.network.ClientPlayerEntity
 */
package r0se.api.rotation;

import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayerEntity;
import r0se.api.rotation.Rotation;
import r0se.manager.Managers;

public class RotationHandler {
    private Rotation cachedRotation;

    public Rotation getCachedRotation() {
        return this.cachedRotation;
    }

    public void applyRotations(ClientPlayerEntity player) {
        if (player == null || !Managers.ROTATION.hasClientRotation()) {
            return;
        }
        this.cachedRotation = new Rotation((Entity)player);
        Managers.ROTATION.getClientRotation().apply((Entity)player);
    }

    public void revertRotations(ClientPlayerEntity player) {
        if (player == null || this.cachedRotation == null) {
            return;
        }
        this.cachedRotation.apply((Entity)player);
        this.cachedRotation = null;
    }

    public void clearCachedRotation() {
        this.cachedRotation = null;
    }

    public void resetRotations(Rotation playerRotation, float speed) {
        if (!Managers.ROTATION.hasClientRotation()) {
            return;
        }
        this.cachedRotation = playerRotation;
    }
}


