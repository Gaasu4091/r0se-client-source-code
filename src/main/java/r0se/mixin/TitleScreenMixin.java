/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package r0se.mixin;

import java.util.Objects;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import r0se.R0SE;

@Mixin(value={TitleScreen.class})
public abstract class TitleScreenMixin
extends Screen {
    @Shadow
    private long backgroundFadeStart;
    @Shadow
    @Final
    private boolean doBackgroundFade;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    public void hookRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        boolean hovered;
        float f = this.doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0f : 1.0f;
        float g = this.doBackgroundFade ? MathHelper.clamp((float)(f - 1.0f), (float)0.0f, (float)1.0f) : 1.0f;
        int alpha = MathHelper.ceil((float)(g * 255.0f)) << 24;
        if ((alpha & 0xFC000000) == 0) {
            return;
        }
        String text = String.format("%s v%s (%s)", R0SE.CLIENT_NAME, R0SE.CLIENT_VERSION, R0SE.CLIENT_STATUS);
        int x = 2;
        Objects.requireNonNull(this.client.textRenderer);
        int y = this.height - 9 * 2 - 2;
        int color = 0xFFFFFF | alpha;
        context.drawTextWithShadow(this.client.textRenderer, text, x, y, color);
        int textWidth = this.client.textRenderer.getWidth(text);
        Objects.requireNonNull(this.client.textRenderer);
        int textHeight = 9;
        boolean bl = hovered = mouseX >= x && mouseX <= x + textWidth && mouseY >= y && mouseY <= y + textHeight;
        if (hovered) {
            context.fill(x, y + textHeight, x + textWidth, y + textHeight - 1, -1);
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        boolean hovered;
        String text = String.format("%s v%s (%s)", R0SE.CLIENT_NAME, R0SE.CLIENT_VERSION, R0SE.CLIENT_STATUS);
        int x = 2;
        Objects.requireNonNull(this.client.textRenderer);
        int y = this.height - 9 * 2 - 2;
        int textWidth = this.client.textRenderer.getWidth(text);
        Objects.requireNonNull(this.client.textRenderer);
        int textHeight = 9;
        boolean bl = hovered = mouseX >= (double)x && mouseX <= (double)(x + textWidth) && mouseY >= (double)y && mouseY <= (double)(y + textHeight);
        if (hovered && button == 0) {
            try {
                Util.getOperatingSystem().open("https://github.com/7rn5/R0SE");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            cir.setReturnValue(true);
        }
    }
}


