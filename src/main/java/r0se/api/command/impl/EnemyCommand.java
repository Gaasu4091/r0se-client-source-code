/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package r0se.api.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Set;
import r0se.api.command.Command;
import r0se.api.social.SocialType;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class EnemyCommand
extends Command {
    public EnemyCommand() {
        super("enemy", "Manages enemy socials", new String[]{"enemies", "e"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.executes(context -> {
            this.listEnemies();
            return 1;
        });
        builder.then(((RequiredArgumentBuilder)this.argument("action", StringArgumentType.word()).executes(context -> {
            String action = StringArgumentType.getString((CommandContext)context, (String)"action");
            if (action.equalsIgnoreCase("list")) {
                this.listEnemies();
                return 1;
            }
            ChatUtil.error("Enemy", "Usage: enemy <add|remove|toggle|list> [name]");
            return 0;
        })).then(this.argument("name", StringArgumentType.greedyString()).executes(context -> {
            String action = StringArgumentType.getString((CommandContext)context, (String)"action");
            String name = StringArgumentType.getString((CommandContext)context, (String)"name").trim();
            if (name.isEmpty()) {
                ChatUtil.error("Enemy", "Name cannot be empty");
                return 0;
            }
            switch (action.toLowerCase()) {
                case "add": {
                    Managers.SOCIAL.addEnemy(name);
                    ChatUtil.info("Enemy", "Added " + name);
                    return 1;
                }
                case "remove": 
                case "del": 
                case "delete": {
                    boolean removed = Managers.SOCIAL.removeEnemy(name);
                    ChatUtil.info("Enemy", removed ? "Removed " + name : name + " was not an enemy");
                    return removed ? 1 : 0;
                }
                case "toggle": {
                    boolean enabled = Managers.SOCIAL.toggleEnemy(name);
                    ChatUtil.info("Enemy", (enabled ? "Added " : "Removed ") + name);
                    return 1;
                }
            }
            ChatUtil.error("Enemy", "Unknown action: " + action);
            return 0;
        })));
    }

    private void listEnemies() {
        Set<String> enemies = Managers.SOCIAL.getByType(SocialType.ENEMY);
        if (enemies.isEmpty()) {
            ChatUtil.info("Enemy", "No enemies added");
            return;
        }
        ChatUtil.info("Enemy", "Enemies: " + String.join((CharSequence)", ", enemies));
    }
}

