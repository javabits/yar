package org.javabits.yar.guice;

import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Id;

/**
 * @author Romain Gilles
 *         Date: 5/31/13
 *         Time: 1:38 PM
 */
public interface BlockingSupplierFactory {
    BlockingSupplierFactory DEFAULT_BLOCKING_SUPPLIER = new DefaultBlockingSupplierFactory();

    <T> BlockingSupplier<T> create(InternalRegistry registry, Id<T> id);
}
