package org.javabits.yar.guice;

import com.google.common.base.Supplier;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.javabits.yar.*;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Romain Gilles
 */
public class RegistryHookTest {
    private RegistryHook registryHook;
    private Registry registry;
    private final int[] count = new int[]{0};

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
    public void testInvalidate() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final Id<MyService> id = Ids.newId(MyService.class);
        Registration<MyService> myServiceRegistration = registry.put(id, new Supplier<MyService>() {
            @Nullable
            @Override
            public MyService get() {
                return new MyServiceImpl();
            }
        });
        MyServiceWatcher watcher = new MyServiceWatcher(barrier);
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
        barrier.await(5, MILLISECONDS);
        barrier.reset();
        assertThat(watcher.counter.get(), is(1));
        assertThat(registry.ids(), hasItem(id));
        registryHook.invalidate(MyService.class);
        barrier.await(5, MILLISECONDS);
        barrier.reset();
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
        final CyclicBarrier barrier;

        private MyServiceWatcher(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Nullable
        @Override
        public org.javabits.yar.Supplier<MyService> add(org.javabits.yar.Supplier<MyService> element) {
            counter.incrementAndGet();
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            return element;
        }

        @Override
        public void remove(org.javabits.yar.Supplier<MyService> element) {
            counter.decrementAndGet();
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testTypeListener() throws Exception {
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
        Registration<MyService> serviceRegistration = registry.put(Ids.newId(MyService.class), new Supplier<MyService>() {
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
        Registration<MyService> serviceRegistration2 = registry.put(Ids.newId(MyService.class, Names.named("test")), new Supplier<MyService>() {
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

    @Test
    public void testHasPendingListenerUpdateTasks() throws Exception {
        Id<String> stringId = Ids.newId(String.class);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        registry.put(stringId, new Supplier<String>() {
            @Override
            public String get() {
                return "test";
            }
        });
        registry.addWatcher(IdMatchers.newKeyMatcher(stringId), new Watcher<String>() {
            @Nullable
            @Override
            public org.javabits.yar.Supplier<String> add(org.javabits.yar.Supplier<String> element) {
                try {
                    assertThat(countDownLatch.await(10, SECONDS), is(true));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void remove(org.javabits.yar.Supplier<String> element) {
                //nothing to do ;)
            }
        });

        assertThat(registryHook.hasPendingListenerUpdateTasks(), is(true));
        countDownLatch.countDown();
        final CountDownLatch endOfTaskBarrier = new CountDownLatch(1);
        registryHook.addEndOfListenerUpdateTasksListener(new RegistryHook.EndOfListenerUpdateTasksListener() {
            @Override
            public void completed() {
                endOfTaskBarrier.countDown();
            }
        });
        countDownLatch.countDown();
        assertThat(endOfTaskBarrier.await(5, MILLISECONDS), is(true));
    }
}
