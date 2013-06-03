package org.javabits.yar.guice;

import java.util.EventListener;

/**
 * TODO comment
 * Date: 5/30/13
 * Time: 9:27 AM
 *
 * @author Romain Gilles
 */
interface KeyListener<T> extends EventListener {
    void keyAdded(KeyEvent<T> event);
    void keyRemoved(KeyEvent<T> event);
}
