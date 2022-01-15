package ru.vzotov.d3fx.quadtree;

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
