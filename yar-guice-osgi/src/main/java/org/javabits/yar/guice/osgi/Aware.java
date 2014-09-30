package org.javabits.yar.guice.osgi;

/**
 * Root interface for all XxxAware interface.
 * This interface is used as a marker as Serializable one.
 * <p>The sub interfaces of this one are used by implementation to indicate that they are
 * interesting into the context of bundle registry that request a supplier value.
 * For example, the instance returned by a given supplier may want to know in which
 * bundle it as been get. So that, it can play with this bundle or its associated registry.
 * </p>
 * <p>Implementation must be non scoped object. More formally,
 * implementation must be created at each time.
 * </p>
 */
public interface Aware {
}
