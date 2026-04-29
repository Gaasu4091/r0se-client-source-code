/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.LightmapTextureManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package r0se.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import r0se.impl.module.render.Ambience;
import r0se.impl.module.render.NoRender;
import r0se.manager.Managers;

@Mixin(value={LightmapTextureManager.class})
public class LightmapTextureManagerMixin {
    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void updateLight(Args args) {
        if (this.getAmbience().getFullbright()) {
            args.set(2, -1);
        }
    }

    @Inject(method={"getDarknessFactor"}, at={@At(value="HEAD")}, cancellable=true)
    private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> ci) {
        if (this.getNoRender().getDarkness()) {
            ci.setReturnValue(Float.valueOf(0.0f));
        }
    }

    private NoRender getNoRender() {
        return Managers.MODULES.getFeature(NoRender.class);
    }

    private Ambience getAmbience() {
        return Managers.MODULES.getFeature(Ambience.class);
    }
}


