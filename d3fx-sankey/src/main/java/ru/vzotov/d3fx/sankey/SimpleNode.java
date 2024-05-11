package ru.vzotov.d3fx.sankey;

import java.util.ArrayList;
import java.util.List;

public class SimpleNode<I, V> implements Node<I, V> {

    /**
     * the array of outgoing links which have this node as their source
     */
    private List<Link<I,V>> sourceLinks;

    /**
     * the array of incoming links which have this node as their target
     */
    private List<Link<I,V>> targetLinks;

    /**
     * The node’s value;
     * This is the sum of link.value for the node’s incoming links, or node.fixedValue if defined
     */
    private V value;

    private V fixedValue;

    /**
     * the node’s zero-based index within the array of nodes
     */
    private int index;

    /**
     * the node’s zero-based graph depth, derived from the graph topology
     */
    private int depth;

    /**
     * the node’s zero-based graph height, derived from the graph topology
     */
    private int height;

    /**
     * the node’s zero-based column index, corresponding to its horizontal position
     */
    private int layer;

    /**
     * the node’s minimum horizontal position, derived from node.depth
     */
    private double x0;

    /**
     * the node’s maximum horizontal position (node.x0 + sankey.nodeWidth)
     */
    private double x1;

    /**
     * the node’s minimum vertical position
     */
    private double y0;

    /**
     * the node’s maximum vertical position (node.y1 - node.y0 is proportional to node.value)
     */
    private double y1;

    private final I id;

    public SimpleNode(I id, V value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Node{" +
                "value=" + value +
                ", id=" + id +
                '}';
    }

    public I id() {
        return id;
    }

    public List<Link<I,V>> sourceLinks() {
        return sourceLinks;
    }

    protected void setSourceLinks(List<Link<I,V>> links) {
        this.sourceLinks = links;
    }

    public List<Link<I,V>> targetLinks() {
        return targetLinks;
    }

    protected void setTargetLinks(List<Link<I,V>> links) {
        this.targetLinks = links;
    }

    @Override
    public void clearLinks() {
        setSourceLinks(new ArrayList<>());
        setTargetLinks(new ArrayList<>());
    }

    /**
     * @return the node’s zero-based index within the array of nodes
     */
    public int index() {
        return index;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    /**
     * the node’s zero-based graph depth, derived from the graph topology
     */
    public int depth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int height() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int layer() {
        return layer;
    }

    public void setLayer(int l) {
        this.layer = l;
    }

    public double x0() {
        return x0;
    }

    public double x1() {
        return x1;
    }

    @Override
    public void setHorizontalPosition(double x0, double x1) {
        this.x0 = x0;
        this.x1 = x1;
    }

    public double y0() {
        return y0;
    }

    public double y1() {
        return y1;
    }

    public void setVerticalPosition(double y0, double y1) {
        this.y0 = y0;
        this.y1 = y1;
    }

    public V value() {
        return value;
    }

    public V value(V v) {
        value = v;
        return value;
    }

    public V fixedValue() {
        return fixedValue;
    }

    public void setFixedValue(V fixedValue) {
        this.fixedValue = fixedValue;
    }

}
