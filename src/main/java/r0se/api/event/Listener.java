/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import r0se.api.event.Subscribe;

public class Listener {
    private final Class<?> subscriber;
    private final Object owner;
    private Consumer<Object> consumer;
    private final int priority;

    public Listener(Class<?> klass, Object object, Method method) {
        this.owner = object;
        this.subscriber = method.getParameters()[0].getType();
        this.priority = method.getAnnotation(Subscribe.class).priority();
        try {
            method.setAccessible(true);
            this.consumer = event -> {
                try {
                    method.invoke(this.owner, event);
                }
                catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Object getOwner() {
        return this.owner;
    }

    public Class<?> getSubscriber() {
        return this.subscriber;
    }

    public int getPriority() {
        return this.priority;
    }

    public void invoke(Object event) {
        this.consumer.accept(event);
    }
}
