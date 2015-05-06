package org.javabits.yar;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;

/**
 * Utility methods to manipulate Annotations.
 * <p/>
 * <p><b>Note:</b>
 * This class reuses implementation from Guice internal {@code Annotations} class.
 * As Yar is intended to be used in OSGi environment we cannot access to this class
 * therefore we are obliged to duplicate sub part. So, thanks to Guice for those features.
 * </p>
 * Date: 6/24/13
 * Time: 10:19 PM
 *
 * @author Romain Gilles
 */
public final class Annotations {

    private static final String RUNTIME_RETENTION_ERROR = "%s is not retained at runtime. Please annotate it with @Retention(RUNTIME).";

    private Annotations() {
        throw new AssertionError("Not for you!");
    }

    /**
     * Returns the given {@code annotationClass} parameter if it's retained at runtime otherwise throw an {@code }
     *
     * @param annotationClass the not {@code null} annotation type whose the runtime retention has to be validated.
     * @param message         optional message used if the annotationClass is {@code null}
     *                        otherwise "annotationClass" is used as a message.
     * @return the given {@code annotationClass} parameter if it's retained at runtime otherwise throw
     *         an {@code IllegalArgumentException}
     * @see #isRetainedAtRuntime(Class)
     */
    public static Class<? extends Annotation> checkRuntimeRetention(Class<? extends Annotation> annotationClass, @Nullable String message) {
        requireNonNull(annotationClass, firstNonNull(message, "annotationClass"));
        checkArgument(isRetainedAtRuntime(annotationClass), RUNTIME_RETENTION_ERROR, annotationClass.getName());
        return annotationClass;
    }

    /**
     * Returns {@code true} if the given annotation type is marked has retained at runtime.
     *
     * @see Retention
     * @see RetentionPolicy#RUNTIME
     */
    public static boolean isRetainedAtRuntime(Class<? extends Annotation> annotationClass) {
        Retention retention = requireNonNull(annotationClass, "annotationClass").getAnnotation(Retention.class);
        return retention != null && RUNTIME == retention.value();
    }

    /**
     * Returns {@code true} if the given annotation type has no attributes.
     */
    public static boolean isMarker(Class<? extends Annotation> annotationType) {
        return annotationType.getDeclaredMethods().length == 0;
    }

    /**
     * Returns {@code true} if the given annotation's type has no attributes.
     *
     * @see #isMarker(Class)
     */
    public static boolean isMarker(Annotation annotation) {
        return isMarker(annotation.annotationType());
    }
}
