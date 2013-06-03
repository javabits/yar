package org.javabits.yar.guice;

import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Id;

/**
 * @author Romain Gilles
 *         Date: 5/31/13
 *         Time: 1:39 PM
 */
public class NoWaitBlockingSupplierFactory implements BlockingSupplierFactory {
    @Override
    public <T> BlockingSupplier<T> create(SimpleRegistry registry, Id<T> id) {
        return new NoWaitBlockingSupplier<T>(registry.getDirectly(id));
    }
}
