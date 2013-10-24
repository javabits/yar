package org.javabits.yar;

import java.lang.reflect.Type;
import java.util.Collection;

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
 *         ((RegistryHook)myRegistry).invalidate(type);
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
    void invalidate(Type type);

    void invalidateAll(Collection<Type> types);

    void addTypeListener(TypeListener typeListener);

    void removeTypeListener(TypeListener typeListener);

    /**
     * Returns {@code true} if this execution strategy contains no pending task.
     * This information in a lightly concurrent environment is really not relevant.
     * It can be used at startup time when the system is not under evy stress.
     *
     * @return {@code true} if at this point of time there is no pending task.
     */
    boolean hasPendingListenerUpdateTasks();

    /**
     * Add a listener to the pending {@code Watcher} / {@code SupplierListener} update tasks list.
     * It tack a snapshot of the pending update task then call the
     * {@link org.javabits.yar.RegistryHook.EndOfListenerUpdateTasksListener#completed() completed()}
     * method when all the pending task are achieved. Regarding the execution strategy you choose then
     * this action can be call in different order.
     *
     * @param listener the listener whose the {@code complete} method has to be call when the snapshot
     *                 of the current pending task is completed.
     */
    void addEndOfListenerUpdateTasksListener(EndOfListenerUpdateTasksListener listener);

    interface EndOfListenerUpdateTasksListener {

        void completed();
    }
}
