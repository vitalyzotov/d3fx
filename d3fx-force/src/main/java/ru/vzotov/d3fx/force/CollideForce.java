package ru.vzotov.d3fx.force;

import ru.vzotov.d3fx.quadtree.QuadNode;
import ru.vzotov.d3fx.quadtree.QuadTree;
import javafx.collections.ObservableList;

import java.util.function.Function;

import static ru.vzotov.d3fx.force.ForceAnimation.jiggle;

public class CollideForce<N extends ForcedNode<?>> extends CustomizableForce<N> {

    private double strength = 1.0d;
    private int iterations = 1;
    private Function<N, Double> radius;

    private double[] radii = new double[DEFAULT_CAPACITY];
    private int capacity = DEFAULT_CAPACITY;

    @Override
    public void force(double alpha) {
        int i, n = nodes.size();

        for (int k = 0; k < iterations; ++k) {
            QuadTree<N, CollideQuadNode<N>> tree = QuadTree.quadTree(
                    nodes, this::x, this::y, CollideQuadNode::new, CollideQuadNode::new
            ).visitAfter(this::prepare);
            for (i = 0; i < n; ++i) {
                N node = nodes.get(i);
                double ri = radii[node.index];
                double ri2 = ri * ri;
                double xi = node.getX() + node.vx;
                double yi = node.getY() + node.vy;
                tree.visit((CollideQuadNode<N> quad, double x0, double y0, double x1, double y1) -> {
                    N data = quad.data;
                    double rj = quad.r;
                    double r = ri + rj;
                    if (data != null) {
                        if (data.index > node.index) {
                            double x = xi - data.getX() - data.vx,
                                    y = yi - data.getY() - data.vy,
                                    l = x * x + y * y;
                            if (l < r * r) {
                                if (Double.compare(x, 0d) == 0) {
                                    x = jiggle();
                                    l += x * x;
                                }
                                if (Double.compare(y, 0d) == 0) {
                                    y = jiggle();
                                    l += y * y;
                                }
                                l = (r - (l = Math.sqrt(l))) / l * strength;
                                node.vx += (x *= l) * (r = (rj *= rj) / (ri2 + rj));
                                node.vy += (y *= l) * r;
                                data.vx -= x * (r = 1 - r);
                                data.vy -= y * r;
                            }
                        }
                        return false;
                    }
                    return x0 > xi + r || x1 < xi - r || y0 > yi + r || y1 < yi - r;
                });
            }
        }
    }

    private boolean prepare(CollideQuadNode<N> quad, double x0, double y0, double x1, double y1) {
        if (quad.isLeaf()) {
            quad.r = radii[quad.data.index];
            return false;
        }
        quad.r = 0;
        for (var i = 0; i < 4; ++i) {
            if (quad.get(i) != null && quad.get(i).r > quad.r) {
                quad.r = quad.get(i).r;
            }
        }
        return false;
    }

    public CollideForce(ObservableList<N> nodes) {
        this(nodes, (node) -> 1d);
    }

    public CollideForce(ObservableList<N> nodes, Function<N, Double> radius) {
        super(nodes);
        this.radius = radius;
        initialize();
    }

    @Override
    protected void ensureCapacity(int n) {
        if (n > capacity) {
            capacity = n;
            radii = ensureCapacity(radii, capacity);
        }
    }

    @Override
    protected void initNode(int i, N node) {
        radii[i] = radius.apply(node);
    }

    private double x(N d) {
        return d.getX() + d.vx;
    }

    private double y(N d) {
        return d.getY() + d.vy;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public Function<N, Double> getRadius() {
        return radius;
    }

    public void setRadius(Function<N, Double> radius) {
        this.radius = radius;
    }

    private static class CollideQuadNode<N extends ForcedNode> extends QuadNode<N, CollideQuadNode<N>> {
        private double r;

        public CollideQuadNode() {
        }

        public CollideQuadNode(N data) {
            super(data);
        }
    }
}
