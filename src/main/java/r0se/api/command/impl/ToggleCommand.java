/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package r0se.api.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import r0se.api.command.Command;
import r0se.api.feature.Feature;
import r0se.api.feature.ToggleableFeature;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class ToggleCommand
extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a feature", new String[]{"t"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.then(this.argument("feature", StringArgumentType.word()).executes(context -> {
            String input = StringArgumentType.getString((CommandContext)context, (String)"feature");
            Feature feature = Managers.MODULES.getFeature(input);
            if (!(feature instanceof ToggleableFeature)) {
                ChatUtil.error("Toggle", "Feature not found or not toggleable: " + input);
                return 0;
            }
            ToggleableFeature toggleable = (ToggleableFeature)feature;
            toggleable.toggle();
            ChatUtil.info("Toggle", toggleable.getName() + " -> " + (toggleable.isEnabled() ? "ON" : "OFF"));
            return 1;
        }));
    }
}

