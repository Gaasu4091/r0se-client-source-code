/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.screen.Screen
 */
package r0se.impl.gui.base;

import java.lang.reflect.Method;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;

public class R0SEScreen
extends Screen {
    private static Method APPLY_BLUR_METHOD;
    private static boolean BLUR_LOOKUP_DONE;

    protected R0SEScreen(Text title) {
        super(title);
    }

    protected void applyGameBlur() {
        if (!BLUR_LOOKUP_DONE) {
            BLUR_LOOKUP_DONE = true;
            try {
                APPLY_BLUR_METHOD = Screen.class.getDeclaredMethod("applyBlur", new Class[0]);
                APPLY_BLUR_METHOD.setAccessible(true);
            }
            catch (ReflectiveOperationException ignored) {
                APPLY_BLUR_METHOD = null;
            }
        }
        if (APPLY_BLUR_METHOD == null) {
            return;
        }
        try {
            APPLY_BLUR_METHOD.invoke((Object)this, new Object[0]);
        }
        catch (ReflectiveOperationException ignored) {
            APPLY_BLUR_METHOD = null;
        }
    }
}


