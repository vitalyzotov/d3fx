package ru.vzotov.d3fx.force;

import javafx.scene.Node;

public class Link<D extends Node, N extends ForcedNode<D>> {
    private int index;
    private final D node;
    private final N source;
    private final N target;

    public Link(D node, N source, N target) {
        this.node = node;
        this.source = source;
        this.target = target;
    }

    void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public N getSource() {
        return source;
    }

    public N getTarget() {
        return target;
    }

    public Node getNode() {
        return node;
    }
}
