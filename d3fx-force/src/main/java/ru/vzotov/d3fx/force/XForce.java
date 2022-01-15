package ru.vzotov.d3fx.force;

import javafx.collections.ObservableList;

import java.util.function.Function;

public class XForce<N extends ForcedNode<?>> extends CustomizableForce<N> {
    private final Function<N, Double> strength;
    private final Function<N, Double> targetX;
    private double[] strengths = new double[DEFAULT_CAPACITY];
    private double[] xz = new double[DEFAULT_CAPACITY];
    private int capacity = DEFAULT_CAPACITY;

    @Override
    public void force(double alpha) {
        N node;
        for (int i = 0, n = nodes.size(); i < n; ++i) {
            node = nodes.get(i);
            node.vx += (xz[i] - node.getX()) * strengths[i] * alpha;
        }
    }

    public XForce(ObservableList<N> nodes, Function<N, Double> targetX) {
        this(nodes, targetX, (node) -> 0.1d);
    }

    public XForce(ObservableList<N> nodes, Function<N, Double> targetX, Function<N, Double> strength) {
        super(nodes);
        this.strength = strength;
        this.targetX = targetX;
        initialize();
    }

    @Override
    protected void ensureCapacity(int n) {
        if (n > capacity) {
            this.capacity = n;
            this.xz = ensureCapacity(this.xz, this.capacity);
            this.strengths = ensureCapacity(this.strengths, this.capacity);
        }
    }

    @Override
    protected void initNode(int i, N node) {
        strengths[i] = Double.isNaN(xz[i] = targetX.apply(node)) ? 0 : strength.apply(node);
    }

}
