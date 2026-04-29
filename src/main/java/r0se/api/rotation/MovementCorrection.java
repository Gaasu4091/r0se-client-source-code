/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.MathHelper
 */
package r0se.api.rotation;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.MathHelper;
import r0se.impl.module.client.Rotations;
import r0se.manager.Managers;

public class MovementCorrection {
    public Vec2f correctMovement(float deltaYaw, float forward, float sideways) {
        Rotations rotations = Managers.MODULES.getFeature(Rotations.class);
        float delta = deltaYaw * ((float)Math.PI / 180);
        float cos = MathHelper.cos((float)delta);
        float sin = MathHelper.sin((float)delta);
        float correctedForward = forward * cos + sideways * sin;
        float correctedSideways = sideways * cos - forward * sin;
        if (rotations != null && rotations.getMoveFix().getValue() == Rotations.MoveFix.NORMAL) {
            correctedForward = Math.round(correctedForward);
            correctedSideways = Math.round(correctedSideways);
        }
        Vec2f movement = new Vec2f(correctedSideways, correctedForward);
        return rotations != null && (Boolean)rotations.getNormalizeMovement().getValue() != false ? movement.normalize() : movement;
    }
}


