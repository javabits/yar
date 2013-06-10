package org.javabits.yar;

import java.util.EventObject;

/**
 * TODO comment
 * Date: 5/29/13
 * Time: 10:33 PM
 *
 * @author Romain Gilles
 */
public class TypeEvent extends EventObject {

    public enum Type {
        ADDED, REMOVED
    }
    private final Type eventType;
    private final java.lang.reflect.Type type;
    /**
     * Constructs a prototypical Event.
     *
     * @param type The object on which the Event initially occurred.
     * @throws IllegalArgumentException
     *          if source is null.
     */
    private TypeEvent(Type eventType, java.lang.reflect.Type type) {
        super(type);
        this.eventType = eventType;
        this.type = type;
    }

    public java.lang.reflect.Type type() {
        return type;
    }

    public Type eventType() {
        return eventType;
    }

    public static TypeEvent newAddTypeEvent(java.lang.reflect.Type type) {
        return new TypeEvent(Type.ADDED, type);
    }

    public static TypeEvent newRemoveTypeEvent(java.lang.reflect.Type type) {
        return new TypeEvent(Type.REMOVED, type);
    }

    @Override
    public String toString() {
        return "TypeEvent{" +
                "eventType=" + eventType +
                ", type=" + type +
                '}';
    }
}
