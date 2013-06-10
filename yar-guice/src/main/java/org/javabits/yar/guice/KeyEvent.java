package org.javabits.yar.guice;

import java.lang.reflect.Type;
import java.util.EventObject;

/**
 * TODO comment
 * Date: 5/30/13
 * Time: 9:29 AM
 *
 * @author Romain Gilles
 */
public class KeyEvent<T> extends EventObject {

    private final T key;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException
     *          if source is null.
     */
    private KeyEvent(T source) {
        super(source);
        key = source;
    }

    public T key() {
        return key;
    }

    public static <T> KeyEvent<T> newKeyEvent(T key) {
        return new KeyEvent<>(key);
    }
}
