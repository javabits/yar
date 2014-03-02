package org.javabits.yar.guice.osgi.internal;

import org.javabits.yar.RegistryHook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author Romain Gilles
 */
@RunWith(MockitoJUnitRunner.class)
public class BundleTypeCleanerTest {
    @Mock
    RegistryHook registryHook;

    @Test
    public void testConstructorRegistration() {
        //given registryHook
        //when
        BundleTypeCleaner bundleTypeCleaner = new BundleTypeCleaner(registryHook);
        //then
        verify(registryHook).addTypeListener(bundleTypeCleaner);
    }
}
