package org.javabits.yar.guice;

import java.util.function.Supplier;
import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Id;
import org.javabits.yar.Ids;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TODO comment
 * Date: 5/21/13
 * Time: 1:30 PM
 *
 * @author Romain Gilles
 */
public class DynamicCollectionsBindingTest {
    @Test
    public void testBindListToRegistry() {
        //given
        BlockingSupplierRegistry blockingSupplierRegistry = YarGuices.newLoadingCacheBlockingSupplierRegistry();
        Module registryDeclarationModule = YarGuices.newRegistryDeclarationModule(blockingSupplierRegistry);
        final TypeLiteral<List<MyInterface>> listTypeLiteral = new TypeLiteral<List<MyInterface>>() {
        };
        final Key<List<MyInterface>> listKey2 = Key.get(listTypeLiteral, Names.named("test"));
        Injector injector = Guice.createInjector(registryDeclarationModule, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(listTypeLiteral).toRegistry();
                bind(listKey2).toRegistry();
            }
        });
        Key<List<MyInterface>> listKey = Key.get(listTypeLiteral);
        Iterable<MyInterface> myInterfaceList = injector.getInstance(listKey);
        Iterable<MyInterface> myInterfaceList2 = injector.getInstance(listKey2);
        assertThat(myInterfaceList, is(not(nullValue())));
        assertThat(myInterfaceList, is(emptyIterable()));
        assertThat(myInterfaceList2, is(emptyIterable()));
        //when
        final MyInterfaceImpl myImpl = new MyInterfaceImpl();
        final Id<MyInterface> id = Ids.newId(MyInterface.class);
        blockingSupplierRegistry.put(id, new Supplier<MyInterface>() {
            @Nullable
            @Override
            public MyInterface get() {
                return myImpl;
            }
        });
        final MyInterfaceImpl myImpl2 = new MyInterfaceImpl();
        blockingSupplierRegistry.put(id, new Supplier<MyInterface>() {
            @Nullable
            @Override
            public MyInterface get() {
                return myImpl2;
            }
        });

        //then
        assertThat(myInterfaceList, hasItem(myImpl));
        assertThat(myInterfaceList, hasItem(myImpl2));
        assertThat(myInterfaceList2, is(emptyIterable()));
        myInterfaceList2 = injector.getInstance(listKey);
        assertThat(myInterfaceList2, hasItem(myImpl2));
    }


}
