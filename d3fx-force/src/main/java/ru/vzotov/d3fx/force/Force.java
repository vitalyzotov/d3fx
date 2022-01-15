package ru.vzotov.d3fx.force;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class Force<N extends ForcedNode<?>> {

    protected ObservableList<N> nodes;

    protected Force(ObservableList<N> nodes) {
        if (nodes == null) throw new IllegalArgumentException();
        this.nodes = nodes;
        this.nodes.addListener(this::nodesChanged);
    }

    protected void nodesChanged(ListChangeListener.Change<? extends N> c) {
    }

    public abstract void force(double alpha);

    protected static double notZero(double v, double ifZero) {
        return Double.compare(v, 0d) == 0 ? ifZero : v;
    }

}
