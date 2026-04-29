/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Hand
 *  net.minecraft.util.ActionResult
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Type
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.client.network.ClientPlayerEntity
 */
package r0se.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import r0se.R0SE;
import r0se.api.inventory.SwapSession;
import r0se.api.rotation.Rotation;
import r0se.api.rotation.RotationActionResult;
import r0se.impl.module.client.AntiCheat;
import r0se.impl.module.world.SpeedMine;
import r0se.manager.Manager;
import r0se.manager.Managers;
import r0se.manager.api.PlacementContext;
import r0se.manager.api.PlacementFailReason;
import r0se.manager.api.PlacementPlan;
import r0se.manager.api.PlacementProfile;
import r0se.manager.api.PlacementResult;
import r0se.manager.impl.ActionSessionManager;
import r0se.util.world.BlockUtil;

public class InteractManager
implements Manager {
    private static final int INTERACT_ROTATION_PRIORITY = 300;
    private static final int INTERACT_ROTATION_TICKS = 2;
    private boolean placementLock;
    private SwapSession placementSwap = SwapSession.NONE;
    private Rotation pendingPlacementRotation;
    private long pendingPlacementRotationTick = -1L;
    private boolean placementRotationWaiting;

    public boolean isPlacementLocked() {
        return this.placementLock;
    }

    public boolean isPlacementRotationWaiting() {
        return this.placementRotationWaiting;
    }

    public boolean canInteract() {
        ClientPlayerEntity player = R0SE.mc.player;
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return player != null && (config == null || (Boolean)config.getMultiTask().getValue() != false || !player.isUsingItem());
    }

    public boolean startPlacement(int slot) {
        if (this.placementLock || slot < 0 || !this.canInteract()) {
            this.debug("startPlacement rejected lock=" + this.placementLock + " slot=" + slot + " canInteract=" + this.canInteract());
            return false;
        }
        SwapSession swap = Managers.INVENTORY.swapTo(slot);
        if (!swap.isValid()) {
            this.debug("startPlacement failed inventory swap slot=" + slot + " reason=" + swap.getFailReason());
            return false;
        }
        this.placementSwap = swap;
        this.placementLock = true;
        this.debug("startPlacement slot=" + slot + " mode=" + String.valueOf((Object)swap.getMode()) + " ownsSwap=" + swap.ownsSwap() + " serverSlot=" + Managers.INVENTORY.getServerSlot());
        return true;
    }

    public void endPlacement() {
        Managers.ROTATION.clearClientRotation();
        this.placementSwap.close();
        this.placementSwap = SwapSession.NONE;
        this.placementLock = false;
        this.debug("endPlacement");
    }

    public boolean placeBlock(BlockPos pos, int slot) {
        return this.placeBlock(pos, slot, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean placeBlock(BlockPos pos, int slot, boolean allowAirPlace) {
        SpeedMine speedMine;
        boolean placed;
        if (!this.startPlacement(slot)) {
            return false;
        }
        try {
            placed = this.placeBlock(pos, Hand.MAIN_HAND, allowAirPlace);
        }
        finally {
            this.endPlacement();
        }
        if (placed && (speedMine = Managers.MODULES.getFeature(SpeedMine.class)) != null) {
            speedMine.onPredictedPlacement(pos);
        }
        return placed;
    }

    public boolean placeBlock(BlockPos pos, Hand hand) {
        return this.placeBlock(pos, hand, false);
    }

    public boolean placeBlock(BlockPos pos, Hand hand, boolean allowAirPlace) {
        return this.placeBlock(pos, hand, Collections.emptySet(), allowAirPlace);
    }

    public boolean placeBlock(BlockPos pos, Hand hand, Set<BlockPos> plannedSupports, boolean allowAirPlace) {
        return this.tryPlaceBlock(pos, hand, plannedSupports, allowAirPlace).isSuccess();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PlacementResult tryPlaceBlock(BlockPos pos, Hand hand, Set<BlockPos> plannedSupports, boolean allowAirPlace) {
        ActionResult result;
        ItemStack stack;
        if (R0SE.mc.player == null || R0SE.mc.world == null || R0SE.mc.interactionManager == null) {
            this.debug("placeBlock invalid mc state");
            if (R0SE.mc.player == null) {
                return PlacementResult.fail(PlacementFailReason.NO_PLAYER);
            }
            if (R0SE.mc.world == null) {
                return PlacementResult.fail(PlacementFailReason.NO_WORLD);
            }
            return PlacementResult.fail(PlacementFailReason.NO_INTERACTION_MANAGER);
        }
        ItemStack class_17992 = stack = hand == Hand.MAIN_HAND ? Managers.INVENTORY.getHeldStack() : R0SE.mc.player.getOffHandStack();
        if (!(stack.getItem() instanceof BlockItem)) {
            this.debug("placeBlock invalid stack pos=" + pos.toShortString());
            return PlacementResult.fail(PlacementFailReason.NO_ITEM);
        }
        if (!R0SE.mc.world.getBlockState(pos).isReplaceable()) {
            this.debug("placeBlock invalid stack/replaceable pos=" + pos.toShortString());
            return PlacementResult.fail(PlacementFailReason.TARGET_NOT_REPLACEABLE);
        }
        if (BlockUtil.hasEntityCollision(pos)) {
            this.debug("placeBlock entity collision pos=" + pos.toShortString());
            return PlacementResult.fail(PlacementFailReason.ENTITY_COLLISION);
        }
        PlacementContext context = this.getPlacementContext(pos, plannedSupports == null ? Collections.emptySet() : plannedSupports, allowAirPlace);
        if (context == null) {
            this.debug("placeBlock no context pos=" + pos.toShortString() + " air=" + allowAirPlace);
            return PlacementResult.fail(allowAirPlace ? PlacementFailReason.AIRPLACE_REJECTED : PlacementFailReason.NO_CLICKABLE_SIDE);
        }
        if (!this.isWithinPlaceRange(context)) {
            this.debug("placeBlock range failed pos=" + pos.toShortString() + " distance=" + String.format("%.2f", this.getPlaceDistance(context)) + " max=" + this.getPlacementProfile().getRange());
            return PlacementResult.fail(PlacementFailReason.RANGE_FAILED, context, null);
        }
        BlockHitResult hitResult = context.toHitResult();
        if (!this.applyRotation(context.getHitPos())) {
            this.debug("placeBlock rotation rejected pos=" + pos.toShortString());
            return PlacementResult.fail(PlacementFailReason.ROTATION_REJECTED, context, null);
        }
        ActionSessionManager.ActionSession session = Managers.ACTIONS.begin("place", "Interact", context.getTargetPos(), context.getClickedFace());
        try {
            result = this.sendPlacement(context, hitResult, hand);
        }
        finally {
            Managers.ACTIONS.end(session, "sent");
        }
        this.debug("placeBlock pos=" + pos.toShortString() + " clicked=" + context.getClickedPos().toShortString() + " face=" + String.valueOf(context.getClickedFace()) + " replace=" + context.isReplaceClicked() + " air=" + context.isAirPlace() + " profile=" + this.getPlacementProfile().name() + " strict=" + String.valueOf((Object)this.getStrictDirectionMode()) + " distance=" + String.format("%.2f", this.getPlaceDistance(context)) + " inventory=" + this.describeInventoryState() + " result=" + String.valueOf(result));
        if (result != null && result.isAccepted()) {
            this.swing(hand);
            return PlacementResult.success(context, result);
        }
        return PlacementResult.fail(PlacementFailReason.INTERACT_REJECTED, context, result);
    }

    public boolean canPlaceBlock(BlockPos pos) {
        return this.canPlaceBlock(pos, Collections.emptySet());
    }

    public PlacementPlan planPlacement(BlockPos targetPos, Set<BlockPos> plannedSupports, boolean allowAirPlace) {
        if (targetPos == null || R0SE.mc.player == null || R0SE.mc.world == null) {
            return PlacementPlan.fail(targetPos, R0SE.mc.player == null ? PlacementFailReason.NO_PLAYER : PlacementFailReason.NO_WORLD);
        }
        if (!R0SE.mc.world.getBlockState(targetPos).isReplaceable()) {
            return PlacementPlan.fail(targetPos, PlacementFailReason.TARGET_NOT_REPLACEABLE);
        }
        if (BlockUtil.hasEntityCollision(targetPos)) {
            this.debug("planPlacement entity collision target=" + targetPos.toShortString());
            return PlacementPlan.fail(targetPos, PlacementFailReason.ENTITY_COLLISION);
        }
        Set<BlockPos> planned = plannedSupports == null ? Collections.emptySet() : plannedSupports;
        PlacementContext direct = this.getPlacementContext(targetPos, planned, allowAirPlace);
        if (direct != null) {
            if (!this.isWithinPlaceRange(direct)) {
                this.debug("planPlacement range failed target=" + targetPos.toShortString() + " distance=" + String.format("%.2f", this.getPlaceDistance(direct)) + " max=" + this.getPlacementProfile().getRange());
                return PlacementPlan.fail(targetPos, PlacementFailReason.RANGE_FAILED);
            }
            return PlacementPlan.direct(targetPos, direct);
        }
        if (allowAirPlace) {
            return PlacementPlan.fail(targetPos, PlacementFailReason.AIRPLACE_REJECTED);
        }
        PlacementPlan supportPlan = this.findSupportPlan(targetPos, planned);
        if (supportPlan.isValid()) {
            return supportPlan;
        }
        return PlacementPlan.fail(targetPos, supportPlan.getFailReason());
    }

    public boolean canPlanPlaceBlock(BlockPos pos) {
        return this.canPlanPlaceBlock(pos, Collections.emptySet(), false);
    }

    public boolean canPlanPlaceBlock(BlockPos pos, Set<BlockPos> plannedSupports) {
        return this.canPlanPlaceBlock(pos, plannedSupports, false);
    }

    public boolean canPlanPlaceBlock(BlockPos pos, Set<BlockPos> plannedSupports, boolean allowAirPlace) {
        if (pos == null || R0SE.mc.player == null || R0SE.mc.world == null) {
            this.debug("canPlanPlaceBlock invalid state pos=" + (pos == null ? "null" : pos.toShortString()));
            return false;
        }
        if (!R0SE.mc.world.getBlockState(pos).isReplaceable()) {
            this.debug("canPlanPlaceBlock blocked replaceable=false pos=" + pos.toShortString());
            return false;
        }
        if (BlockUtil.hasEntityCollision(pos)) {
            this.debug("canPlanPlaceBlock blocked entity_collision pos=" + pos.toShortString());
            return false;
        }
        if (allowAirPlace) {
            this.debug("canPlanPlaceBlock airplace=true pos=" + pos.toShortString());
            return true;
        }
        PlacementContext context = this.getPlacementContext(pos, plannedSupports == null ? Collections.emptySet() : plannedSupports, false);
        boolean allowed = context != null;
        this.debug("canPlanPlaceBlock " + allowed + " pos=" + pos.toShortString() + " side=" + (context == null ? "none" : context.getClickedFace().asString()) + " planned=" + (plannedSupports == null ? 0 : plannedSupports.size()));
        return allowed;
    }

    public boolean canPlaceBlock(BlockPos pos, Set<BlockPos> plannedSupports) {
        return this.canPlaceBlock(pos, plannedSupports, false);
    }

    public boolean canPlaceBlock(BlockPos pos, Set<BlockPos> plannedSupports, boolean allowAirPlace) {
        if (pos == null || R0SE.mc.player == null || R0SE.mc.world == null) {
            return false;
        }
        if (!R0SE.mc.world.getBlockState(pos).isReplaceable()) {
            return false;
        }
        if (BlockUtil.hasEntityCollision(pos)) {
            return false;
        }
        return this.getPlacementContext(pos, plannedSupports, allowAirPlace) != null;
    }

    public Direction getPlanDirection(BlockPos pos) {
        return this.getPlanDirection(pos, Collections.emptySet());
    }

    public Direction getPlanDirection(BlockPos pos, Set<BlockPos> plannedSupports) {
        if (pos == null || R0SE.mc.world == null || !R0SE.mc.world.getBlockState(pos).isReplaceable()) {
            return null;
        }
        PlacementContext context = this.getPlacementContext(pos, plannedSupports == null ? Collections.emptySet() : plannedSupports, false);
        return context == null ? null : context.getClickedFace();
    }

    public ActionResult interactBlock(BlockHitResult hitResult, Hand hand) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null || hitResult == null || !this.canInteract()) {
            return ActionResult.FAIL;
        }
        if (!this.applyRotation(hitResult.getPos())) {
            return ActionResult.FAIL;
        }
        ActionResult result = R0SE.mc.interactionManager.interactBlock(R0SE.mc.player, hand, hitResult);
        if (result != null && result.isAccepted()) {
            this.swing(hand);
        }
        Managers.ROTATION.clearClientRotation();
        return result;
    }

    public ActionResult interactItem(Hand hand) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null || !this.canInteract()) {
            return ActionResult.FAIL;
        }
        if (!this.applyRotation(R0SE.mc.player.getEyePos().add(R0SE.mc.player.getRotationVecClient().multiply(4.0)))) {
            return ActionResult.FAIL;
        }
        ActionResult result = R0SE.mc.interactionManager.interactItem((PlayerEntity)R0SE.mc.player, hand);
        if (result != null && result.isAccepted()) {
            this.swing(hand);
        }
        Managers.ROTATION.clearClientRotation();
        return result;
    }

    public boolean attackEntity(Entity entity) {
        if (R0SE.mc.player == null || R0SE.mc.interactionManager == null || entity == null || !entity.isAlive()) {
            return false;
        }
        if (!this.applyRotation(entity.getBoundingBox().getCenter())) {
            return false;
        }
        R0SE.mc.interactionManager.attackEntity((PlayerEntity)R0SE.mc.player, entity);
        this.swing(Hand.MAIN_HAND);
        Managers.ROTATION.clearClientRotation();
        return true;
    }

    private void swing(Hand hand) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (R0SE.mc.player != null && (config == null || ((Boolean)config.getSwing().getValue()).booleanValue())) {
            R0SE.mc.player.swingHand(hand);
        }
    }

    private boolean applyRotation(Vec3d target) {
        boolean strictWait;
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config == null || R0SE.mc.player == null) {
            return false;
        }
        Rotation rotation = this.getRotationTo(R0SE.mc.player.getEyePos(), target);
        RotationActionResult result = Managers.ROTATION.prepareActionRotation(rotation, 300, 2, "interact", strictWait = this.shouldWaitForPlacementRotation(config), this.pendingPlacementRotation, this.pendingPlacementRotationTick);
        if (!result.isReady()) {
            if (result.isWaiting()) {
                this.pendingPlacementRotation = result.getRotation();
                this.pendingPlacementRotationTick = result.getTick();
                this.placementRotationWaiting = true;
            } else {
                this.placementRotationWaiting = false;
            }
            this.debug("applyRotation rejected mode=" + String.valueOf(config.getRotations().getValue()) + " state=" + String.valueOf((Object)result.getState()) + " strictWait=" + strictWait + " yaw=" + String.format("%.2f", Float.valueOf(rotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(rotation.getPitch())));
            return false;
        }
        this.pendingPlacementRotation = null;
        this.pendingPlacementRotationTick = -1L;
        this.placementRotationWaiting = false;
        this.debug("applyRotation mode=" + String.valueOf(config.getRotations().getValue()) + " state=" + String.valueOf((Object)result.getState()) + " strictWait=" + strictWait + " yaw=" + String.format("%.2f", Float.valueOf(rotation.getYaw())) + " pitch=" + String.format("%.2f", Float.valueOf(rotation.getPitch())) + " serverYaw=" + String.format("%.2f", Float.valueOf(Managers.ROTATION.getServerRotation().getYaw())) + " serverPitch=" + String.format("%.2f", Float.valueOf(Managers.ROTATION.getServerRotation().getPitch())));
        return true;
    }

    private boolean shouldWaitForPlacementRotation(AntiCheat config) {
        return config != null && (Boolean)config.getPlaceWait().getValue() != false && config.getStrictDirection().getValue() == AntiCheat.StrictDirectionMode.GRIM && config.getRotations().getValue() == AntiCheat.RotationMode.SILENT;
    }

    private Rotation getRotationTo(Vec3d from, Vec3d to) {
        Vec3d delta = to.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, horizontal)));
        return new Rotation(yaw, pitch);
    }

    private PlacementContext getPlacementContext(BlockPos targetPos) {
        return this.getPlacementContext(targetPos, Collections.emptySet());
    }

    private PlacementContext getPlacementContext(BlockPos targetPos, Set<BlockPos> plannedSupports) {
        return this.getPlacementContext(targetPos, plannedSupports, false);
    }

    private PlacementContext getPlacementContext(BlockPos targetPos, Set<BlockPos> plannedSupports, boolean allowAirPlace) {
        if (R0SE.mc.world == null || R0SE.mc.player == null) {
            return null;
        }
        PlacementContext best = this.getBestPlacementContext(targetPos, plannedSupports);
        if (best != null) {
            return best;
        }
        return allowAirPlace ? this.getAirPlacementContext(targetPos) : null;
    }

    private PlacementPlan findSupportPlan(BlockPos targetPos, Set<BlockPos> planned) {
        PlacementPlan best = PlacementPlan.fail(targetPos, PlacementFailReason.NO_CLICKABLE_SIDE);
        List<BlockPos> candidates = this.getSupportCandidates(targetPos);
        int rejected = 0;
        for (BlockPos supportPos : candidates) {
            if (planned.contains(supportPos)) {
                ++rejected;
                this.debug("support reject reason=planned target=" + targetPos.toShortString() + " support=" + supportPos.toShortString());
                continue;
            }
            if (!BlockUtil.canPlace(supportPos)) {
                ++rejected;
                this.debug("support reject reason=cannot_place target=" + targetPos.toShortString() + " support=" + supportPos.toShortString());
                continue;
            }
            PlacementContext supportContext = this.getPlacementContext(supportPos, planned, false);
            if (supportContext == null) {
                ++rejected;
                this.debug("support reject reason=no_support_context target=" + targetPos.toShortString() + " support=" + supportPos.toShortString());
                continue;
            }
            if (!this.isWithinPlaceRange(supportContext)) {
                ++rejected;
                this.debug("support reject reason=range target=" + targetPos.toShortString() + " support=" + supportPos.toShortString() + " distance=" + String.format("%.2f", this.getPlaceDistance(supportContext)));
                continue;
            }
            LinkedHashSet<BlockPos> withSupport = new LinkedHashSet<BlockPos>(planned);
            withSupport.add(supportPos);
            PlacementContext targetAfterSupport = this.getPlacementContext(targetPos, withSupport, false);
            if (targetAfterSupport == null) {
                ++rejected;
                this.debug("support reject reason=target_still_blocked target=" + targetPos.toShortString() + " support=" + supportPos.toShortString());
                continue;
            }
            if (!this.isWithinPlaceRange(targetAfterSupport)) {
                ++rejected;
                this.debug("support reject reason=target_range target=" + targetPos.toShortString() + " support=" + supportPos.toShortString() + " distance=" + String.format("%.2f", this.getPlaceDistance(targetAfterSupport)));
                continue;
            }
            best = PlacementPlan.withSupport(targetPos, supportPos, targetAfterSupport, supportContext);
            this.debug("support plan target=" + targetPos.toShortString() + " support=" + supportPos.toShortString() + " candidates=" + candidates.size() + " rejected=" + rejected + " score=" + String.format("%.2f", this.getSupportScore(targetPos, supportPos)));
            break;
        }
        if (!best.isValid()) {
            this.debug("support plan failed target=" + targetPos.toShortString() + " candidates=" + candidates.size() + " rejected=" + rejected);
        }
        return best;
    }

    private List<BlockPos> getSupportCandidates(BlockPos targetPos) {
        ArrayList<BlockPos> candidates = new ArrayList<BlockPos>();
        candidates.add(targetPos.down());
        for (Direction direction : Direction.Type.HORIZONTAL) {
            candidates.add(targetPos.offset(direction));
        }
        candidates.sort(Comparator.comparingDouble(pos -> this.getSupportScore(targetPos, (BlockPos)pos)));
        return candidates;
    }

    private double getSupportScore(BlockPos targetPos, BlockPos supportPos) {
        double score = 0.0;
        if (supportPos.equals((Object)targetPos.down())) {
            score -= 100.0;
        }
        if (R0SE.mc.player != null) {
            score += R0SE.mc.player.squaredDistanceTo(supportPos.toCenterPos()) * 0.01;
        }
        return score += (double)Math.abs(supportPos.getY() - targetPos.getY()) * 2.0;
    }

    private boolean isWithinPlaceRange(PlacementContext context) {
        return R0SE.mc.player != null && this.getPlaceDistanceSq(context) <= this.getPlacementProfile().getRange() * this.getPlacementProfile().getRange();
    }

    private double getPlaceDistance(PlacementContext context) {
        return Math.sqrt(this.getPlaceDistanceSq(context));
    }

    private double getPlaceDistanceSq(PlacementContext context) {
        if (R0SE.mc.player == null || context == null) {
            return Double.MAX_VALUE;
        }
        return R0SE.mc.player.getEyePos().squaredDistanceTo(context.getHitPos());
    }

    private PlacementContext getAirPlacementContext(BlockPos targetPos) {
        if (R0SE.mc.player == null || R0SE.mc.world == null || !R0SE.mc.world.getBlockState(targetPos).isReplaceable()) {
            return null;
        }
        Box targetBounds = new Box(targetPos);
        Vec3d hitPos = new Vec3d(targetBounds.getCenter().x, targetBounds.maxY, targetBounds.getCenter().z);
        PlacementContext context = new PlacementContext(targetPos, targetPos, Direction.UP, hitPos, false, true);
        return this.isContextAllowed(context, targetBounds) ? context : null;
    }

    private boolean isAirPlaceContext(PlacementContext context) {
        return context.isAirPlace();
    }

    private ActionResult sendPlacement(PlacementContext context, BlockHitResult hitResult, Hand hand) {
        PlacementProfile profile = this.getPlacementProfile();
        if (profile.shouldUseSequencedPacket(context.isAirPlace())) {
            this.debug("sendPlacement packet profile=" + profile.name() + " air=" + context.isAirPlace() + " clicked=" + context.getClickedPos().toShortString() + " face=" + String.valueOf(context.getClickedFace()) + " inventory=" + this.describeInventoryState());
            return this.sendPlacementPacket(hitResult, hand);
        }
        this.debug("sendPlacement internal profile=" + profile.name() + " clicked=" + context.getClickedPos().toShortString() + " face=" + String.valueOf(context.getClickedFace()) + " inventory=" + this.describeInventoryState());
        return R0SE.mc.interactionManager.interactBlock(R0SE.mc.player, hand, hitResult);
    }

    private ActionResult sendPlacementPacket(BlockHitResult hitResult, Hand hand) {
        if (R0SE.mc.interactionManager == null || R0SE.mc.world == null) {
            return ActionResult.FAIL;
        }
        return Managers.NETWORK.sendSequencedPacket(sequence -> new PlayerInteractBlockC2SPacket(hand, hitResult, sequence), "place-sequenced") ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    private PlacementContext getBestPlacementContext(BlockPos pos, Set<BlockPos> plannedSupports) {
        if (R0SE.mc.world == null) {
            return null;
        }
        Set<BlockPos> planned = plannedSupports == null ? Collections.emptySet() : plannedSupports;
        PlacementContext best = null;
        double bestScore = Double.MAX_VALUE;
        int rejected = 0;
        for (Direction direction : Direction.values()) {
            Vec3d hitPos;
            BlockPos neighbor = pos.offset(direction);
            boolean plannedSupport = planned.contains(neighbor);
            if (R0SE.mc.world.getBlockState(neighbor).isReplaceable() && !plannedSupport) {
                ++rejected;
                continue;
            }
            Direction clickedFace = direction.getOpposite();
            PlacementContext context = new PlacementContext(pos, neighbor, clickedFace, hitPos = this.getSnowHitPosition(neighbor, clickedFace), false);
            if (!this.isContextAllowed(context, new Box(neighbor))) {
                ++rejected;
                this.debug("context reject reason=strict target=" + pos.toShortString() + " clicked=" + neighbor.toShortString() + " face=" + String.valueOf(clickedFace) + " strict=" + String.valueOf((Object)this.getStrictDirectionMode()));
                continue;
            }
            double score = this.getPlacementScore(context, plannedSupport);
            if (!(score < bestScore)) continue;
            best = context;
            bestScore = score;
        }
        if (best != null) {
            this.debug("context selected target=" + pos.toShortString() + " clicked=" + best.getClickedPos().toShortString() + " face=" + String.valueOf(best.getClickedFace()) + " score=" + String.format("%.3f", bestScore) + " rejected=" + rejected + " strict=" + String.valueOf((Object)this.getStrictDirectionMode()) + " profile=" + this.getPlacementProfile().name());
        }
        return best;
    }

    private double getPlacementScore(PlacementContext context, boolean plannedSupport) {
        double score = this.getPlaceDistanceSq(context) * 0.1;
        Rotation rotation = this.getRotationTo(R0SE.mc.player.getEyePos(), context.getHitPos());
        score += (double)Math.abs(this.wrapDegrees(Managers.ROTATION.getServerRotation().getYaw() - rotation.getYaw())) * 0.01;
        score += (double)Math.abs(Managers.ROTATION.getServerRotation().getPitch() - rotation.getPitch()) * 0.01;
        if (plannedSupport) {
            score -= 1.0;
        }
        if (context.getClickedFace() == Direction.UP) {
            score -= 0.25;
        }
        return score;
    }

    private boolean isSnowFaceVisible(BlockPos clickedPos, Direction clickedFace) {
        if (R0SE.mc.player == null) {
            return false;
        }
        Vec3d eyes = R0SE.mc.player.getEyePos();
        Vec3d centered = clickedPos.toCenterPos();
        Vec3d offset = Vec3d.of((Vec3i)clickedFace.getVector()).multiply(0.5);
        Vec3d faceCenter = centered.add(offset);
        return switch (clickedFace) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> {
                if (eyes.z < faceCenter.z) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (eyes.z > faceCenter.z) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (eyes.x < faceCenter.x) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (eyes.x > faceCenter.x) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (this.getStrictDirectionMode() == AntiCheat.StrictDirectionMode.GRIM) {
                    if (eyes.y > faceCenter.y) {
                        yield true;
                    }
                    yield false;
                }
                if (eyes.y + 0.5 > faceCenter.y) {
                    yield true;
                }
                yield false;
            }
            case Direction.DOWN -> eyes.y < faceCenter.y;
        };
    }

    private Vec3d getSnowHitPosition(BlockPos clickedPos, Direction clickedFace) {
        return clickedPos.toCenterPos().add(Vec3d.of((Vec3i)clickedFace.getVector()).multiply(0.5));
    }

    private boolean isContextAllowed(PlacementContext context, Box clickedBounds) {
        AntiCheat.StrictDirectionMode mode = this.getStrictDirectionMode();
        if (mode == AntiCheat.StrictDirectionMode.OFF || R0SE.mc.player == null || R0SE.mc.world == null) {
            return true;
        }
        if (!this.isFaceVisible(clickedBounds, context.getClickedFace())) {
            return false;
        }
        if (mode == AntiCheat.StrictDirectionMode.GRIM) {
            Vec3d hitPos = context.getHitPos();
            return switch (context.getClickedFace()) {
                default -> throw new MatchException(null, null);
                case Direction.NORTH, Direction.SOUTH -> {
                    if (hitPos.x >= clickedBounds.minX && hitPos.x <= clickedBounds.maxX && hitPos.y >= clickedBounds.minY && hitPos.y <= clickedBounds.maxY) {
                        yield true;
                    }
                    yield false;
                }
                case Direction.WEST, Direction.EAST -> {
                    if (hitPos.z >= clickedBounds.minZ && hitPos.z <= clickedBounds.maxZ && hitPos.y >= clickedBounds.minY && hitPos.y <= clickedBounds.maxY) {
                        yield true;
                    }
                    yield false;
                }
                case Direction.UP, Direction.DOWN -> hitPos.x >= clickedBounds.minX && hitPos.x <= clickedBounds.maxX && hitPos.z >= clickedBounds.minZ && hitPos.z <= clickedBounds.maxZ;
            };
        }
        return true;
    }

    private boolean isFaceVisible(Box clickedBounds, Direction clickedFace) {
        if (R0SE.mc.player == null) {
            return false;
        }
        Vec3d eyePos = R0SE.mc.player.getEyePos();
        if (clickedBounds.contains(eyePos)) {
            return true;
        }
        return switch (clickedFace) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> {
                if (eyePos.z <= clickedBounds.minZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (eyePos.z >= clickedBounds.maxZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (eyePos.x <= clickedBounds.minX) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (eyePos.x >= clickedBounds.maxX) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (eyePos.y >= clickedBounds.maxY) {
                    yield true;
                }
                yield false;
            }
            case Direction.DOWN -> eyePos.y <= clickedBounds.minY;
        };
    }

    private Vec3d getHitPosition(BlockPos targetPos, Direction clickedFace, Box clickedBounds) {
        Vec3d center = Vec3d.ofCenter((Vec3i)targetPos);
        double x = this.clamp(center.x, clickedBounds.minX, clickedBounds.maxX);
        double y = this.clamp(center.y, clickedBounds.minY, clickedBounds.maxY);
        double z = this.clamp(center.z, clickedBounds.minZ, clickedBounds.maxZ);
        return switch (clickedFace) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> new Vec3d(x, y, clickedBounds.minZ);
            case Direction.SOUTH -> new Vec3d(x, y, clickedBounds.maxZ);
            case Direction.WEST -> new Vec3d(clickedBounds.minX, y, z);
            case Direction.EAST -> new Vec3d(clickedBounds.maxX, y, z);
            case Direction.UP -> new Vec3d(x, clickedBounds.maxY, z);
            case Direction.DOWN -> new Vec3d(x, clickedBounds.minY, z);
        };
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private PlacementProfile getPlacementProfile() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return PlacementProfile.fromMode(config == null ? AntiCheat.PlacementMode.VANILLA : (AntiCheat.PlacementMode)((Object)config.getPlacements().getValue()));
    }

    private AntiCheat.StrictDirectionMode getStrictDirectionMode() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config == null ? AntiCheat.StrictDirectionMode.OFF : (AntiCheat.StrictDirectionMode)((Object)config.getStrictDirection().getValue());
    }

    private void debug(String message) {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        if (config == null || !((Boolean)config.getDebug().getValue()).booleanValue()) {
            return;
        }
        Managers.DEBUG.log("InteractDebug", message);
    }

    private float wrapDegrees(float value) {
        return MathHelper.wrapDegrees((float)value);
    }

    private String describeInventoryState() {
        if (R0SE.mc.player == null) {
            return "no_player";
        }
        return "clientSlot=" + R0SE.mc.player.getInventory().selectedSlot + ",serverSlot=" + Managers.INVENTORY.getServerSlot() + ",swapActive=" + Managers.INVENTORY.hasSwap();
    }
}



