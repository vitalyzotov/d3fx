package ru.vzotov.d3fx.sankey;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * @param <I> Type of node identifier
 */
public interface Graph<I> {

    /**
     * Mapping function to get node identifier
     *
     * @return mapping function for node identifier
     */
    Function<Node<I>, I> id();

    Comparator<Link<I>> linkSort();

    List<? extends Node<I>> nodes();

    List<Link<I>> links();
}
