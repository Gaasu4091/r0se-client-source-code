/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.ToolItem
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.world.BlockView
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.block.BlockState
 *  net.minecraft.registry.entry.RegistryEntry
 */
package r0se.manager.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.impl.module.client.AntiCheat;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class BlockProgressManager
implements Manager {
    private final List<BreakEntry> breakEntries = new CopyOnWriteArrayList<BreakEntry>();

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.breakEntries.clear();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (R0SE.mc.player == null || R0SE.mc.world == null) {
            this.breakEntries.clear();
            return;
        }
        this.breakEntries.removeIf(entry -> R0SE.mc.world.getBlockState(entry.getPos()).isAir() || System.currentTimeMillis() - entry.getStartTime() > 5000L);
        for (BreakEntry entry2 : this.breakEntries) {
            entry2.updateDamage();
        }
    }

    public void onBreakProgress(int entityId, BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.countBreaks(entityId) >= 2L) {
            this.breakEntries.stream().filter(entry -> entry.getEntityId() == entityId).min(Comparator.comparingLong(BreakEntry::getStartTime)).ifPresent(this.breakEntries::remove);
        }
        BreakEntry entry2 = new BreakEntry(entityId, pos.toImmutable());
        entry2.start();
        this.breakEntries.add(entry2);
        this.debug("track entity=" + entityId + " pos=" + pos.toShortString());
    }

    public void clear(BlockPos pos, String source) {
        if (pos == null) {
            return;
        }
        boolean removed = this.breakEntries.removeIf(entry -> pos.equals((Object)entry.getPos()));
        if (removed) {
            this.debug("clear source=" + source + " pos=" + pos.toShortString());
        }
    }

    public long countBreaks(int entityId) {
        return this.breakEntries.stream().filter(entry -> entry.getEntityId() == entityId).count();
    }

    public boolean isBreaking(BlockPos pos) {
        return pos != null && this.breakEntries.stream().anyMatch(entry -> pos.equals((Object)entry.getPos()));
    }

    public boolean isPassed(BlockPos pos, float damage) {
        return pos != null && this.breakEntries.stream().anyMatch(entry -> pos.equals((Object)entry.getPos()) && entry.getBlockDamage() >= damage);
    }

    public Set<BlockPos> getMines(float damage) {
        return this.breakEntries.stream().filter(entry -> entry.getBlockDamage() >= damage).map(BreakEntry::getPos).collect(Collectors.toSet());
    }

    public float calcBlockBreakingDelta(BlockState state, BlockPos pos) {
        if (R0SE.mc.player == null || R0SE.mc.world == null || state == null || state.isAir()) {
            return 1.0f;
        }
        float hardness = state.getHardness((BlockView)R0SE.mc.world, pos);
        if (hardness == -1.0f) {
            return 0.0f;
        }
        int divisor = this.canHarvest(state) ? 30 : 100;
        return this.getBlockBreakingSpeed(state) / hardness / (float)divisor;
    }

    private float getBlockBreakingSpeed(BlockState state) {
        int efficiency;
        int tool = this.getBestTool(state);
        ItemStack stack = R0SE.mc.player.getInventory().getStack(tool);
        float speed = stack.getMiningSpeedMultiplier(state);
        if (speed > 1.0f && (efficiency = this.getEfficiencyLevel(stack)) > 0 && !stack.isEmpty()) {
            speed += (float)(efficiency * efficiency) + 1.0f;
        }
        if (R0SE.mc.player.hasStatusEffect(StatusEffects.HASTE)) {
            int amplifier = R0SE.mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1;
            speed *= 1.0f + (float)amplifier * 0.2f;
        }
        if (R0SE.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float multiplier = switch (R0SE.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            };
            speed *= multiplier;
        }
        if (!R0SE.mc.player.isOnGround()) {
            speed /= 5.0f;
        }
        return speed;
    }

    private boolean canHarvest(BlockState state) {
        if (!state.isToolRequired()) {
            return true;
        }
        return R0SE.mc.player.getInventory().getStack(this.getBestTool(state)).isSuitableFor(state);
    }

    private int getBestTool(BlockState state) {
        int slot = -1;
        float bestSpeed = 0.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = R0SE.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ToolItem) && !(stack.getItem() instanceof SwordItem)) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            int efficiency = this.getEfficiencyLevel(stack);
            if (efficiency > 0) {
                speed += (float)(efficiency * efficiency) + 1.0f;
            }
            if (!(speed > bestSpeed)) continue;
            bestSpeed = speed;
            slot = i;
        }
        return slot == -1 ? R0SE.mc.player.getInventory().selectedSlot : slot;
    }

    private int getEfficiencyLevel(ItemStack stack) {
        for (Object2IntMap.Entry entry : EnchantmentHelper.getEnchantments((ItemStack)stack).getEnchantmentEntries()) {
            if (!((RegistryEntry)entry.getKey()).getKey().isPresent() || ((RegistryEntry)entry.getKey()).getKey().get() != Enchantments.EFFICIENCY) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    private void debug(String message) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config != null && ((Boolean)config.getDebug().getValue()).booleanValue()) {
            Managers.DEBUG.log("BlockProgressDebug", message);
        }
    }

    public final class BreakEntry {
        private final int entityId;
        private final BlockPos pos;
        private long startTime;
        private float blockDamage;
        private boolean started;

        private BreakEntry(int entityId, BlockPos pos) {
            this.entityId = entityId;
            this.pos = pos;
        }

        private void start() {
            this.started = true;
            this.startTime = System.currentTimeMillis();
        }

        private void updateDamage() {
            if (this.started && R0SE.mc.world != null) {
                this.blockDamage = Math.min(1.0f, this.blockDamage + BlockProgressManager.this.calcBlockBreakingDelta(R0SE.mc.world.getBlockState(this.pos), this.pos));
            }
        }

        public int getEntityId() {
            return this.entityId;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public float getBlockDamage() {
            return this.blockDamage;
        }
    }
}


