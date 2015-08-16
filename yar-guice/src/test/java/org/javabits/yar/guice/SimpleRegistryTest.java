package org.javabits.yar.guice;

import java.util.Collections;
import java.util.function.Supplier;
import com.google.common.reflect.TypeToken;
import org.javabits.yar.Id;
import org.javabits.yar.Ids;
import org.javabits.yar.Registry;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.javabits.yar.guice.SimpleRegistry.newLoadingCacheRegistry;

/**
 * TODO comment
 * Date: 5/3/13
 *
 * @author Romain Gilles
 */
@SuppressWarnings("ConstantConditions")
public class SimpleRegistryTest {
    private Registry registry;

    @Before
    public void setUp() throws Exception {
        registry = newLoadingCacheRegistry();
    }

    @Test
    public void testPutIdGuavaSupplier() {
        Id<String> id = Ids.newId(String.class);
        assertThat(registry.get(id), is(nullValue()));
        registry.put(id, () -> "test");
        assertThat(registry.get(id), is(not(nullValue())));
        assertThat(registry.get(id).get(), is("test"));
    }

    @Test
    public void testPutIdYarSupplier() {
        final Id<String> id = Ids.newId(String.class);
        assertThat(registry.get(id), is(nullValue()));
        registry.put(id, () -> "test");
        assertThat(registry.get(id), is(not(nullValue())));
        assertThat(registry.get(id).get(), is("test"));
    }

    @Test
    public void testPutIdGuiceSupplier() {
        final Id<String> id = Ids.newId(String.class);
        assertThat(registry.get(id), is(nullValue()));

        registry.put(id, GuiceSupplier.of(() -> "test"));
        assertThat(registry.get(id), is(not(nullValue())));
        assertThat(registry.get(id).get(), is("test"));
    }

    @Test(expected = NullPointerException.class) @SuppressWarnings("unchecked")
    public void testGetClassNullPointerException() throws Exception {
        registry.get((Class) null);
    }

    @Test(expected = NullPointerException.class) @SuppressWarnings("unchecked")
    public void testGetAllClassNullPointerException() throws Exception {
        registry.getAll((Class) null);
    }

    @Test(expected = NullPointerException.class) @SuppressWarnings("unchecked")
    public void testGetIdNullPointerException() throws Exception {
        registry.get((Id) null);
    }

    @Test(expected = NullPointerException.class) @SuppressWarnings("unchecked")
    public void testGetAllIdNullPointerException() throws Exception {
        registry.getAll((Id) null);
    }

    @Test
    public void testGetTypeToken() {
        final TypeToken<List<String>> type = new TypeToken<List<String>>() {
        };
        final Id<List<String>> id = GuiceId.of(type.getType(), null);
        assertThat(registry.get(id), is(nullValue()));

        registry.put(id, GuiceSupplier.of(Collections::emptyList));
        assertThat(registry.get(type), is(not(nullValue())));
        assertThat(registry.get(type).get(), is((List) emptyList()));
    }

    @Test(expected = NullPointerException.class) @SuppressWarnings("unchecked")
    public void testGetTypeTokenNullPointerException() throws Exception {
        registry.get((TypeToken) null);
    }

    @Test
    public void testGetAllTypeToken() {
        final TypeToken<String> type = new TypeToken<String>() {
        };
        final Id<String> id = GuiceId.of(type.getType(), null);
        assertThat(registry.get(id), is(nullValue()));

        registry.put(id, GuiceSupplier.of(() -> "test"));
        assertThat(registry.getAll(type), is(not(nullValue())));
        assertThat(registry.getAll(type).size(), is(1));
        assertThat(registry.getAll(type).get(0).get(), is("test"));
    }

    @Test(expected = NullPointerException.class) @SuppressWarnings("unchecked")
    public void testGetAllTypeTokenNullPointerException() throws Exception {
        registry.getAll((TypeToken) null);
    }

}
