/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.util.Hand
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.ToolItem
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.world.BlockView
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShapes
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.block.BlockState
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.registry.entry.RegistryEntry
 */
package r0se.impl.module.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.entry.RegistryEntry;
import r0se.R0SE;
import r0se.api.collection.FirstOutQueue;
import r0se.api.event.Subscribe;
import r0se.api.event.render.RenderWorldEvent;
import r0se.api.event.world.AttackBlockEvent;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.render.BoxRenderMode;
import r0se.api.render.ColorUtil;
import r0se.api.render.Easing;
import r0se.api.render.animation.Animation;
import r0se.api.rotation.Rotation;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.IntSetting;
import r0se.api.settings.Setting;
import r0se.impl.module.client.AntiCheat;
import r0se.manager.Managers;
import r0se.manager.api.MiningFailReason;
import r0se.manager.impl.ActionSessionManager;

public class SpeedMine
extends ToggleableFeature {
    private static final int ASYNC_INSTANT_BURST = 4;
    private final DoubleSetting damage = this.addSetting(new DoubleSetting("Damage", 0.7, 0.7, 1.0).precision(1));
    private final DoubleSetting range = this.addSetting(new DoubleSetting("Range", 5.0, 1.0, 6.0));
    private final BoolSetting doubleMine = this.addSetting(new BoolSetting("DoubleMine", true));
    private final GroupSetting rebreakGroup = this.addSetting(new GroupSetting("RebreakGroup", "Rebreak", false));
    private final EnumSetting<RebreakMode> rebreak = this.addSetting((EnumSetting)((Setting)new EnumSetting<RebreakMode>("Rebreak", RebreakMode.FAST).insideGroup(this.rebreakGroup)).visibleWhen(this.rebreakGroup::isExpanded));
    private final BoolSetting async = this.addSetting((BoolSetting)((Setting)new BoolSetting("Async", false).insideGroup(this.rebreakGroup)).visibleWhen(() -> this.rebreakGroup.isExpanded() && this.rebreak.getValue() == RebreakMode.INSTANT));
    private final DoubleSetting instantDelay = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("InstantDelay", 0.0, 0.0, 0.25).insideGroup(this.rebreakGroup)).visibleWhen(() -> this.rebreakGroup.isExpanded() && this.rebreak.getValue() == RebreakMode.INSTANT));
    private final BoolSetting holdingBest = this.addSetting((BoolSetting)((Setting)new BoolSetting("HoldingBest", true).insideGroup(this.rebreakGroup)).visibleWhen(this.rebreakGroup::isExpanded));
    private final GroupSetting autoSwapGroup = this.addSetting(new GroupSetting("AutoSwapGroup", "AutoSwap", false));
    private final EnumSetting<AutoSwapMode> autoSwap = this.addSetting((EnumSetting)((Setting)new EnumSetting<AutoSwapMode>("AutoSwap", AutoSwapMode.SILENT).insideGroup(this.autoSwapGroup)).visibleWhen(this.autoSwapGroup::isExpanded));
    private final BoolSetting alternative = this.addSetting((BoolSetting)((Setting)new BoolSetting("Alternative", false).insideGroup(this.autoSwapGroup)).visibleWhen(this.autoSwapGroup::isExpanded));
    private final BoolSetting rotate = this.addSetting((BoolSetting)new BoolSetting("Rotate", true).hide());
    private final GroupSetting rotateGroup = this.addSetting(new GroupSetting("RotateGroup", "Rotate", false).linkToggle(this.rotate));
    private final IntSetting limit = this.addSetting((IntSetting)((Setting)new IntSetting("Limit", 2, 1, 8).insideGroup(this.rotateGroup)).visibleWhen(this.rotateGroup::isExpanded));
    private final BoolSetting render = this.addSetting((BoolSetting)new BoolSetting("Enabled", true).hide());
    private final GroupSetting renderGroup = this.addSetting(new GroupSetting("Render", false).linkToggle(this.render));
    private final ColorSetting fillColor = this.addSetting((ColorSetting)((Setting)new ColorSetting("Fill", new Color(64, 54, 128, 70)).enableSync(ColorSyncMode.SECONDARY).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final ColorSetting outlineColor = this.addSetting((ColorSetting)((Setting)new ColorSetting("Outline", new Color(178, 154, 255, 210)).enableSync(ColorSyncMode.PRIMARY).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final DoubleSetting lineWidth = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("LineWidth", 1.0, 0.1, 4.0).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final BoolSetting fade = this.addSetting((BoolSetting)((Setting)new BoolSetting("Fade", true).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final DoubleSetting fadeTime = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("FadeTime", 1.0, 0.1, 2.0).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final EnumSetting<BoxRenderMode> renderMode = this.addSetting((EnumSetting)((Setting)new EnumSetting<BoxRenderMode>("RenderMode", BoxRenderMode.BOTH).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final BoolSetting debug = this.addSetting(new BoolSetting("Debug", false));
    private final Map<MiningData, Animation> fadeList = new HashMap<MiningData, Animation>();
    private FirstOutQueue<MiningData> miningQueue = new FirstOutQueue(2);
    private long lastBreak;
    private long lastInstantAt;

    public SpeedMine() {
        super("SpeedMine", "Mines blocks faster with a packet mining queue.", FeatureCategory.WORLD, "speedy", "doublemine");
        this.getNotify().setValue(true);
    }

    @Override
    protected void onEnable() {
        this.miningQueue = new FirstOutQueue((Boolean)this.doubleMine.getValue() != false ? 2 : 1);
    }

    @Override
    protected void onDisable() {
        this.miningQueue.clear();
        this.fadeList.clear();
        this.syncToClientSlot();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        this.syncDoubleMineQueue();
        if (R0SE.mc.player == null || R0SE.mc.world == null || R0SE.mc.getNetworkHandler() == null) {
            this.miningQueue.clear();
            return;
        }
        if (R0SE.mc.player.isCreative() || R0SE.mc.player.isSpectator()) {
            return;
        }
        if (this.miningQueue.isEmpty()) {
            return;
        }
        Iterator<MiningData> iterator = this.miningQueue.iterator();
        while (iterator.hasNext()) {
            MiningData data = iterator.next();
            data.updateObservedState();
            if (data.getState().isAir()) {
                data.resetBreakTime();
            }
            if (this.isDataPacketMine(data)) {
                if (!this.isWithinRange(data.getPos())) {
                    this.syncToClientSlot();
                    this.logDebug("queue-remove pos=" + data.getPos().toShortString() + " reason=secondary_range attempted=" + data.hasAttemptedBreak());
                    iterator.remove();
                    continue;
                }
                if (data.getState().isAir()) {
                    this.syncToClientSlot();
                    this.logDebug("queue-remove pos=" + data.getPos().toShortString() + " reason=secondary_air attempted=" + data.hasAttemptedBreak());
                    iterator.remove();
                    continue;
                }
                if (data.hasAttemptedBreak() && data.passedAttemptedBreakTime(this.getSecondaryRetryDelayMs(data))) {
                    data.clearAttemptedBreak();
                    data.markSecondaryRetryWindow();
                    this.logDebug("secondary-retry pos=" + data.getPos().toShortString() + " damage=" + this.fmt(data.getBlockDamage()) + " delay=" + this.getSecondaryRetryDelayMs(data) + " retries=" + data.getSecondaryRetries());
                }
            }
            float damageDelta = Managers.BLOCKS.calcBlockBreakingDelta(data.getState(), data.getPos());
            data.damage(damageDelta);
            if (!this.isDataPacketMine(data) || !(data.getBlockDamage() >= 1.0f) || data.getSlot() == -1) continue;
            if (R0SE.mc.player.isUsingItem() && !this.isMultiTaskAllowed()) {
                return;
            }
            if (data.getSlot() != Managers.INVENTORY.getServerSlot()) {
                this.swapTo(data.getSlot());
            }
            if (data.hasAttemptedBreak()) continue;
            this.stopMining(data, data.hasSecondaryRetries());
            data.markBreakAttempt();
            this.logDebug("secondary-ready pos=" + data.getPos().toShortString() + " damage=" + this.fmt(data.getBlockDamage()) + " slot=" + data.getSlot());
        }
        MiningData primary = this.miningQueue.peek();
        if (primary == null) {
            return;
        }
        if (!this.isWithinRange(primary.getPos())) {
            this.logDebug("primary-remove pos=" + primary.getPos().toShortString() + " reason=range");
            this.miningQueue.remove(primary);
            return;
        }
        if (primary.getState().isAir()) {
            return;
        }
        if (this.rebreak.getValue() == RebreakMode.INSTANT && primary.hasAttemptedBreak()) {
            if (primary.passedAttemptedBreakTime(this.getInstantAttemptTimeoutMs(primary))) {
                primary.markQuietFailure();
                this.logDebug("instant-timeout pos=" + primary.getPos().toShortString() + " elapsed=" + (System.currentTimeMillis() - primary.getBreakTime()) + " attempts=" + primary.getInstantAttempts() + " solidGen=" + primary.getSolidGeneration() + " requestGen=" + primary.getBreakRequestGeneration());
            } else {
                return;
            }
        }
        if (this.rebreak.getValue() != RebreakMode.INSTANT && primary.getBlockDamage() >= this.getPrimaryStopDamage(primary) && primary.hasAttemptedBreak() && primary.passedAttemptedBreakTime(500L)) {
            this.abortMining(primary);
            this.miningQueue.remove(primary);
            this.logDebug("primary-remove pos=" + primary.getPos().toShortString() + " reason=attempt_timeout");
            return;
        }
        if (primary.hasAttemptedBreak()) {
            return;
        }
        if (primary.getBlockDamage() >= this.getPrimaryStopDamage(primary)) {
            if (R0SE.mc.player.isUsingItem() && !this.isMultiTaskAllowed()) {
                return;
            }
            if (this.rebreak.getValue() == RebreakMode.INSTANT && !this.canInstantRebreak()) {
                return;
            }
            boolean instantRebreak = this.shouldPrimeInstantRebreak(primary);
            if (this.rebreak.getValue() == RebreakMode.INSTANT && primary.isInstantMine() && !instantRebreak) {
                this.logDebug("instant-wait pos=" + primary.getPos().toShortString() + " damage=" + this.fmt(primary.getBlockDamage()) + " solidGen=" + primary.getSolidGeneration() + " requestGen=" + primary.getBreakRequestGeneration() + " sinceRetry=" + (System.currentTimeMillis() - primary.getLastInstantRetryAt()));
                return;
            }
            if (instantRebreak) {
                primary.markInstantRebreakPrimed();
                this.logDebug("instant-rebreak-stop-only pos=" + primary.getPos().toShortString() + " solidGen=" + primary.getSolidGeneration() + " requestGen=" + primary.getBreakRequestGeneration() + " attempts=" + primary.getInstantAttempts());
            }
            this.stopMining(primary, primary.isInstantMine() && !instantRebreak);
            if (this.rebreak.getValue() == RebreakMode.INSTANT) {
                this.lastInstantAt = System.currentTimeMillis();
                primary.markInstantMine();
            }
            if (this.rebreak.getValue() != RebreakMode.INSTANT) {
                this.miningQueue.remove(primary);
            }
            if (!primary.hasAttemptedBreak()) {
                primary.markBreakAttempt();
            }
        }
    }

    @Subscribe
    public void onAttackBlock(AttackBlockEvent event) {
        if (R0SE.mc.player == null || R0SE.mc.world == null || R0SE.mc.player.isCreative() || R0SE.mc.player.isSpectator()) {
            return;
        }
        event.cancel();
        BlockState state = R0SE.mc.world.getBlockState(event.getPos());
        if (state.isAir() || state.getHardness((BlockView)R0SE.mc.world, event.getPos()) == -1.0f || !this.isWithinRange(event.getPos())) {
            return;
        }
        this.startManualMine(event.getPos(), event.getDirection());
        R0SE.mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Subscribe
    public void onRenderWorld(RenderWorldEvent event) {
        if (!((Boolean)this.render.getValue()).booleanValue() || R0SE.mc.world == null) {
            return;
        }
        this.syncDoubleMineQueue();
        for (Map.Entry<MiningData, Animation> entry2 : this.fadeList.entrySet()) {
            MiningData data = entry2.getKey();
            entry2.getValue().setState(false);
            this.renderData(event, data, entry2.getValue(), true);
        }
        for (MiningData data : this.miningQueue) {
            if (data.getState().isAir() && !this.shouldRenderInstantReservation(data)) continue;
            this.fadeList.putIfAbsent(data, new Animation(true, (float)((Double)this.fadeTime.getValue() * 1000.0), Easing.SMOOTH_STEP));
            this.renderData(event, data, this.fadeList.get(data), false);
        }
        this.fadeList.entrySet().removeIf(entry -> ((Animation)entry.getValue()).getFactor() == 0.0 && !this.miningQueue.contains((MiningData)entry.getKey()));
    }

    public void onServerAirConfirmed(BlockPos pos) {
        if (pos == null) {
            return;
        }
        for (MiningData data : this.miningQueue) {
            if (!data.hasAttemptedBreak() || !data.getPos().equals((Object)pos)) continue;
            data.clearAttemptedBreak();
            data.setServerAirConfirmedAt(System.currentTimeMillis());
            data.clearInstantRebreakPrimed();
            data.resetBlockDamage();
            this.logDebug("server-air pos=" + pos.toShortString() + " primary=" + (data == this.miningQueue.peek()) + " secondary=" + this.isDataPacketMine(data));
        }
    }

    public void onServerSolidConfirmed(BlockPos pos) {
        if (pos == null) {
            return;
        }
        for (MiningData data : this.miningQueue) {
            if (!data.getPos().equals((Object)pos)) continue;
            data.setServerSolidConfirmedAt(System.currentTimeMillis());
            data.observeServerSolid();
            data.forceReady();
            this.logDebug("server-solid pos=" + pos.toShortString() + " primary=" + (data == this.miningQueue.peek()) + " secondary=" + this.isDataPacketMine(data) + " instant=" + data.isInstantMine() + " solidGen=" + data.getSolidGeneration());
        }
    }

    public void onPredictedPlacement(BlockPos pos) {
        if (pos == null || R0SE.mc.player == null || R0SE.mc.world == null || this.rebreak.getValue() != RebreakMode.INSTANT) {
            return;
        }
        MiningData primary = this.miningQueue.peek();
        if (!(primary != null && primary.getPos().equals((Object)pos) && primary.isStarted() && primary.isInstantMine())) {
            return;
        }
        boolean asyncBurst = (Boolean)this.async.getValue();
        if (!asyncBurst && primary.hasAttemptedBreak()) {
            return;
        }
        primary.markInstantRebreakPrimed();
        primary.markPredictedInstantAttempt();
        int burst = asyncBurst ? 4 : 1;
        this.logDebug("instant-predicted-place-stop pos=" + pos.toShortString() + " solidGen=" + primary.getSolidGeneration() + " requestGen=" + primary.getBreakRequestGeneration() + " attempts=" + primary.getInstantAttempts() + " async=" + asyncBurst + " burst=" + burst + " attempted=" + primary.hasAttemptedBreak());
        this.stopMining(primary, false, true, burst);
        this.lastInstantAt = System.currentTimeMillis();
        primary.markInstantMine();
        if (!primary.hasAttemptedBreak()) {
            primary.markBreakAttempt();
        }
    }

    private void startManualMine(BlockPos pos, Direction direction) {
        this.syncDoubleMineQueue();
        this.clickMine(new MiningData(pos.toImmutable(), direction));
    }

    private void syncDoubleMineQueue() {
        int targetSize;
        int n = targetSize = (Boolean)this.doubleMine.getValue() != false ? 2 : 1;
        if (this.miningQueue.maxSize() == targetSize) {
            return;
        }
        FirstOutQueue<MiningData> updated = new FirstOutQueue<MiningData>(targetSize);
        int added = 0;
        for (MiningData data2 : this.miningQueue) {
            if (added >= targetSize) break;
            updated.add(data2);
            ++added;
        }
        this.miningQueue = updated;
        this.fadeList.keySet().removeIf(data -> !this.miningQueue.contains((MiningData)data));
        this.logDebug("queue-resize target=" + targetSize + " size=" + this.miningQueue.size());
    }

    private void clickMine(MiningData data) {
        if (this.miningQueue.size() <= 2) {
            this.queueMiningData(data);
        }
    }

    private void queueMiningData(MiningData data) {
        if (data.getState().isAir()) {
            return;
        }
        for (MiningData current : this.miningQueue) {
            if (!current.getPos().equals((Object)data.getPos())) continue;
            this.logDebug("queue-skip pos=" + data.getPos().toShortString() + " reason=duplicate");
            return;
        }
        if (this.startMining(data)) {
            MiningData removed = this.miningQueue.addFirst(data);
            if (removed != null) {
                this.logDebug("queue-evict pos=" + removed.getPos().toShortString() + " reason=replace_primary_keep_secondary");
            }
            this.logDebug("queue-add pos=" + data.getPos().toShortString() + " dir=" + String.valueOf(data.getDirection()) + " mode=" + String.valueOf((Object)this.getMiningMode()) + " size=" + this.miningQueue.size() + " primary=" + this.describeData(this.miningQueue.peek()) + " secondary=" + this.describeData(this.miningQueue.size() == 2 ? this.miningQueue.getLast() : null));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean startMining(MiningData data) {
        if (data.isStarted()) {
            return false;
        }
        data.setStarted();
        data.setDirection(this.getMiningDirection(data));
        AntiCheat.MiningMode mode = this.getMiningMode();
        String source = "speedmine-start";
        ActionSessionManager.ActionSession session = Managers.ACTIONS.begin("mine-start", "SpeedMine", data.getPos(), data.getDirection());
        try {
            if (mode == AntiCheat.MiningMode.GRIM_STRICT) {
                if (this.isMineSyncEnabled()) {
                    this.sendDig(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-sync-start");
                    this.sendDig(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-sync-stop");
                } else {
                    this.sendDig(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-strict-prefinish");
                    this.sendDig(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-strict-restart");
                    this.sendDig(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-strict-stop");
                }
                this.sendSwing(source + "-strict-swing-1");
                this.sendSwing(source + "-strict-swing-2");
                this.sendSwing(source + "-strict-swing-3");
            } else if (mode == AntiCheat.MiningMode.GRIM) {
                this.sendLegacyStart(data, source + "-legacy-1");
                this.sendLegacyStart(data, source + "-legacy-2");
            } else {
                this.sendDig(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-vanilla");
            }
        }
        finally {
            Managers.ACTIONS.end(session, "started");
        }
        return true;
    }

    private void sendLegacyStart(MiningData data, String source) {
        this.sendDig(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-start");
        this.sendDig(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), source + "-stop");
        this.sendSwing(source + "-swing");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void abortMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir()) {
            return;
        }
        Direction direction = this.getMiningDirection(data);
        data.setDirection(direction);
        ActionSessionManager.ActionSession session = Managers.ACTIONS.begin("mine-abort", "SpeedMine", data.getPos(), direction);
        try {
            this.sendDig(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), direction, "speedmine-abort");
            this.syncToClientSlot();
        }
        finally {
            Managers.ACTIONS.end(session, "aborted");
        }
    }

    private void stopMining(MiningData data) {
        this.stopMining(data, false);
    }

    private void stopMining(MiningData data, boolean retry) {
        this.stopMining(data, retry, false);
    }

    private void stopMining(MiningData data, boolean retry, boolean allowAirState) {
        this.stopMining(data, retry, allowAirState, 1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void stopMining(MiningData data, boolean retry, boolean allowAirState, int stopCount) {
        if (!data.isStarted() || !allowAirState && data.getState().isAir()) {
            return;
        }
        Direction direction = this.getMiningDirection(data);
        data.setDirection(direction);
        ActionSessionManager.ActionSession session = Managers.ACTIONS.begin("mine-stop", "SpeedMine", data.getPos(), direction);
        try {
            int slot;
            boolean canSwap;
            boolean quietRetry = retry;
            if (((Boolean)this.rotate.getValue()).booleanValue() && !quietRetry) {
                Rotation rotation = this.getRotation(data.getPos(), direction);
                if (this.getMiningMode() == AntiCheat.MiningMode.VANILLA) {
                    Managers.ROTATION.setRotation(rotation);
                } else {
                    Managers.ROTATION.setRotationSilent(rotation);
                }
            }
            boolean bl = canSwap = (slot = data.getSlot()) != -1 && slot != Managers.INVENTORY.getServerSlot();
            if (canSwap) {
                this.swapTo(slot);
            }
            boolean instantReservationStop = this.rebreak.getValue() == RebreakMode.INSTANT && data.isInstantMine() && data.isInstantRebreakPrimed();
            int stops = MathHelper.clamp((int)stopCount, (int)1, (int)4);
            for (int i = 0; i < stops; ++i) {
                String suffix = stops == 1 ? "" : "-" + (i + 1);
                this.sendDig(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), direction, "speedmine-stop" + suffix);
                if (!instantReservationStop) continue;
                this.sendSwing("speedmine-instant-swing" + suffix);
            }
            if (this.shouldAbortAfterStop()) {
                this.sendDig(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), direction, "speedmine-stop-abort");
            }
            this.lastBreak = System.currentTimeMillis();
            if (canSwap && !quietRetry) {
                this.syncToClientSlot();
            }
            if (((Boolean)this.rotate.getValue()).booleanValue() && this.getMiningMode() != AntiCheat.MiningMode.VANILLA && !quietRetry) {
                Managers.ROTATION.setRotationSilentSync();
            }
            this.logDebug("stop pos=" + data.getPos().toShortString() + " dir=" + String.valueOf(direction) + " damage=" + this.fmt(data.getBlockDamage()) + " slot=" + slot + " retry=" + retry + " quiet=" + quietRetry + " stops=" + stops + " queue=" + this.miningQueue.size());
        }
        finally {
            Managers.ACTIONS.end(session, "stopped");
        }
    }

    private void swapTo(int slot) {
        if (slot < 0 || slot > 8 || this.autoSwap.getValue() == AutoSwapMode.NONE) {
            return;
        }
        if (this.autoSwap.getValue() == AutoSwapMode.NORMAL) {
            Managers.INVENTORY.setSelectedSlot(slot);
        } else {
            Managers.INVENTORY.setSlotSilent(slot);
        }
        this.logDebug("swap-to slot=" + slot + " mode=" + String.valueOf(this.autoSwap.getValue()) + " server=" + Managers.INVENTORY.getServerSlot());
    }

    private void syncToClientSlot() {
        if (this.autoSwap.getValue() == AutoSwapMode.SILENT) {
            Managers.INVENTORY.syncSelectedSlot();
            this.logDebug("swap-sync server=" + Managers.INVENTORY.getServerSlot());
        }
    }

    private boolean canInstantRebreak() {
        long delayMs = (long)((Double)this.instantDelay.getValue() * 1000.0);
        return delayMs <= 0L || System.currentTimeMillis() - this.lastInstantAt >= delayMs;
    }

    private long getInstantAttemptTimeoutMs(MiningData data) {
        long base = Math.max(250L, (long)((Double)this.instantDelay.getValue() * 1000.0));
        return Math.min(1200L, Math.max(650L, base + (long)Math.max(0, data.getInstantAttempts() - 1) * 125L));
    }

    private long getSecondaryRetryDelayMs(MiningData data) {
        long base = this.getMiningMode() == AntiCheat.MiningMode.VANILLA ? 500L : 650L;
        return Math.min(1800L, base + (long)data.getSecondaryRetries() * 250L);
    }

    private boolean shouldRenderInstantReservation(MiningData data) {
        return this.rebreak.getValue() == RebreakMode.INSTANT && data == this.miningQueue.peek();
    }

    private boolean shouldPrimeInstantRebreak(MiningData data) {
        return this.rebreak.getValue() == RebreakMode.INSTANT && data.isInstantMine() && !data.getState().isAir() && !data.isInstantRebreakPrimed() && (data.hasFreshSolidGeneration() || data.passedInstantRetryDelay(this.getInstantAttemptTimeoutMs(data)));
    }

    private boolean isDataPacketMine(MiningData data) {
        return (Boolean)this.doubleMine.getValue() != false && this.miningQueue.size() == 2 && data == this.miningQueue.getLast();
    }

    private float getPrimaryStopDamage(MiningData data) {
        return (float)((Double)this.damage.getValue()).doubleValue();
    }

    private int getBestToolNoFallback(BlockState state) {
        int slot = -1;
        float bestTool = 0.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = R0SE.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ToolItem) && !(stack.getItem() instanceof SwordItem)) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            int efficiency = this.getEfficiencyLevel(stack);
            if (efficiency > 0) {
                speed += (float)(efficiency * efficiency) + 1.0f;
            }
            if (!(speed > bestTool)) continue;
            bestTool = speed;
            slot = i;
        }
        return slot;
    }

    private int getEfficiencyLevel(ItemStack stack) {
        for (Object2IntMap.Entry entry : EnchantmentHelper.getEnchantments((ItemStack)stack).getEnchantmentEntries()) {
            if (!((RegistryEntry)entry.getKey()).getKey().isPresent() || ((RegistryEntry)entry.getKey()).getKey().get() != Enchantments.EFFICIENCY) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    private void sendDig(PlayerActionC2SPacket.Action action, BlockPos pos, Direction direction, String source) {
        this.sendPacket((Packet<?>)new PlayerActionC2SPacket(action, pos, direction), source);
        this.logDebug("dig action=" + String.valueOf(action) + " source=" + source + " pos=" + pos.toShortString() + " dir=" + String.valueOf(direction) + " mode=" + String.valueOf((Object)this.getMiningMode()));
    }

    private void sendSwing(String source) {
        this.sendPacket((Packet<?>)new HandSwingC2SPacket(Hand.MAIN_HAND), source);
        this.logDebug("swing source=" + source);
    }

    private void sendPacket(Packet<?> packet, String source) {
        Managers.NETWORK.sendPacket(packet, Managers.ACTIONS.currentLabel() + "/" + source);
    }

    private Direction getMiningDirection(MiningData data) {
        Direction fallback = data.getDirection();
        if (Managers.MINING.getMineFailReason(data.getPos(), fallback) == MiningFailReason.NONE) {
            return fallback;
        }
        Direction best = fallback;
        double bestDistance = Double.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            double distance;
            if (Managers.MINING.getMineFailReason(data.getPos(), direction) != MiningFailReason.NONE) continue;
            Vec3d hit = data.getPos().toCenterPos().add(Vec3d.of((Vec3i)direction.getVector()).multiply(0.5));
            double d = distance = R0SE.mc.player == null ? 0.0 : R0SE.mc.player.getEyePos().squaredDistanceTo(hit);
            if (!(distance < bestDistance)) continue;
            best = direction;
            bestDistance = distance;
        }
        if (best != fallback) {
            this.logDebug("direction-refresh pos=" + data.getPos().toShortString() + " old=" + String.valueOf(fallback) + " new=" + String.valueOf(best) + " fail=" + String.valueOf((Object)Managers.MINING.getMineFailReason(data.getPos(), fallback)));
        }
        return best;
    }

    private Rotation getRotation(BlockPos pos, Direction direction) {
        Vec3d hit = pos.toCenterPos().add(Vec3d.of((Vec3i)direction.getVector()).multiply(0.5));
        Vec3d eyes = R0SE.mc.player.getEyePos();
        Vec3d delta = hit.subtract(eyes);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, horizontal)));
        return new Rotation(yaw, pitch);
    }

    private void renderData(RenderWorldEvent event, MiningData data, Animation animation, boolean fading) {
        if (data == null || R0SE.mc.world == null) {
            return;
        }
        BlockState state = data.getState();
        VoxelShape shape = state.getOutlineShape((BlockView)R0SE.mc.world, data.getPos());
        shape = shape.isEmpty() ? VoxelShapes.fullCube() : shape;
        Box raw = shape.getBoundingBox();
        Box box = new Box((double)data.getPos().getX() + raw.minX, (double)data.getPos().getY() + raw.minY, (double)data.getPos().getZ() + raw.minZ, (double)data.getPos().getX() + raw.maxX, (double)data.getPos().getY() + raw.maxY, (double)data.getPos().getZ() + raw.maxZ);
        Vec3d center = box.getCenter();
        float target = this.isDataPacketMine(data) ? 1.0f : this.getPrimaryStopDamage(data);
        float interpolated = data.getBlockDamage() + (data.getBlockDamage() - data.getLastDamage()) * event.getTickDelta();
        boolean reserved = this.shouldRenderInstantReservation(data) && state.isAir();
        float progress = state.isAir() ? 1.0f : MathHelper.clamp((float)(interpolated / Math.max(0.001f, target)), (float)0.0f, (float)1.0f);
        float eased = (float)Easing.SMOOTH_STEP.ease(progress);
        double dx = (raw.maxX - raw.minX) / 2.0;
        double dy = (raw.maxY - raw.minY) / 2.0;
        double dz = (raw.maxZ - raw.minZ) / 2.0;
        Box scaled = new Box(center, center).expand(dx * (double)eased, dy * (double)eased, dz * (double)eased);
        animation.setLength((float)((Double)this.fadeTime.getValue() * 1000.0));
        if (!fading) {
            animation.setState(true);
        }
        float alpha = fading && (Boolean)this.fade.getValue() != false ? (float)animation.getFactor() : 1.0f;
        float progressAlpha = reserved ? 0.75f : 0.55f + 0.45f * progress;
        Color fill = ColorUtil.scaleAlpha(Managers.COLORS.resolve(this.fillColor), alpha * progressAlpha);
        Color outline = ColorUtil.scaleAlpha(Managers.COLORS.resolve(this.outlineColor), alpha * progressAlpha);
        BoxRenderMode mode = (BoxRenderMode)((Object)this.renderMode.getValue());
        if (mode.isFill()) {
            Managers.RENDER.renderBox(event.getMatrices(), scaled, fill, true, false, null, ((Double)this.lineWidth.getValue()).floatValue());
        }
        if (mode.isOutline()) {
            Managers.RENDER.renderBox(event.getMatrices(), scaled, outline, false, true, null, ((Double)this.lineWidth.getValue()).floatValue());
        }
    }

    private boolean isWithinRange(BlockPos pos) {
        return R0SE.mc.player != null && R0SE.mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos()) <= (Double)this.range.getValue() * (Double)this.range.getValue();
    }

    private boolean isMultiTaskAllowed() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config != null && (Boolean)config.getMultiTask().getValue() != false;
    }

    private boolean isMineSyncEnabled() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config != null && (Boolean)config.getMineSync().getValue() != false && config.getMining().getValue() != AntiCheat.MiningMode.VANILLA;
    }

    private boolean shouldAbortAfterStop() {
        AntiCheat.MiningMode mode = this.getMiningMode();
        return mode == AntiCheat.MiningMode.VANILLA && this.rebreak.getValue() != RebreakMode.INSTANT;
    }

    private AntiCheat.MiningMode getMiningMode() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config == null ? AntiCheat.MiningMode.VANILLA : (AntiCheat.MiningMode)((Object)config.getMining().getValue());
    }

    private String fmt(float value) {
        return String.format("%.2f", Float.valueOf(value));
    }

    private void logDebug(String message) {
        if (((Boolean)this.debug.getValue()).booleanValue()) {
            Managers.DEBUG.log("SpeedMineDebug", message);
        }
    }

    private String describeData(MiningData data) {
        if (data == null) {
            return "none";
        }
        return data.getPos().toShortString() + "/d=" + this.fmt(data.getBlockDamage()) + "/attempt=" + data.hasAttemptedBreak();
    }

    public static enum RebreakMode {
        NONE,
        FAST,
        INSTANT;

    }

    public static enum AutoSwapMode {
        NONE,
        NORMAL,
        SILENT;

    }

    private static final class MiningData {
        private final BlockPos pos;
        private Direction direction;
        private boolean attemptedBreak;
        private long breakTime;
        private long serverAirConfirmedAt;
        private long serverSolidConfirmedAt;
        private long instantStartedAt;
        private long lastInstantRetryAt;
        private long lastSecondaryRetryAt;
        private int solidGeneration;
        private int breakRequestGeneration = -1;
        private boolean instantRebreakPrimed;
        private boolean predictedInstantAttempt;
        private boolean lastAir;
        private int instantAttempts;
        private int secondaryRetries;
        private float lastDamage;
        private float blockDamage;
        private boolean started;
        private BlockState lastNonAirState;

        private MiningData(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
            BlockState state = this.getState();
            this.lastAir = state.isAir();
            if (!this.lastAir) {
                this.lastNonAirState = state;
            }
        }

        private void markBreakAttempt() {
            this.attemptedBreak = true;
            this.breakRequestGeneration = this.solidGeneration;
            this.resetBreakTime();
        }

        private void clearAttemptedBreak() {
            this.attemptedBreak = false;
            this.predictedInstantAttempt = false;
        }

        private void resetBreakTime() {
            this.breakTime = System.currentTimeMillis();
        }

        private boolean hasAttemptedBreak() {
            return this.attemptedBreak;
        }

        private boolean passedAttemptedBreakTime(long time) {
            return System.currentTimeMillis() - this.breakTime >= time;
        }

        private long getBreakTime() {
            return this.breakTime;
        }

        private void markInstantMine() {
            if (this.instantStartedAt == 0L) {
                this.instantStartedAt = System.currentTimeMillis();
            }
            ++this.instantAttempts;
            this.lastInstantRetryAt = System.currentTimeMillis();
            this.breakRequestGeneration = this.solidGeneration;
        }

        private boolean isInstantMine() {
            return this.instantStartedAt > 0L;
        }

        private int getInstantAttempts() {
            return this.instantAttempts;
        }

        private void markQuietFailure() {
            this.attemptedBreak = false;
            this.instantRebreakPrimed = false;
            this.predictedInstantAttempt = false;
            this.lastInstantRetryAt = System.currentTimeMillis();
        }

        private boolean passedInstantRetryDelay(long delayMs) {
            return this.lastInstantRetryAt > 0L && System.currentTimeMillis() - this.lastInstantRetryAt >= delayMs;
        }

        private long getLastInstantRetryAt() {
            return this.lastInstantRetryAt;
        }

        private void markSecondaryRetryWindow() {
            this.lastSecondaryRetryAt = System.currentTimeMillis();
            ++this.secondaryRetries;
        }

        private boolean hasSecondaryRetries() {
            return this.secondaryRetries > 0 || this.lastSecondaryRetryAt > 0L;
        }

        private int getSecondaryRetries() {
            return this.secondaryRetries;
        }

        private boolean hasFreshSolidGeneration() {
            return this.solidGeneration > this.breakRequestGeneration;
        }

        private boolean isInstantRebreakPrimed() {
            return this.instantRebreakPrimed;
        }

        private void markInstantRebreakPrimed() {
            this.instantRebreakPrimed = true;
        }

        private void markPredictedInstantAttempt() {
            this.predictedInstantAttempt = true;
        }

        private void clearInstantRebreakPrimed() {
            this.instantRebreakPrimed = false;
        }

        private void damage(float amount) {
            this.lastDamage = this.blockDamage;
            this.blockDamage += amount;
        }

        private void resetBlockDamage() {
            this.lastDamage = 0.0f;
            this.blockDamage = 0.0f;
        }

        private void forceReady() {
            this.lastDamage = this.blockDamage;
            this.blockDamage = Math.max(this.blockDamage, 1.0f);
        }

        private void updateObservedState() {
            boolean air = this.getState().isAir();
            if (!air) {
                this.lastNonAirState = this.getState();
            }
            if (this.lastAir && !air) {
                this.observeSolid();
            }
            this.lastAir = air;
        }

        private void observeServerSolid() {
            BlockState state = this.getState();
            if (!state.isAir()) {
                this.lastNonAirState = state;
            }
            if (this.lastAir) {
                this.observeSolid();
            } else {
                this.forceReady();
            }
            this.lastAir = false;
        }

        private void observeSolid() {
            ++this.solidGeneration;
            if (!this.predictedInstantAttempt || !this.attemptedBreak) {
                this.clearAttemptedBreak();
                this.clearInstantRebreakPrimed();
            }
            this.forceReady();
        }

        private int getSolidGeneration() {
            return this.solidGeneration;
        }

        private int getBreakRequestGeneration() {
            return this.breakRequestGeneration;
        }

        private void setStarted() {
            this.started = true;
        }

        private boolean isStarted() {
            return this.started;
        }

        private BlockPos getPos() {
            return this.pos;
        }

        private Direction getDirection() {
            return this.direction;
        }

        private void setDirection(Direction direction) {
            if (direction != null) {
                this.direction = direction;
            }
        }

        private BlockState getState() {
            return R0SE.mc.world.getBlockState(this.pos);
        }

        private int getSlot() {
            SpeedMine feature = Managers.MODULES.getFeature(SpeedMine.class);
            return feature == null ? -1 : feature.getBestToolNoFallback(this.getToolState());
        }

        private BlockState getToolState() {
            BlockState state = this.getState();
            return state.isAir() && this.lastNonAirState != null ? this.lastNonAirState : state;
        }

        private float getBlockDamage() {
            return this.blockDamage;
        }

        private float getLastDamage() {
            return this.lastDamage;
        }

        private void setServerAirConfirmedAt(long serverAirConfirmedAt) {
            this.serverAirConfirmedAt = serverAirConfirmedAt;
            this.lastAir = true;
        }

        private void setServerSolidConfirmedAt(long serverSolidConfirmedAt) {
            this.serverSolidConfirmedAt = serverSolidConfirmedAt;
        }
    }
}



