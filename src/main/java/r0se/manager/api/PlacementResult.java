/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.ActionResult
 */
package r0se.manager.api;

import net.minecraft.util.ActionResult;
import r0se.manager.api.PlacementContext;
import r0se.manager.api.PlacementFailReason;

public class PlacementResult {
    public static final PlacementResult SUCCESS = new PlacementResult(true, PlacementFailReason.NONE, null, null);
    private final boolean success;
    private final PlacementFailReason failReason;
    private final PlacementContext context;
    private final ActionResult actionResult;

    private PlacementResult(boolean success, PlacementFailReason failReason, PlacementContext context, ActionResult actionResult) {
        this.success = success;
        this.failReason = failReason == null ? PlacementFailReason.NONE : failReason;
        this.context = context;
        this.actionResult = actionResult;
    }

    public static PlacementResult success(PlacementContext context, ActionResult actionResult) {
        return new PlacementResult(true, PlacementFailReason.NONE, context, actionResult);
    }

    public static PlacementResult fail(PlacementFailReason failReason) {
        return new PlacementResult(false, failReason, null, null);
    }

    public static PlacementResult fail(PlacementFailReason failReason, PlacementContext context, ActionResult actionResult) {
        return new PlacementResult(false, failReason, context, actionResult);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public PlacementFailReason getFailReason() {
        return this.failReason;
    }

    public PlacementContext getContext() {
        return this.context;
    }

    public ActionResult getActionResult() {
        return this.actionResult;
    }
}


