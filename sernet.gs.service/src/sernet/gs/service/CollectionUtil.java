package sernet.gs.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

public class CollectionUtil {

    private CollectionUtil() {

    }

    public static <T> @NonNull Set<T> unmodifiableSet(Set<? extends T> set) {
        Set<T> unmodifiableCopy = Collections.unmodifiableSet(set);
        if (unmodifiableCopy == null) {
            return new HashSet<>();
        }
        return unmodifiableCopy;
    }
}
