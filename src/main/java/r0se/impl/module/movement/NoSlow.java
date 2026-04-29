/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil
 */
package r0se.impl.module.movement;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.settings.BoolSetting;
import r0se.mixin.KeyBindingAccessor;
import r0se.util.misc.CheckUtil;

public class NoSlow
extends ToggleableFeature {
    private final BoolSetting invMove = this.bool("InventoryMove", true);

    public NoSlow() {
        super("NoSlow", "Disable/custom some slow item/block", FeatureCategory.MOVEMENT, new String[0]);
        this.setNotifyEnabled(true);
    }

    @Override
    protected void onDisable() {
        this.set(NoSlow.mc.options.forwardKey, false);
        this.set(NoSlow.mc.options.backKey, false);
        this.set(NoSlow.mc.options.leftKey, false);
        this.set(NoSlow.mc.options.rightKey, false);
        this.set(NoSlow.mc.options.jumpKey, false);
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (!this.shouldRunWithScreen()) {
            return;
        }
        if (((Boolean)this.invMove.getValue()).booleanValue() && CheckUtil.checkScreen()) {
            KeyBinding[] keys;
            long handle = mc.getWindow().getHandle();
            for (KeyBinding binding : keys = new KeyBinding[]{NoSlow.mc.options.jumpKey, NoSlow.mc.options.forwardKey, NoSlow.mc.options.backKey, NoSlow.mc.options.rightKey, NoSlow.mc.options.leftKey}) {
                binding.setPressed(InputUtil.isKeyPressed((long)handle, (int)((KeyBindingAccessor)binding).getBoundKey().getCode()));
            }
        }
        if (!((Boolean)this.invMove.getValue()).booleanValue() && NoSlow.mc.currentScreen != null) {
            this.set(NoSlow.mc.options.forwardKey, false);
            this.set(NoSlow.mc.options.backKey, false);
            this.set(NoSlow.mc.options.leftKey, false);
            this.set(NoSlow.mc.options.rightKey, false);
            this.set(NoSlow.mc.options.jumpKey, false);
            return;
        }
    }

    private void set(KeyBinding bind, boolean pressed) {
        bind.setPressed(pressed);
    }
}


