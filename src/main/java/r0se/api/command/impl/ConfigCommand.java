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
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class ConfigCommand
extends Command {
    public ConfigCommand() {
        super("config", "Saves or loads client config", new String[]{"cfg"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.then(this.argument("action", StringArgumentType.word()).executes(context -> {
            String action = StringArgumentType.getString((CommandContext)context, (String)"action");
            if (action.equalsIgnoreCase("save")) {
                Managers.CONFIG.saveFeatures();
                Managers.COMMANDS.save();
                ChatUtil.info("Config", "Saved client config");
                return 1;
            }
            if (action.equalsIgnoreCase("load")) {
                Managers.CONFIG.loadFeatures();
                Managers.COMMANDS.load();
                ChatUtil.info("Config", "Loaded client config");
                return 1;
            }
            ChatUtil.error("Config", "Unknown action: " + action + " (use save/load)");
            return 0;
        }));
    }
}

