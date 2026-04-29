/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.RenderTickCounter
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package r0se.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import r0se.R0SE;
import r0se.api.event.render.RenderOverlayEvent;
import r0se.impl.module.render.NoRender;
import r0se.manager.Managers;

@Mixin(value={InGameHud.class})
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void r0se$renderOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client.options.hudHidden) {
            return;
        }
        R0SE.eventHandler.post(new RenderOverlayEvent(context, tickCounter.getTickDelta(true)));
    }

    @Inject(method={"renderPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (this.getNoRender().getBlindness()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderSpyglassOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderSpyglassOverlay(DrawContext context, float scale, CallbackInfo ci) {
        if (this.getNoRender().getBlindness()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderVignetteOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderVignetteOverlay(DrawContext context, Entity entity, CallbackInfo ci) {
        if (this.getNoRender().getBlindness()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method={"renderMiscOverlays"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal=0))
    private void onRenderPumpkinOverlay(Args args) {
        if (this.getNoRender().getBlindness()) {
            args.set(2, (Object)Float.valueOf(0.0f));
        }
    }

    @Inject(method={"renderHeldItemTooltip"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        if (this.getNoRender().getHeldTooltip()) {
            ci.cancel();
        }
    }

    private NoRender getNoRender() {
        return Managers.MODULES.getFeature(NoRender.class);
    }
}


