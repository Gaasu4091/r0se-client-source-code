/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Hand
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.BlockPos
 */
package r0se.impl.module.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.render.RenderWorldEvent;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.render.ColorUtil;
import r0se.api.render.Easing;
import r0se.api.render.state.BlockRenderState;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.Setting;
import r0se.impl.module.client.AntiCheat;
import r0se.manager.Managers;
import r0se.manager.api.PlacementPlan;
import r0se.util.world.ProtectionUtil;

public class FeetPlace
extends ToggleableFeature {
    private final DoubleSetting delay = this.addSetting(new DoubleSetting("Delay", 0.0, 0.0, 1000.0).precision(1).suffix("ms"));
    private final DoubleSetting blocksPerTick = this.addSetting(new DoubleSetting("BPT", 3.0, 1.0, 8.0).precision(1));
    private final BoolSetting airPlace = this.addSetting(new BoolSetting("AirPlace", false));
    private final BoolSetting jumpDisable = this.addSetting(new BoolSetting("JumpDisable", true));
    private final BoolSetting render = this.addSetting((BoolSetting)new BoolSetting("Enabled", true).hide());
    private final GroupSetting renderGroup = this.addSetting(new GroupSetting("Render", false).linkToggle(this.render));
    private final ColorSetting fillColor = this.addSetting((ColorSetting)((Setting)new ColorSetting("Fill", new Color(64, 54, 128, 55)).enableSync(ColorSyncMode.SECONDARY).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final ColorSetting outlineColor = this.addSetting((ColorSetting)((Setting)new ColorSetting("Outline", new Color(178, 154, 255, 185)).enableSync(ColorSyncMode.PRIMARY).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final DoubleSetting lineWidth = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("LineWidth", 1.0, 0.1, 4.0).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final BoolSetting fade = this.addSetting((BoolSetting)((Setting)new BoolSetting("Fade", true).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final DoubleSetting fadeTime = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("FadeTime", 1.0, 0.1, 2.0).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final BoolSetting debug = this.addSetting(new BoolSetting("Debug", false));
    private final Map<BlockPos, Long> placed = new LinkedHashMap<BlockPos, Long>();
    private final Map<BlockPos, BlockRenderState> renderStates = new LinkedHashMap<BlockPos, BlockRenderState>();
    private double prevY;
    private long lastDebugAt;
    private long tickCounter;
    private long lastPlaceAt;

    public FeetPlace() {
        super("FeetPlace", "Places obsidian around your feet", FeatureCategory.COMBAT, "feettrap", "surround");
        this.getNotify().setValue(true);
    }

    @Override
    protected void onEnable() {
        if (R0SE.mc.player != null) {
            this.prevY = R0SE.mc.player.getY();
        }
        this.placed.clear();
        this.renderStates.clear();
        this.tickCounter = 0L;
        this.lastPlaceAt = 0L;
        this.logDebug("enabled");
    }

    @Override
    protected void onDisable() {
        this.placed.clear();
        this.renderStates.clear();
        this.logDebug("disabled");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onTick(TickEvent event) {
        ++this.tickCounter;
        if (R0SE.mc.player == null || R0SE.mc.world == null) {
            this.renderStates.clear();
            return;
        }
        if (((Boolean)this.jumpDisable.getValue()).booleanValue() && !R0SE.mc.player.isOnGround()) {
            this.logDebug("jumpDisable triggered at y=" + String.format("%.2f", R0SE.mc.player.getY()));
            this.disable();
            return;
        }
        if (Math.abs(R0SE.mc.player.getY() - this.prevY) > 0.5) {
            this.prevY = R0SE.mc.player.getY();
            this.renderStates.clear();
        }
        this.cleanupPlaced(this.nowOrCurrentMillis());
        int obbySlot = Managers.INVENTORY.getItemSlot(Items.OBSIDIAN);
        if (obbySlot == -1) {
            this.hideAllRenderStates();
            this.logDebug("no obsidian found");
            return;
        }
        long now = System.currentTimeMillis();
        if ((Double)this.delay.getValue() > 0.0 && now - this.lastPlaceAt < (long)((Double)this.delay.getValue()).doubleValue()) {
            this.cleanupRenderStates();
            return;
        }
        List<BlockPos> targets = this.collectTargets(this.nowOrCurrentMillis());
        this.updateRenderStates(this.getRenderPositionsForTargets(targets));
        if (targets.isEmpty()) {
            this.logDebug("no place targets");
            this.cleanupRenderStates();
            return;
        }
        this.logDebug("targets=" + targets.size() + " slot=" + obbySlot + " rotationMode=" + String.valueOf((Object)this.getRotationMode()));
        if (!Managers.INTERACT.startPlacement(obbySlot)) {
            this.logDebug("startPlacement failed slot=" + obbySlot);
            this.cleanupRenderStates();
            return;
        }
        try {
            int attempts = 0;
            int placedBlocks = 0;
            LinkedHashSet<BlockPos> placedThisTick = new LinkedHashSet<BlockPos>();
            int limit = Math.max(1, (int)Math.round((Double)this.blocksPerTick.getValue()));
            int maxAttempts = Math.max(limit * 3, limit + 2);
            for (BlockPos target : targets) {
                PlacementAttempt attempt;
                boolean success;
                if (placedBlocks >= limit || attempts >= maxAttempts) break;
                if (((Boolean)this.airPlace.getValue()).booleanValue()) {
                    success = Managers.INTERACT.placeBlock(target, Hand.MAIN_HAND, placedThisTick, true);
                    attempt = new PlacementAttempt(1, success ? 1 : 0);
                    if (success) {
                        placedThisTick.add(target);
                    }
                } else {
                    attempt = this.placeWithSupport(target, placedThisTick);
                }
                success = attempt.placedBlocks() > 0;
                this.logDebug("place " + target.toShortString() + " success=" + success + " placedBlocks=" + attempt.placedBlocks() + " totalPlaced=" + (placedBlocks + attempt.placedBlocks()) + "/" + limit + " attempts=" + (attempts += Math.max(1, attempt.attempts())) + "/" + maxAttempts + " air=" + String.valueOf(this.airPlace.getValue()));
                if (success) {
                    placedBlocks += attempt.placedBlocks();
                    this.placed.put(target, now);
                    continue;
                }
                if (!Managers.INTERACT.isPlacementRotationWaiting()) continue;
                this.logDebug("place wait target=" + target.toShortString() + " reason=rotation_wait");
                break;
            }
            if (placedBlocks > 0) {
                this.lastPlaceAt = now;
            }
        }
        finally {
            Managers.INTERACT.endPlacement();
        }
        this.cleanupRenderStates();
    }

    private PlacementAttempt placeWithSupport(BlockPos target, LinkedHashSet<BlockPos> placedThisTick) {
        LinkedHashSet<BlockPos> planned = new LinkedHashSet<BlockPos>(this.placed.keySet());
        planned.addAll(placedThisTick);
        PlacementPlan plan = Managers.INTERACT.planPlacement(target, planned, false);
        if (!plan.isValid()) {
            this.logDebug("plan failed target=" + target.toShortString() + " reason=" + String.valueOf((Object)plan.getFailReason()));
            return new PlacementAttempt(1, 0);
        }
        if (!plan.hasSupport()) {
            boolean placedTarget = Managers.INTERACT.placeBlock(target, Hand.MAIN_HAND, placedThisTick, false);
            if (placedTarget) {
                placedThisTick.add(target);
            }
            this.logDebug("target " + target.toShortString() + " direct placed=" + placedTarget);
            return new PlacementAttempt(1, placedTarget ? 1 : 0);
        }
        BlockPos supportPos = plan.getSupportPos();
        boolean placedSupport = Managers.INTERACT.placeBlock(supportPos, Hand.MAIN_HAND, placedThisTick, false);
        this.logDebug("support " + supportPos.toShortString() + " placed=" + placedSupport + " target=" + target.toShortString());
        if (!placedSupport) {
            return new PlacementAttempt(1, 0);
        }
        placedThisTick.add(supportPos);
        this.placed.put(supportPos, System.currentTimeMillis());
        LinkedHashSet<BlockPos> retryPlanned = new LinkedHashSet<BlockPos>(this.placed.keySet());
        retryPlanned.addAll(placedThisTick);
        PlacementPlan retryPlan = Managers.INTERACT.planPlacement(target, retryPlanned, false);
        if (!retryPlan.isValid() || retryPlan.hasSupport()) {
            this.logDebug("target retry plan failed target=" + target.toShortString() + " reason=" + String.valueOf((Object)retryPlan.getFailReason()));
            return new PlacementAttempt(2, 1);
        }
        boolean placedTarget = Managers.INTERACT.placeBlock(target, Hand.MAIN_HAND, placedThisTick, false);
        this.logDebug("target retry " + target.toShortString() + " placed=" + placedTarget);
        if (placedTarget) {
            placedThisTick.add(target);
        }
        return new PlacementAttempt(2, placedTarget ? 2 : 1);
    }

    @Subscribe
    public void onRenderWorld(RenderWorldEvent event) {
        if (!((Boolean)this.render.getValue()).booleanValue() || this.renderStates.isEmpty()) {
            return;
        }
        Color resolvedFill = Managers.COLORS.resolve(this.fillColor);
        Color resolvedOutline = Managers.COLORS.resolve(this.outlineColor);
        for (Map.Entry<BlockPos, BlockRenderState> entry : this.renderStates.entrySet()) {
            BlockRenderState state = entry.getValue();
            state.setLength((float)((Double)this.fadeTime.getValue() * 1000.0));
            float factor = (float)state.getFactor();
            if (factor <= 0.0f) continue;
            float alphaFactor = (Boolean)this.fade.getValue() != false ? factor : 1.0f;
            Color fadedFill = ColorUtil.scaleAlpha(resolvedFill, alphaFactor);
            Color fadedOutline = ColorUtil.scaleAlpha(resolvedOutline, alphaFactor);
            Managers.RENDER.renderScaledBox(event.getMatrices(), entry.getKey(), 1.0f, fadedFill, true, false, null, (float)((Double)this.lineWidth.getValue()).doubleValue());
            Managers.RENDER.renderScaledBox(event.getMatrices(), entry.getKey(), 1.0f, fadedOutline, false, true, null, (float)((Double)this.lineWidth.getValue()).doubleValue());
        }
    }

    private List<BlockPos> collectTargets(long now) {
        List<BlockPos> candidates = ProtectionUtil.getSurroundPlacements((PlayerEntity)R0SE.mc.player, (Boolean)this.airPlace.getValue());
        if (((Boolean)this.debug.getValue()).booleanValue()) {
            this.logDebug("candidates=" + this.formatPositions(candidates));
        }
        ArrayList<BlockPos> targets = new ArrayList<BlockPos>();
        for (BlockPos pos : candidates) {
            if (targets.contains(pos)) continue;
            if (this.placed.containsKey(pos)) {
                long time = this.placed.get(pos);
                if (now - time < 60L) continue;
                this.placed.remove(pos);
            }
            targets.add(pos);
        }
        return targets;
    }

    private void updateRenderStates(List<BlockPos> targets) {
        for (BlockRenderState state : this.renderStates.values()) {
            state.markHidden();
        }
        for (BlockPos pos : targets) {
            this.renderStates.computeIfAbsent(pos, ignored -> new BlockRenderState(true, (float)((Double)this.fadeTime.getValue() * 1000.0), Easing.SMOOTH_STEP, this.tickCounter)).markVisible(this.tickCounter);
        }
    }

    private List<BlockPos> getRenderPositionsForTargets(List<BlockPos> targets) {
        LinkedHashSet<BlockPos> planned = new LinkedHashSet<BlockPos>(this.placed.keySet());
        LinkedHashSet<BlockPos> renderPositions = new LinkedHashSet<BlockPos>();
        for (BlockPos target : targets) {
            PlacementPlan plan = Managers.INTERACT.planPlacement(target, planned, (Boolean)this.airPlace.getValue());
            if (!plan.isValid()) {
                renderPositions.add(target);
                continue;
            }
            renderPositions.addAll(plan.getRenderPositions());
            planned.addAll(plan.getRenderPositions());
        }
        return new ArrayList<BlockPos>(renderPositions);
    }

    private void hideAllRenderStates() {
        for (BlockRenderState state : this.renderStates.values()) {
            state.markHidden();
        }
        this.cleanupRenderStates();
    }

    private void cleanupRenderStates() {
        this.renderStates.entrySet().removeIf(entry -> ((BlockRenderState)entry.getValue()).isFinished() && this.tickCounter - ((BlockRenderState)entry.getValue()).getLastSeenTick() > 1L);
    }

    private void cleanupPlaced(long now) {
        this.placed.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 200L);
    }

    private long nowOrCurrentMillis() {
        return System.currentTimeMillis();
    }

    private AntiCheat.RotationMode getRotationMode() {
        AntiCheat antiCheat = Managers.MODULES.getFeature(AntiCheat.class);
        return antiCheat == null ? AntiCheat.RotationMode.SILENT : (AntiCheat.RotationMode)((Object)antiCheat.getRotations().getValue());
    }

    private void logDebug(String message) {
        if (!((Boolean)this.debug.getValue()).booleanValue()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastDebugAt < 125L && (message.startsWith("targets=") || message.equals("no place targets"))) {
            return;
        }
        this.lastDebugAt = now;
        Managers.DEBUG.log("FeetPlaceDebug", message);
    }

    private String formatPositions(List<BlockPos> positions) {
        if (positions.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < positions.size(); ++i) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(positions.get(i).toShortString());
        }
        builder.append(']');
        return builder.toString();
    }

    private record PlacementAttempt(int attempts, int placedBlocks) {
    }
}


