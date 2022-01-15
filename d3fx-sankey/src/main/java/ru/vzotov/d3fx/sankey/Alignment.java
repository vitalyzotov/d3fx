package ru.vzotov.d3fx.sankey;

@FunctionalInterface
public interface Alignment<I> {
    int align(Node<I> node, int n);
}
