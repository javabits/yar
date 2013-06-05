package org.javabits.yar.guice;

import com.google.common.base.Supplier;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.javabits.yar.*;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
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
        final Id<MyService> id = GuiceId.of(MyService.class);
        Registration<MyService> myServiceRegistration = registry.put(id, new Supplier<MyService>() {
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
                return id;
            }
        }, watcher
        );
        assertThat(myServiceRegistration, is(not(nullValue())));
        assertThat(myServiceWatcherRegistration, is(not(nullValue())));
        assertThat(watcher.counter.get(), is(1));
        assertThat(registry.ids(), hasItem(id));
        registryHook.invalidate(MyService.class);
        assertThat(watcher.counter.get(), is(0));
        assertThat(registry.ids(), not(hasItem(id)));
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
        public org.javabits.yar.Supplier<MyService> add(org.javabits.yar.Supplier<MyService> element) {
            counter.incrementAndGet();
            return element;
        }

        @Override
        public void remove(org.javabits.yar.Supplier<MyService> element) {
            counter.decrementAndGet();
        }
    }

    @Test
    public void testTypeListener() throws Exception {
        final int[] count = {0};
        registryHook.addTypeListener(new TypeListener() {
            @Override
            public void typeChanged(TypeEvent typeEvent) {
                synchronized (count) {
                    switch (typeEvent.eventType()) {
                        case ADDED:
                            count[0] = count[0] + 1;
                            break;
                        case REMOVED:
                            count[0] = count[0] - 1;
                            break;
                        default:
                            throw new UnsupportedOperationException("un supported event type: " + typeEvent);
                    }
                }
            }
        });
        //when register first service
        Registration<MyService> serviceRegistration = registry.put(GuiceId.of(MyService.class), new Supplier<MyService>() {
            @Override
            public MyService get() {
                return new MyServiceImpl();
            }
        });
        // then type count must be 1
        synchronized (count) {
            assertThat(count[0], is(1));
        }
        // when register second service on the same type
        Registration<MyService> serviceRegistration2 = registry.put(GuiceId.of(Key.get(MyService.class, Names.named("test"))), new Supplier<MyService>() {
            @Override
            public MyService get() {
                return new MyServiceImpl();
            }
        });
        // then type count must still be 1
        synchronized (count) {
            assertThat(count[0], is(1));
        }
        // when remove first service registration
        registry.remove(serviceRegistration);
        // then type count must be 1 because the second service registration is still in the registry
        synchronized (count) {
            assertThat(count[0], is(1));
        }
        // when remove the second service registration
        registry.remove(serviceRegistration2);
        // then count must be 1 because not invalidated
        synchronized (count) {
            assertThat(count[0], is(1));
        }
        // when invalidate the type MyService
        assertThat(registry.getAll(MyService.class), org.hamcrest.Matchers.<org.javabits.yar.Supplier<MyService>>iterableWithSize(0));
        registryHook.invalidate(MyService.class);
        // then count must be 0
        synchronized (count) {
            assertThat(count[0], is(0));
        }
        // when re-register a service on type MyService
        registry.put(GuiceId.of(Key.get(MyService.class, Names.named("test"))), new Supplier<MyService>() {
            @Override
            public MyService get() {
                return new MyServiceImpl();
            }
        });
        // then count must be 1 again
        synchronized (count) {
            assertThat(count[0], is(1));
        }
    }
}
