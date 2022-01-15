package ru.vzotov.d3fx.quadtree;

@FunctionalInterface
public interface Visitor<E, Q extends QuadNode<E, Q>> {
    boolean visit(Q node, double x0, double y0, double x1, double y1);
}
