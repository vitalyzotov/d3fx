package ru.vzotov.d3fx.quadtree;

/**
 * Represents a quadrant in a quadtree, encapsulating a node and its spatial bounds.
 *
 * @param <E> the type of elements stored in the quadtree
 * @param <Q> the specific type of QuadNode used in the quadtree
 */
public class Quad<E,Q extends QuadNode<E,Q>> {
    Q node;
    double x0;
    double y0;
    double x1;
    double y1;

    public Quad(Q node, double x0, double y0, double x1, double y1) {
        this.node = node;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }
}
