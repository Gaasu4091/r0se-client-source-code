/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.client.render.Frustum
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.WorldRenderer
 *  net.minecraft.client.render.LightmapTextureManager
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.client.render.RenderTickCounter
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.R0SE;
import r0se.api.event.render.RenderWorldEvent;
import r0se.impl.imixin.IWorldRendererAccess;

@Mixin(value={WorldRenderer.class})
public abstract class WorldRendererMixin
implements IWorldRendererAccess {
    @Override
    @Accessor(value="frustum")
    public abstract Frustum r0se$getFrustum();

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void hookRenderWorld(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(gameRenderer.getCamera().getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(gameRenderer.getCamera().getYaw() + 180.0f));
        R0SE.eventHandler.post(new RenderWorldEvent(matrices, tickCounter.getTickDelta(true)));
        matrices.pop();
    }
}


