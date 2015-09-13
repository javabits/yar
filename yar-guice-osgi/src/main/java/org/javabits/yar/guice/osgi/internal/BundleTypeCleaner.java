package org.javabits.yar.guice.osgi.internal;

import org.javabits.yar.RegistryHook;
import org.javabits.yar.TypeEvent;
import org.javabits.yar.TypeListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleReference;
import org.osgi.framework.SynchronousBundleListener;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import static org.javabits.yar.guice.Reflections.getRawType;

/**
 * This class is responsible to cleanup all the types from a specific bundle when this one is removed.
 * It is avoid memory leak on ClassLoader.
 * Date: 6/3/13
 *
 * @author Romain Gilles
 */
class BundleTypeCleaner implements SynchronousBundleListener, TypeListener {
    private static final Logger LOG = Logger.getLogger(BundleTypeCleaner.class.getName());
    private final RegistryHook registryHook;
    private final Map<Long, Set<Type>> cache = new ConcurrentHashMap<>();

    BundleTypeCleaner(RegistryHook registryHook) {
        this.registryHook = registryHook;
        registryHook.addTypeListener(this);
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STOPPING:
                long bundleId = event.getBundle().getBundleId();
                Set<Type> types = cache.remove(bundleId);
                if (types != null && !types.isEmpty()) {
                    registryHook.invalidateAll(types);
                }
                break;
            default:
                //nothing to do
        }
    }

    @Override
    public void typeChanged(TypeEvent typeEvent) {
        switch (typeEvent.eventType()) {
            case ADDED:
                Type type = typeEvent.type();
                Class<?> rawType = getRawType(type);
                ClassLoader classLoader = rawType.getClassLoader();
                if (classLoader instanceof BundleReference) {
                    Bundle bundle = ((BundleReference) classLoader).getBundle();
                    cache.computeIfAbsent(bundle.getBundleId(), key -> new CopyOnWriteArraySet<>()).add(type);
                } else {
                    LOG.warning(type + "'s class loader is not a BundleReference");
                }
                break;
            case REMOVED:
            default:
                // nothing to do
        }
    }
}
