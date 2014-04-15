package org.javabits.yar.guice;

import org.javabits.yar.Ids;
import org.javabits.yar.Supplier;
import org.javabits.yar.SupplierEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.javabits.yar.Registry.DEFAULT_TIMEOUT;
import static org.javabits.yar.Registry.DEFAULT_TIME_UNIT;
import static org.javabits.yar.SupplierEvent.Type.ADD;

/**
 * @author Romain Gilles
 *         Date: 5/23/13
 *         Time: 8:49 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class BlockingSupplierImplTest {
    @Mock
    private Supplier<MyInterface> supplier;

    /**
     * This test try to reproduce the construction of a BlockingSupplier within
     * the {@link BlockingSupplierRegistryImpl#get(org.javabits.yar.Id)} method.
     * It can arrived that the registry already contains the requested supplier
     * and therefore the BlockingSupplier will be created with existing supplier
     * and then added to the when the watcher will be handled by the reactor queue.
     */@Test
    public void testCreateWithValueAndAdd() {
        // given a supplier at construction time
        BlockingSupplierImpl<MyInterface> blockingSupplier = new BlockingSupplierImpl<>(Ids.newId(MyInterface.class), supplier, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
        // when trigger supplier changed by the reactor
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier));
        //then now exception must be throw
    }
}
