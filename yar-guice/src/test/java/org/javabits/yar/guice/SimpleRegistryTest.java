package org.javabits.yar.guice;

import com.google.common.base.Supplier;
import org.javabits.yar.Id;
import org.javabits.yar.Registry;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.javabits.yar.guice.SimpleRegistry.newLoadingCacheRegistry;

/**
 * TODO comment
 * Date: 5/3/13
 *
 * @author Romain Gilles
 */
public class SimpleRegistryTest {
    Registry registry;

    @Before
    public void setUp() throws Exception {
        registry = newLoadingCacheRegistry();
    }

    @Test
    public void testPutIdGuavaSupplier() {
        Id<String> id = GuiceId.of(String.class);
        assertThat(registry.get(id), is(nullValue()));
        registry.put(id, new Supplier<String>() {
            @Override
            public String get() {
                return "test";
            }
        });
        assertThat(registry.get(id), is(not(nullValue())));
        assertThat(registry.get(id).get(), is("test"));
    }

    @Test
    public void testPutIdYarSupplier() {
        final Id<String> id = GuiceId.of(String.class);
        assertThat(registry.get(id), is(nullValue()));
        registry.put(id, new org.javabits.yar.Supplier<String>() {
            @Override
            public Id<String> id() {
                return id;
            }

            @Override
            public String get() {
                return "test";
            }
        });
        assertThat(registry.get(id), is(not(nullValue())));
        assertThat(registry.get(id).get(), is("test"));
    }

    @Test
    public void testPutIdGuiceSupplier() {
        final Id<String> id = GuiceId.of(String.class);
        assertThat(registry.get(id), is(nullValue()));

        registry.put(id, GuiceSupplier.of(id, new Provider<String>() {
            @Override
            public String get() {
                return "test";
            }
        }));
        assertThat(registry.get(id), is(not(nullValue())));
        assertThat(registry.get(id).get(), is("test"));
    }
}
