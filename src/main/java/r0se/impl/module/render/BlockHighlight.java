/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.block.BlockState
 *  net.minecraft.util.hit.BlockHitResult
 */
package r0se.impl.module.render;

import java.awt.Color;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.render.RenderWorldEvent;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.render.BoxRenderMode;
import r0se.api.render.ColorUtil;
import r0se.api.render.Easing;
import r0se.api.render.state.BlockRenderState;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.Setting;
import r0se.manager.Managers;

public class BlockHighlight
extends ToggleableFeature {
    private final BoolSetting line = this.addSetting(new BoolSetting("Line", false));
    private final BoolSetting text = this.addSetting(new BoolSetting("Text", true));
    private final DoubleSetting textScale = this.addSetting(new DoubleSetting("TextScale", 0.02, 0.01, 0.05));
    private final BoolSetting render = this.addSetting((BoolSetting)new BoolSetting("Enabled", true).hide());
    private final GroupSetting renderGroup = this.addSetting(new GroupSetting("Render", false).linkToggle(this.render));
    private final ColorSetting fillColor = this.addSetting((ColorSetting)((Setting)new ColorSetting("Fill", new Color(100, 86, 255, 90)).enableSync(ColorSyncMode.SECONDARY).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final ColorSetting outlineColor = this.addSetting((ColorSetting)((Setting)new ColorSetting("Outline", new Color(170, 150, 255, 220)).enableSync(ColorSyncMode.PRIMARY).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final DoubleSetting lineWidth = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("LineWidth", 1.5, 0.5, 4.0).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final BoolSetting fade = this.addSetting((BoolSetting)((Setting)new BoolSetting("Fade", true).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private final EnumSetting<BoxRenderMode> renderMode = this.addSetting((EnumSetting)((Setting)new EnumSetting<BoxRenderMode>("RenderMode", BoxRenderMode.BOTH).insideGroup(this.renderGroup)).visibleWhen(this.renderGroup::isExpanded));
    private BlockPos renderTargetPos;
    private Vec3d renderHitPos;
    private String renderLabel;
    private final BlockRenderState renderState = new BlockRenderState(false, 180.0f, Easing.SMOOTH_STEP, 0L);

    public BlockHighlight() {
        super("BlockHighlight", "Highlights the block you are currently looking at", FeatureCategory.RENDER, "selectionesp", "blockesp");
    }

    @Override
    protected void onDisable() {
        this.clearTarget();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        BlockHitResult blockHitResult;
        if (R0SE.mc.player == null || R0SE.mc.world == null) {
            this.clearTarget();
            return;
        }
        HitResult ItemStackParticleEffect = R0SE.mc.crosshairTarget;
        if (!(ItemStackParticleEffect instanceof BlockHitResult) || (blockHitResult = (BlockHitResult)ItemStackParticleEffect).getType() != HitResult.Type.BLOCK) {
            this.hideTarget();
            return;
        }
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = R0SE.mc.world.getBlockState(pos);
        if (state.isAir()) {
            this.hideTarget();
            return;
        }
        this.renderTargetPos = pos.toImmutable();
        this.renderHitPos = blockHitResult.getPos();
        this.renderLabel = state.getBlock().getName().getString();
        this.renderState.markVisible(System.currentTimeMillis());
    }

    @Subscribe
    public void onRenderWorld(RenderWorldEvent event) {
        float factor;
        if (!((Boolean)this.render.getValue()).booleanValue() || this.renderTargetPos == null) {
            return;
        }
        float f = factor = (Boolean)this.fade.getValue() != false ? (float)this.renderState.getFactor() : 1.0f;
        if (factor <= 0.0f) {
            if (this.renderState.isFinished()) {
                this.clearTarget();
            }
            return;
        }
        Color baseFill = Managers.COLORS.resolve(this.fillColor);
        Color baseOutline = Managers.COLORS.resolve(this.outlineColor);
        Color fillRender = ColorUtil.scaleAlpha(baseFill, factor);
        Color outlineRender = ColorUtil.scaleAlpha(baseOutline, factor);
        Color boxColor = switch ((BoxRenderMode)((Object)this.renderMode.getValue())) {
            default -> throw new MatchException(null, null);
            case BoxRenderMode.FILL -> fillRender;
            case BoxRenderMode.OUTLINE, BoxRenderMode.BOTH -> outlineRender;
        };
        float scale = (Boolean)this.fade.getValue() != false ? Math.max(0.2f, factor) : 1.0f;
        ((BoxRenderMode)((Object)this.renderMode.getValue())).renderScaled(Managers.RENDER, event.getMatrices(), this.renderTargetPos, scale, boxColor, ((Double)this.lineWidth.getValue()).floatValue());
        if (((Boolean)this.line.getValue()).booleanValue()) {
            Vec3d lineEnd;
            Vec3d lineStart = this.renderTargetPos.toCenterPos();
            Vec3d VanillaChestLootTableGenerator = lineEnd = this.renderHitPos != null ? this.renderHitPos : lineStart;
            if (lineStart.squaredDistanceTo(lineEnd) > 1.0E-4) {
                Managers.RENDER.renderLine(lineStart, lineEnd, outlineRender, ((Double)this.lineWidth.getValue()).floatValue());
            }
        }
        if (((Boolean)this.text.getValue()).booleanValue() && this.renderLabel != null) {
            Vec3d textPos = this.renderTargetPos.toCenterPos().add(0.0, 0.75, 0.0);
            int color = ColorUtil.withAlpha(-1, (int)(factor * 255.0f));
            Managers.RENDER.renderWorldText(textPos, this.renderLabel, ((Double)this.textScale.getValue()).floatValue(), color, true);
        }
    }

    private void hideTarget() {
        if (this.renderTargetPos == null) {
            return;
        }
        if (((Boolean)this.fade.getValue()).booleanValue()) {
            this.renderState.markHidden();
        } else {
            this.clearTarget();
        }
    }

    private void clearTarget() {
        this.renderTargetPos = null;
        this.renderHitPos = null;
        this.renderLabel = null;
        this.renderState.setStateHard(false, System.currentTimeMillis());
    }
}



