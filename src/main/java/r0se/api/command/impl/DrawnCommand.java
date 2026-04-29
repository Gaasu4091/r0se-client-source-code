/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package r0se.api.command.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import r0se.api.command.Command;
import r0se.api.feature.Feature;
import r0se.api.feature.ToggleableFeature;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class DrawnCommand
extends Command {
    public DrawnCommand() {
        super("drawn", "Changes drawn state for a feature", new String[]{"visible"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.then(this.argument("feature", StringArgumentType.word()).then(this.argument("value", BoolArgumentType.bool()).executes(context -> {
            String input = StringArgumentType.getString((CommandContext)context, (String)"feature");
            boolean value = BoolArgumentType.getBool((CommandContext)context, (String)"value");
            Feature feature = Managers.MODULES.getFeature(input);
            if (!(feature instanceof ToggleableFeature)) {
                ChatUtil.error("Drawn", "Feature not found or not toggleable: " + input);
                return 0;
            }
            ToggleableFeature toggleable = (ToggleableFeature)feature;
            toggleable.getDrawn().setValue(value);
            ChatUtil.info("Drawn", toggleable.getName() + " -> " + (value ? "shown" : "hidden"));
            return 1;
        })));
    }
}

