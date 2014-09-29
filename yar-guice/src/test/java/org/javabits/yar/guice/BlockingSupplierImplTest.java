package org.javabits.yar.guice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javabits.yar.SupplierEvent.Type.ADD;
import static org.javabits.yar.SupplierEvent.Type.REMOVE;

import org.javabits.yar.Id;
import org.javabits.yar.Ids;
import org.javabits.yar.Supplier;
import org.javabits.yar.SupplierEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Romain Gilles Date: 5/23/13 Time: 8:49 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class BlockingSupplierImplTest {
    public static final Id<MyInterface> ID = Ids.newId(MyInterface.class);
    @Mock
    private Supplier<MyInterface> supplier1;
    @Mock
    private Supplier<MyInterface> supplier2;
    @Mock
    private InternalRegistry registry;
    /**
     * This test try to reproduce the construction of a BlockingSupplier within the
     * {@link BlockingSupplierRegistryImpl#get(org.javabits.yar.Id)} method. It can arrived that the
     * registry already contains the requested supplier and therefore the BlockingSupplier will be
     * created with existing supplier and then added to the when the watcher will be handled by the
     * reactor queue.
     */
    @Test
    public void testCreateWithValueAndAdd() {
        // given a supplier at construction time
        Mockito.when(registry.getDirectly(ID)).thenReturn(supplier1);
        BlockingSupplierImpl<MyInterface> blockingSupplier = newBlockingSupplier();
        // when trigger supplier changed by the reactor
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier2));
        // then now exception must be throw
    }

    @Test
    public void testCreateWithoutAndAdd() {
        // given a null supplier
        BlockingSupplierImpl<MyInterface> blockingSupplier = newBlockingSupplier();
        // when adding a new supplier
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier1));
        // then this added supplier must be returned
        assertThat(getNativeSupplier(blockingSupplier), is(supplier1));
    }

    private Supplier<MyInterface> getNativeSupplier(BlockingSupplierImpl<MyInterface> blockingSupplier) {
        return (Supplier<MyInterface>) blockingSupplier.getNativeSupplier();
    }

    @Test
    public void testCreateWithoutAndAdd2Times() {
        // given a null supplier
        BlockingSupplierImpl<MyInterface> blockingSupplier = newBlockingSupplier();
        // when adding a 2 new suppliers
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier1));
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier2));
        // then the first supplier must be returned
        assertThat(getNativeSupplier(blockingSupplier), is(supplier1));
    }

    @Test
    public void testCreateWithoutAndAddRemoveAdd() {
        // given a null supplier
        BlockingSupplierImpl<MyInterface> blockingSupplier = newBlockingSupplier();
        // when add a supplier then remove it and add a second suppliers
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier1));
        blockingSupplier.supplierChanged(new SupplierEvent(REMOVE, supplier1));
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier2));
        // then the 2s supplier must be returned
        assertThat(getNativeSupplier(blockingSupplier), is(supplier2));
    }

    @Test
    public void testCreateWithoutAndAdd2TimesRemoveLastOne() {
        // given a null supplier
        BlockingSupplierImpl<MyInterface> blockingSupplier = newBlockingSupplier();
        // when adding a 2 new suppliers and remove the second one
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier1));
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier2));
        blockingSupplier.supplierChanged(new SupplierEvent(REMOVE, supplier2));
        // then the 1st supplier must be returned
        assertThat(getNativeSupplier(blockingSupplier), is(supplier1));
    }

    @Test
    public void testCreateWithoutAndRemoveAndAdd() {
        // given a null supplier
        BlockingSupplierImpl<MyInterface> blockingSupplier = newBlockingSupplier();
        // when adding a 2 new suppliers and remove the second one
        blockingSupplier.supplierChanged(new SupplierEvent(REMOVE, supplier2));
        blockingSupplier.supplierChanged(new SupplierEvent(ADD, supplier1));
        // then the 1st supplier must be returned
        assertThat(getNativeSupplier(blockingSupplier), is(supplier1));
    }

    private BlockingSupplierImpl<MyInterface> newBlockingSupplier() {
        return new BlockingSupplierImpl<>(ID, registry);
    }
}
