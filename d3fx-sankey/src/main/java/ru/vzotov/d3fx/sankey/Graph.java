package ru.vzotov.d3fx.sankey;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * @param <I> Type of node identifier
 */
public interface Graph<I, V> {

    /**
     * Mapping function to get node identifier
     *
     * @return mapping function for node identifier
     */
    Function<Node<I, V>, I> id();

    Comparator<Link<I, V>> linkSort();

    List<? extends Node<I, V>> nodes();

    List<Link<I, V>> links();
}
