/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.collection;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;

public final class FirstOutQueue<E>
implements Iterable<E> {
    private final ArrayDeque<E> delegate;
    private final int maxSize;

    public FirstOutQueue(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must be >= 0");
        }
        this.maxSize = maxSize;
        this.delegate = new ArrayDeque(maxSize);
    }

    public E addFirst(E element) {
        if (element == null || this.maxSize == 0) {
            return null;
        }
        E removed = null;
        if (this.delegate.size() == this.maxSize) {
            removed = this.delegate.removeFirst();
        }
        this.delegate.addFirst(element);
        return removed;
    }

    public boolean add(E element) {
        if (element == null || this.maxSize == 0) {
            return false;
        }
        if (this.delegate.size() == this.maxSize) {
            this.delegate.removeFirst();
        }
        this.delegate.addLast(element);
        return true;
    }

    public boolean remove(E element) {
        return this.delegate.remove(element);
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public int size() {
        return this.delegate.size();
    }

    public int maxSize() {
        return this.maxSize;
    }

    public void clear() {
        this.delegate.clear();
    }

    public E peek() {
        return this.delegate.peekFirst();
    }

    public E getFirst() {
        return this.delegate.getFirst();
    }

    public E getLast() {
        return this.delegate.getLast();
    }

    public boolean contains(E element) {
        return this.delegate.contains(element);
    }

    public Collection<E> values() {
        return this.delegate;
    }

    @Override
    public Iterator<E> iterator() {
        return this.delegate.iterator();
    }
}

