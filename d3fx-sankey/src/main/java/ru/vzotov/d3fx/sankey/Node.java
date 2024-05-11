package ru.vzotov.d3fx.sankey;

import java.util.List;

/**
 * @param <I> type of identifier
 * @param <V> type of value
 */
public interface Node<I, V> {


    I id();

    /**
     * @return the list of outgoing links which have this node as their source
     */
    List<Link<I,V>> sourceLinks();

    /**
     * @return the list of incoming links which have this node as their target
     */
    List<Link<I,V>> targetLinks();

    void clearLinks();

    /**
     * @return the node’s zero-based index within the array of nodes
     */
    int index();

    void setIndex(int i);

    /**
     * @return the node’s zero-based graph depth, derived from the graph topology
     */
    int depth();

    void setDepth(int depth);

    /**
     * @return the node’s zero-based graph height, derived from the graph topology
     */
    int height();

    void setHeight(int height);

    /**
     * @return the node’s zero-based column index, corresponding to its horizontal position
     */
    int layer();

    void setLayer(int l);

    /**
     * @return the node’s minimum horizontal position, derived from node.depth
     */
    double x0();

    /**
     * @return the node’s maximum horizontal position (node.x0 + sankey.nodeWidth)
     */
    double x1();

    void setHorizontalPosition(double x0, double x1);

    /**
     * @return the node’s minimum vertical position
     */
    double y0();

    /**
     * @return the node’s maximum vertical position (node.y1 - node.y0 is proportional to node.value)
     */
    double y1();

    void setVerticalPosition(double y0, double y1);

    /**
     * his is the sum of link.value for the node’s incoming links, or node.fixedValue if defined
     * @return The node’s value;
     */
    V value();

    V value(V v);

    V fixedValue();

    void setFixedValue(V fixedValue);

}
