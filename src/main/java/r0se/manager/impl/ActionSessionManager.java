/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package r0se.manager.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class ActionSessionManager
implements Manager {
    private final AtomicLong nextId = new AtomicLong(1L);
    private final ThreadLocal<Deque<ActionSession>> sessions = ThreadLocal.withInitial(ArrayDeque::new);

    public ActionSession begin(String type, String module, BlockPos pos, Direction face) {
        ActionSession session = new ActionSession(this.nextId.getAndIncrement(), this.sanitize(type), this.sanitize(module), pos == null ? null : pos.toImmutable(), face, System.currentTimeMillis());
        this.sessions.get().push(session);
        this.debug("begin " + session.describe());
        return session;
    }

    public void end(ActionSession session, String result) {
        if (session == null) {
            return;
        }
        Deque<ActionSession> stack = this.sessions.get();
        if (!stack.isEmpty() && stack.peek() == session) {
            stack.pop();
        } else {
            stack.remove(session);
        }
        this.debug("end " + session.describe() + " result=" + this.sanitize(result) + " ageMs=" + (System.currentTimeMillis() - session.startedAt()));
    }

    public ActionSession current() {
        Deque<ActionSession> stack = this.sessions.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public String currentLabel() {
        ActionSession session = this.current();
        return session == null ? "none" : session.label();
    }

    private String sanitize(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private void debug(String message) {
        Managers.DEBUG.log("ActionSession", message);
    }

    public record ActionSession(long id, String type, String module, BlockPos pos, Direction face, long startedAt) {
        public String label() {
            return "#" + this.id + "/" + this.type + "/" + this.module;
        }

        public String describe() {
            return this.label() + " pos=" + (this.pos == null ? "null" : this.pos.toShortString()) + " face=" + String.valueOf(this.face);
        }
    }
}


