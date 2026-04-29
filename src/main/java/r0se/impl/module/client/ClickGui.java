/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package r0se.impl.module.client;

import net.minecraft.client.gui.screen.Screen;
import r0se.R0SE;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.IntSetting;
import r0se.impl.gui.clickgui.ClickGuiScreen;

public class ClickGui
extends ToggleableFeature {
    public final DoubleSetting scale = this.addSetting(new DoubleSetting("Scale", 1.0, 1.0, 1.0));
    public final BoolSetting lines = this.addSetting(new BoolSetting("Lines", true));
    public final IntSetting guiAlpha = this.addSetting(new IntSetting("UIAlpha", 20, 0, 255));
    public final BoolSetting background = this.addSetting(new BoolSetting("Background", true));
    public final BoolSetting blur = this.addSetting(new BoolSetting("Blur", true));
    public final BoolSetting gradientFill = this.addSetting(new BoolSetting("Gradient", false));
    public final BoolSetting gear = this.addSetting(new BoolSetting("Gear", true));

    public ClickGui() {
        super("ClickGUI", "Opens the main client interface", FeatureCategory.CLIENT, "gui", "click");
        this.getKeyBind().setValue(344);
    }

    @Override
    public void onEnable() {
        Screen current;
        if (ClickGui.mc.world != null && !((current = R0SE.mc.currentScreen) instanceof ClickGuiScreen)) {
            R0SE.mc.setScreen((Screen)new ClickGuiScreen());
        }
    }
}


