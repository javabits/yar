package org.javabits.yar.guice;

import org.javabits.yar.*;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TODO comment
 * Date: 5/21/13
 * Time: 8:57 AM
 *
 * @author Romain Gilles
 */
public class RegistryHookTest {
    private RegistryHook registryHook;
    private Registry registry;

    @Before
    public void setUp() throws Exception {
        registry = YarGuices.newLoadingCacheBlockingSupplierRegistry();
        if (registry instanceof RegistryHook) {
            registryHook = (RegistryHook) registry;
        } else {
            throw new AssertionError("YarGuices.newLoadingCacheBlockingSupplierRegistry() must return a registry that implements RegistryHook");
        }
    }

    @Test
    public void testInvalidate() {
        Registration<MyService> myServiceRegistration = registry.put(GuiceId.of(MyService.class), new Supplier<MyService>() {
            @Nullable
            @Override
            public MyService get() {
                return new MyServiceImpl();
            }
        });
        MyServiceWatcher watcher = new MyServiceWatcher();
        Registration<MyService> myServiceWatcherRegistration = registry.addWatcher(new IdMatcher<MyService>() {
            @Override
            public boolean matches(Id<MyService> otherId) {
                return MyService.class.equals(Reflections.getRawType(otherId.type()));
            }

            @Override
            public Id<MyService> id() {
                return GuiceId.of(MyService.class);
            }
        }, watcher
        );
        assertThat(myServiceRegistration, is(not(nullValue())));
        assertThat(myServiceWatcherRegistration, is(not(nullValue())));
        assertThat(watcher.counter.get(), is(1));
        assertThat(registry.ids(), hasItem(GuiceId.of(MyService.class)));
        registryHook.removeAll(MyService.class);
        assertThat(watcher.counter.get(), is(0));
        assertThat(registry.ids(), not(hasItem(GuiceId.of(MyService.class))));
        registry.remove(myServiceRegistration);
        registry.removeWatcher(myServiceWatcherRegistration);
    }

    static interface MyService {
    }

    static class MyServiceImpl implements MyService {
    }

    private static class MyServiceWatcher implements Watcher<MyService> {
        final AtomicInteger counter = new AtomicInteger(0);

        @Nullable
        @Override
        public Supplier<MyService> add(Supplier<MyService> element) {
            counter.incrementAndGet();
            return element;
        }

        @Override
        public void remove(Supplier<MyService> element) {
            counter.decrementAndGet();
        }
    }
}
