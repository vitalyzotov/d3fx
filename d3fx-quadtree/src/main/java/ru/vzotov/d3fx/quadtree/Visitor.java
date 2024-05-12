package ru.vzotov.d3fx.quadtree;

/**
 * Represents a functional interface for visiting nodes within a quadtree.
 * This interface is used to define a visitation pattern that can be applied to each node of the quadtree during traversal operations.
 * The visit method is called for each node in the quadtree, and the implementation can perform any operations needed on the nodes,
 * such as checking conditions, modifying node properties, or collecting data.
 *
 * @param <E> the type of elements stored in the quadtree nodes
 * @param <Q> the specific type of QuadNode used in the quadtree
 */
@FunctionalInterface
public interface Visitor<E, Q extends QuadNode<E, Q>> {

    /**
     * Visits each node in the quadtree in pre-order traversal. This method is invoked with the current node and its bounds
     * during the traversal process.
     *
     * @param node The current node being visited.
     * @param x0   The lower x-bound of the node's bounding box (left edge).
     * @param y0   The lower y-bound of the node's bounding box (top edge).
     * @param x1   The upper x-bound of the node's bounding box (right edge).
     * @param y1   The upper y-bound of the node's bounding box (bottom edge).
     * @return A boolean value. If {@code true}, the traversal skips visiting the children of the current node;
     * if {@code false}, the traversal continues to the child nodes.
     */
    boolean visit(Q node, double x0, double y0, double x1, double y1);
}
