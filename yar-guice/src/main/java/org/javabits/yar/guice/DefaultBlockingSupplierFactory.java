package org.javabits.yar.guice;

import com.google.common.util.concurrent.ListenableFuture;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Id;
import org.javabits.yar.Registration;

import static org.javabits.yar.IdMatchers.newKeyMatcher;

/**
 * @author Romain Gilles
 *         Date: 5/31/13
 *         Time: 1:39 PM
 */
public class DefaultBlockingSupplierFactory implements BlockingSupplierFactory {
    @Override
    public <T> BlockingSupplier<T> create(SimpleRegistry registry, Id<T> id) {
        BlockingSupplierImpl<T> supplier = new BlockingSupplierImpl<>(id, registry.getDirectly(id));
        // If an instance of the requested service has been registered, this call will trigger the
        // listener's supplierChanged event with the current value of the service.
        // This is how the supplier instance obtains the initial value of the service.
        ListenableFuture<Registration<T>> registration = registry.addSupplierListener(newKeyMatcher(id), supplier);
        // preserve a reference to the registration to avoid gc and let the caller decides when listener can be gc.
        supplier.setSelfRegistration(registration);
        return supplier;

    }
}
