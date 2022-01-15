package ru.vzotov.d3fx.force;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Arrays;

public abstract class CustomizableForce<N extends ForcedNode<?>> extends Force<N> {

    protected static final int DEFAULT_CAPACITY = 50;

    protected CustomizableForce(ObservableList<N> nodes) {
        super(nodes);
    }

    protected static double[] ensureCapacity(double[] array, int capacity) {
        return (array.length < capacity) ? Arrays.copyOf(array, capacity) : array;
    }

    /**
     * Initialize all nodes
     */
    protected void initialize() {
        final int n = nodes.size();
        ensureCapacity(n);
        for (int i = 0; i < n; i++) {
            initNode(i, nodes.get(i));
        }
    }

    /**
     * Initialize single node
     *
     * @param i    index of node
     * @param node node
     */
    protected abstract void initNode(int i, N node);

    /**
     * Create arrays
     *
     * @param n new array size
     */
    protected abstract void ensureCapacity(int n);

    @Override
    protected void nodesChanged(ListChangeListener.Change<? extends N> c) {
        ensureCapacity(nodes.size());

        boolean removed = false;

        while (c.next()) {
            if (c.wasRemoved()) {
                removed = true;
                break;
            } else {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    initNode(i, nodes.get(i));
                }
            }
        }

        if (removed) {
            initialize();
        }
    }

}
