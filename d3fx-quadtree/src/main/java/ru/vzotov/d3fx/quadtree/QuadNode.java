package ru.vzotov.d3fx.quadtree;

import java.util.Arrays;

public class QuadNode<E, Q extends QuadNode<E, Q>> {
    private final Object[] children;
    public E data;
    public Q next;

    final boolean leaf;

    public QuadNode() {
        leaf = false;
        children = new Object[4];
    }

    public QuadNode(E data) {
        children = null;
        this.data = data;
        this.leaf = true;
    }

    public boolean hasChildren() {
        return !leaf;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public Q set(int index, Q q) {
        children[index] = q;
        return q;
    }

    public Q get(int index) {
        return (Q) children[index];
    }

    @Override
    public String toString() {
        return "QuadNode{" +
                "children=" + Arrays.toString(children) +
                ", data=" + data +
                ", next=" + next +
                '}';
    }
}
