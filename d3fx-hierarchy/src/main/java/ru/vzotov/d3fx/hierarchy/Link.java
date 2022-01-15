package ru.vzotov.d3fx.hierarchy;

public class Link<D extends NodeData, N extends Node<D, N>> {

    private final N source;

    private final N target;

    public Link(N source, N target) {
        this.source = source;
        this.target = target;
    }

    public N getSource() {
        return source;
    }

    public N getTarget() {
        return target;
    }
}
