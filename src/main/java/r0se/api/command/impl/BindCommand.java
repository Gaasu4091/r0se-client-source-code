/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  org.lwjgl.glfw.GLFW
 */
package r0se.api.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.Field;
import java.util.Locale;
import org.lwjgl.glfw.GLFW;
import r0se.api.command.Command;
import r0se.api.feature.BindMode;
import r0se.api.feature.Feature;
import r0se.api.feature.ToggleableFeature;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class BindCommand
extends Command {
    public BindCommand() {
        super("bind", "Changes a feature keybind", new String[]{"b"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)this.argument("feature", StringArgumentType.word()).then(this.argument("key", StringArgumentType.greedyString()).executes(context -> {
            String input = StringArgumentType.getString((CommandContext)context, (String)"feature");
            String keyName = StringArgumentType.getString((CommandContext)context, (String)"key");
            Feature feature = Managers.MODULES.getFeature(input);
            if (!(feature instanceof ToggleableFeature)) {
                ChatUtil.error("Bind", "Feature not found or not toggleable: " + input);
                return 0;
            }
            ToggleableFeature toggleable = (ToggleableFeature)feature;
            int key = this.parseKey(keyName);
            toggleable.getKeyBind().setValue(key);
            ChatUtil.info("Bind", toggleable.getName() + " -> " + this.formatKey(key));
            return 1;
        }))).then(this.argument("mode", StringArgumentType.word()).executes(context -> {
            String input = StringArgumentType.getString((CommandContext)context, (String)"feature");
            String modeName = StringArgumentType.getString((CommandContext)context, (String)"mode");
            Feature feature = Managers.MODULES.getFeature(input);
            if (!(feature instanceof ToggleableFeature)) {
                ChatUtil.error("Bind", "Feature not found or not toggleable: " + input);
                return 0;
            }
            ToggleableFeature toggleable = (ToggleableFeature)feature;
            BindMode mode = this.parseMode(modeName);
            if (mode == null) {
                ChatUtil.error("Bind", "Unknown bind mode: " + modeName);
                return 0;
            }
            toggleable.getBindMode().setValue(mode);
            ChatUtil.info("Bind", toggleable.getName() + " mode -> " + String.valueOf((Object)mode));
            return 1;
        }))).then(this.argument("key", StringArgumentType.word()).then(this.argument("mode", StringArgumentType.word()).executes(context -> {
            String input = StringArgumentType.getString((CommandContext)context, (String)"feature");
            String keyName = StringArgumentType.getString((CommandContext)context, (String)"key");
            String modeName = StringArgumentType.getString((CommandContext)context, (String)"mode");
            Feature feature = Managers.MODULES.getFeature(input);
            if (!(feature instanceof ToggleableFeature)) {
                ChatUtil.error("Bind", "Feature not found or not toggleable: " + input);
                return 0;
            }
            ToggleableFeature toggleable = (ToggleableFeature)feature;
            int key = this.parseKey(keyName);
            BindMode mode = this.parseMode(modeName);
            if (mode == null) {
                ChatUtil.error("Bind", "Unknown bind mode: " + modeName);
                return 0;
            }
            toggleable.getKeyBind().setValue(key);
            toggleable.getBindMode().setValue(mode);
            ChatUtil.info("Bind", toggleable.getName() + " -> " + this.formatKey(key) + " (" + String.valueOf((Object)mode) + ")");
            return 1;
        }))));
    }

    private int parseKey(String keyName) {
        if (keyName == null) {
            return -1;
        }
        String normalized = keyName.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("NONE") || normalized.equals("UNBOUND")) {
            return -1;
        }
        try {
            Field field = GLFW.class.getField("GLFW_KEY_" + normalized.replace(' ', '_'));
            return field.getInt(null);
        }
        catch (Exception field) {
            if (normalized.startsWith("MOUSE")) {
                try {
                    int index = Integer.parseInt(normalized.replace("MOUSE", "").trim());
                    return 0 + Math.max(0, index - 1);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return -1;
        }
    }

    private String formatKey(int key) {
        if (key < 0) {
            return "NONE";
        }
        if (key >= 0 && key <= 7) {
            return "MOUSE" + (key - 0 + 1);
        }
        String name = GLFW.glfwGetKeyName((int)key, (int)0);
        return name == null ? Integer.toString(key) : name.toUpperCase(Locale.ROOT);
    }

    private BindMode parseMode(String value) {
        String normalized;
        if (value == null) {
            return null;
        }
        return switch (normalized = value.trim().toUpperCase(Locale.ROOT).replace("-", "").replace("_", "")) {
            case "TOGGLE" -> BindMode.TOGGLE;
            case "HOLD" -> BindMode.HOLD;
            case "REVERSEHOLD", "REVERSE", "REVERSEDHOLD", "REVERSHOLD" -> BindMode.REVERSE_HOLD;
            default -> null;
        };
    }
}

