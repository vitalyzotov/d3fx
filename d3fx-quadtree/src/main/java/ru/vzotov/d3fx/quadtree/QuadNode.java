package ru.vzotov.d3fx.quadtree;

import java.util.Arrays;

/**
 * Represents a node in a quadtree structure, which can either be an internal node or a leaf node.
 *
 * @param <E> the type of elements stored in the quadtree
 * @param <Q> the specific type of QuadNode, allowing for extension and customization of nodes
 */
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
