package sernet.gs.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

public class CollectionUtil {

    private CollectionUtil() {

    }

    /**
     * Returns consecutive sublists of a list, each of the same size (the final
     * list may be smaller). For example, partitioning a list containing [a, b,
     * c, d, e] with a partition size of 3 yields [[a, b, c], [d, e]] -- an
     * outer list containing two inner lists of three and two elements, all in
     * the original order.
     * 
     * @param <T>
     *            the element type
     * @param list
     *            the list to return consecutive sublists of
     * @param size
     *            the desired size of each sublist (the last may be smaller)
     * @return a list of consecutive sublists
     */
    public static <T> Collection<List<T>> partition(List<T> list, int size) {
        final AtomicInteger counter = new AtomicInteger();
        return list.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size))
                .values();
    }

    public static <T> @NonNull Set<T> unmodifiableSet(Set<? extends T> set) {
        Set<T> unmodifiableCopy = Collections.unmodifiableSet(set);
        if (unmodifiableCopy == null) {
            return new HashSet<>();
        }
        return unmodifiableCopy;
    }
}
