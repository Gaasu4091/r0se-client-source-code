/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Key
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package r0se.mixin;

import java.util.Map;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={KeyBinding.class})
public interface KeyBindingAccessor {
    @Accessor(value="boundKey")
    public InputUtil.Key getBoundKey();

    @Accessor(value="CATEGORY_ORDER_MAP")
    public static Map<String, Integer> getCategoryOrderMap() {
        return null;
    }
}



