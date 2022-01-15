package ru.vzotov.d3fx.force;

import javafx.collections.ObservableList;

import java.util.function.Function;

public class RadialForce<N extends ForcedNode<?>> extends CustomizableForce<N> {

    private Function<N, Double> radius;
    private double x;
    private double y;
    private Function<N, Double> strength = (node) -> 0.1d;
    private double[] strengths = new double[DEFAULT_CAPACITY];
    private double[] radiuses = new double[DEFAULT_CAPACITY];
    private int capacity = DEFAULT_CAPACITY;

    @Override
    public void force(double alpha) {
        int n = nodes.size();
        for (int i = 0; i < n; ++i) {
            var node = nodes.get(i);
            double dx = notZero(node.getX() - x, 1e-6);
            double dy = notZero(node.getY() - y, 1e-6);
            double r = Math.sqrt(dx * dx + dy * dy);
            double k = (radiuses[i] - r) * strengths[i] * alpha / r;
            node.vx += dx * k;
            node.vy += dy * k;
        }
    }

    public RadialForce(ObservableList<N> nodes, double radius) {
        this(nodes, (node) -> radius);
    }

    public RadialForce(ObservableList<N> nodes, Function<N, Double> radius) {
        this(nodes, radius, 0d, 0d);
    }

    public RadialForce(ObservableList<N> nodes, Function<N, Double> radius,
                       double x, double y) {
        super(nodes);
        this.radius = radius;
        this.x = x;
        this.y = y;
        initialize();
    }

    @Override
    protected void ensureCapacity(int n) {
        if (n > capacity) {
            this.capacity = n;
            strengths = ensureCapacity(strengths, capacity);
            radiuses = ensureCapacity(radiuses, capacity);
        }
    }

    @Override
    protected void initNode(int i, N node) {
        radiuses[i] = radius.apply(node);
        strengths[i] = Double.isNaN(radiuses[i]) ? 0d : strength.apply(node);
    }

    public Function<N, Double> getRadius() {
        return radius;
    }

    public void setRadius(Function<N, Double> radius) {
        this.radius = radius;
        initialize();
    }

    public Function<N, Double> getStrength() {
        return strength;
    }

    public void setStrength(Function<N, Double> strength) {
        this.strength = strength;
        initialize();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
