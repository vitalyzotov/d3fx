package ru.vzotov.d3fx.sankey;

@FunctionalInterface
public interface Alignment {
    int align(Node<?,?> node, int n);
}
