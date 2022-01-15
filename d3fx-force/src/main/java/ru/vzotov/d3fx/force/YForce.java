package ru.vzotov.d3fx.force;

import javafx.collections.ObservableList;

import java.util.function.Function;

public class YForce<N extends ForcedNode<?>> extends CustomizableForce<N> {
    private final Function<N, Double> strength;
    private final Function<N, Double> targetY;
    private double[] strengths = new double[DEFAULT_CAPACITY];
    private double[] yz = new double[DEFAULT_CAPACITY];
    private int capacity = DEFAULT_CAPACITY;

    @Override
    public void force(double alpha) {
        N node;
        for (int i = 0, n = nodes.size(); i < n; ++i) {
            node = nodes.get(i);
            node.vy += (yz[i] - node.getY()) * strengths[i] * alpha;
        }
    }

    public YForce(ObservableList<N> nodes, Function<N, Double> targetY) {
        this(nodes, targetY, (node) -> 0.1d);
    }

    public YForce(ObservableList<N> nodes, Function<N, Double> targetY, Function<N, Double> strength) {
        super(nodes);
        this.strength = strength;
        this.targetY = targetY;
        initialize();
    }

    @Override
    protected void ensureCapacity(int n) {
        if (n > capacity) {
            this.capacity = n;
            this.yz = ensureCapacity(this.yz, this.capacity);
            this.strengths = ensureCapacity(this.strengths, this.capacity);
        }
    }

    @Override
    protected void initNode(int i, N node) {
        strengths[i] = Double.isNaN(yz[i] = targetY.apply(node)) ? 0 : strength.apply(node);
    }

}
