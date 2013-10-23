package org.javabits.yar.guice;

import org.javabits.yar.Id;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: rogilles
 * Date: 10/22/13
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Concurrents {
    private static final Logger LOG = Logger.getLogger(Concurrents.class.getName());

    private Concurrents() {
        throw new AssertionError("Not for you!");
    }

    public static void executeWithLog(Callable<Void> callable, Id<?> id, String message) {
        try {
            callable.call();
        } catch (CancellationException e) {
            LOG.log(Level.SEVERE, String.format("%s canceled: %s", message, id), e);
        } catch (ExecutionException | RuntimeException e) {
            LOG.log(Level.SEVERE, String.format("%s error: %s", message, id), e);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, String.format("%s interrupted: %s", message, id), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("%s exception: %s", message, id), e);
        }
    }
}
