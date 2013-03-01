package org.yar.guice;

import org.yar.Supplier;
import org.yar.Watcher;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
* TODO comment
* Date: 2/28/13
* Time: 11:11 AM
*
* @author Romain Gilles
*/
abstract class AbstractBlockingSupplier<T> implements Supplier<T>, Watcher<Supplier<T>> {
    /**
     * Main lock guarding all access
     */
    final ReentrantLock lock;
    /**
     * Condition for waiting takes
     */
    final Condition notEmpty;

    Supplier<T> delegate;

    AbstractBlockingSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
        lock = new ReentrantLock();//TODO see if we propose the fair mode?
        notEmpty = lock.newCondition();
    }

    @Nullable
    @Override
    public final Supplier<T> add(Supplier<T> element) {
        requireNonNull(element, "element");
        lock.lock();
        try {
            if (delegate == null) {
                delegate = element;
                notEmpty.signal();
                return element;
            }
            return null;

        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void remove(Supplier<T> element) {
        lock.lock();
        try {
            delegate = null;
        } finally {
            lock.unlock();
        }
    }
}
