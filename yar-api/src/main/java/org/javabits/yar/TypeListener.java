package org.javabits.yar;

import java.util.EventListener;

/**
 * TODO comment
 * Date: 5/29/13
 * Time: 10:31 PM
 *
 * @author Romain Gilles
 */
public interface TypeListener extends EventListener {
    void typeChanged(TypeEvent typeEvent);
}
