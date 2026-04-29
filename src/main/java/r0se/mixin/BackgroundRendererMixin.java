/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.BackgroundRenderer
 *  net.minecraft.client.render.BackgroundRenderer$FogType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package r0se.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import r0se.impl.module.render.NoRender;
import r0se.manager.Managers;

@Mixin(value={BackgroundRenderer.class})
public class BackgroundRendererMixin {
    @Inject(method={"applyFog"}, at={@At(value="TAIL")})
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        NoRender noRender = Managers.MODULES.getFeature(NoRender.class);
        if (noRender != null && noRender.isEnabled() && noRender.getFog()) {
            RenderSystem.setShaderFogStart((float)(viewDistance * 4.0f));
            RenderSystem.setShaderFogEnd((float)(viewDistance * 4.25f));
        }
    }
}



