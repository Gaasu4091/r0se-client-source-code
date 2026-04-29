/*
 * Decompiled with CFR 0.152.
 */
package r0se.manager;

import r0se.manager.impl.ActionSessionManager;
import r0se.manager.impl.BlockProgressManager;
import r0se.manager.impl.ChatManager;
import r0se.manager.impl.ColorManager;
import r0se.manager.impl.CommandManager;
import r0se.manager.impl.ConfigManager;
import r0se.manager.impl.DebugManager;
import r0se.manager.impl.InputManager;
import r0se.manager.impl.InteractManager;
import r0se.manager.impl.InventoryManager;
import r0se.manager.impl.MiningManager;
import r0se.manager.impl.ModuleManager;
import r0se.manager.impl.NetworkManager;
import r0se.manager.impl.PacketTraceManager;
import r0se.manager.impl.RenderManager;
import r0se.manager.impl.RotationManager;
import r0se.manager.impl.SocialManager;
import r0se.manager.impl.TargetManager;

public final class Managers {
    public static final ModuleManager MODULES = new ModuleManager();
    public static final CommandManager COMMANDS = new CommandManager();
    public static final NetworkManager NETWORK = new NetworkManager();
    public static final RotationManager ROTATION = new RotationManager();
    public static final InventoryManager INVENTORY = new InventoryManager();
    public static final InteractManager INTERACT = new InteractManager();
    public static final MiningManager MINING = new MiningManager();
    public static final BlockProgressManager BLOCKS = new BlockProgressManager();
    public static final SocialManager SOCIAL = new SocialManager();
    public static final TargetManager TARGETING = new TargetManager();
    public static final RenderManager RENDER = new RenderManager();
    public static final ColorManager COLORS = new ColorManager();
    public static final ChatManager CHAT = new ChatManager();
    public static final InputManager INPUT = new InputManager();
    public static final ConfigManager CONFIG = new ConfigManager();
    public static final DebugManager DEBUG = new DebugManager();
    public static final ActionSessionManager ACTIONS = new ActionSessionManager();
    public static final PacketTraceManager TRACE = new PacketTraceManager();

    private Managers() {
    }

    public static void initialize() {
        MODULES.init();
        COMMANDS.init();
        NETWORK.init();
        ROTATION.init();
        INVENTORY.init();
        INTERACT.init();
        MINING.init();
        BLOCKS.init();
        SOCIAL.init();
        TARGETING.init();
        RENDER.init();
        COLORS.init();
        CHAT.init();
        INPUT.init();
        CONFIG.init();
        DEBUG.init();
        ACTIONS.init();
        TRACE.init();
    }

    public static void shutdown() {
        TRACE.shutdown();
        ACTIONS.shutdown();
        DEBUG.shutdown();
        CONFIG.shutdown();
        INPUT.shutdown();
        CHAT.shutdown();
        COLORS.shutdown();
        RENDER.shutdown();
        TARGETING.shutdown();
        SOCIAL.shutdown();
        BLOCKS.shutdown();
        MINING.shutdown();
        INTERACT.shutdown();
        INVENTORY.shutdown();
        ROTATION.shutdown();
        NETWORK.shutdown();
        COMMANDS.shutdown();
        MODULES.shutdown();
    }
}

