/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.input.Input
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package r0se.mixin;

import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Input.class})
public interface InputAccessor {
    @Accessor(value="movementForward")
    public float r0se$getMovementForward();

    @Accessor(value="movementForward")
    public void r0se$setMovementForward(float var1);

    @Accessor(value="movementSideways")
    public float r0se$getMovementSideways();

    @Accessor(value="movementSideways")
    public void r0se$setMovementSideways(float var1);
}


