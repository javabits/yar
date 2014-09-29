/*
 * Copyright (c) 9/29/14 9:34 AM Romain Gilles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javabits.yar.guice.osgi;

import static com.google.common.base.Suppliers.ofInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.javabits.yar.guice.YarGuices.builder;

import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Id;
import org.javabits.yar.Ids;
import org.javabits.yar.guice.ExecutionStrategy;
import org.javabits.yar.guice.YarGuices;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;

import com.google.common.base.Supplier;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BundleRegistryTest {
    public static final Id<MyInterface> ID = Ids.newId(MyInterface.class);
    public static final Supplier INSTANCE_SUPPLIER = ofInstance(new MyInterface() {});
    @Mock
    private Bundle bundle;

    private BlockingSupplierRegistry blockingSupplierRegistry;
    private BundleRegistry registry;

    @Before
    public void setup() {
        YarGuices.Builder builder = builder();
        builder.listenerUpdateExecutionStrategy(ExecutionStrategy.Type.SAME_THREAD);
        blockingSupplierRegistry = builder.build();
        registry = new BundleRegistry(blockingSupplierRegistry, bundle);

    }

    @Test
    public void testGetNativeSupplier() {
        registry.put(ID, INSTANCE_SUPPLIER);
        OSGiSupplier<MyInterface> supplier = registry.get(ID);
        assertThat(supplier.getNativeSupplier(), is(INSTANCE_SUPPLIER));
    }

    @Test
    public void testGetBundle() {
        registry.put(ID, INSTANCE_SUPPLIER);
        OSGiSupplier<MyInterface> supplier = registry.get(ID);
        assertThat(supplier.getBundle(), is(bundle));
    }


    @Test
    public void testGetAllWithBundle() {
        registry.put(ID, INSTANCE_SUPPLIER);
        List<org.javabits.yar.Supplier<MyInterface>> suppliers = registry.getAll(ID);
        assertThat(((BundleSupplier)suppliers.get(0)).getBundle(), is(bundle));
    }

    @Test
    public void testGetAllWithoutBundle() {
        blockingSupplierRegistry.put(ID, INSTANCE_SUPPLIER);
        List<org.javabits.yar.Supplier<MyInterface>> suppliers = registry.getAll(ID);
        assertThat(((BundleSupplier)suppliers.get(0)).getBundle(), is(nullValue()));
    }

    static interface MyInterface {
    }
}
