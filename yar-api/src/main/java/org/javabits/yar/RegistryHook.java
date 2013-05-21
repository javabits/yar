package org.javabits.yar;

import java.lang.reflect.Type;

/**
 * This class provides methods for frameworks that need specific actions on the registry
 * like invalidate all ids associated to a specific type. This action can be interesting
 * when dynamic class loading is used.
 * The {@link Registry} does not directly extends this interface.
 * Framework that need this kind of service have to cast the current instance of the registry
 * they handle to use those features.
 * <pre>
 *     Type type = ...;
 *     Registry myRegistry = ...;
 *     if (myRegistry instanceof RegistryHook) {
 *         ((RegistryHook)myRegistry).removeAll(type);
 *     }
 * </pre>
 * Date: 5/20/13
 * Time: 6:08 PM
 *
 * @author Romain Gilles
 */
public interface RegistryHook {
    /**
     * Removes all the entries whose the {@link Id} is associated to provided type.
     * It first recursively remove all the supplier associated to the given type
     * and then remove all watcher associated to the given type.
     *
     * @param type the type whose the corresponding entries will be removed.
     */
    void removeAll(Type type);
}
