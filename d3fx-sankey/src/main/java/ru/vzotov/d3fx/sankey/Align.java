package ru.vzotov.d3fx.sankey;

public final class Align {

    public static Alignment left() {
        return (node, n) -> node.depth();
    }

    public static Alignment right() {
        return (node, n) -> n - 1 - node.height();
    }

    public static Alignment justify() {
        return (node, n) -> (!node.sourceLinks().isEmpty()) ? node.depth() : n - 1;
    }

    public static Alignment center() {
        return (node, n) -> !node.targetLinks().isEmpty() ? node.depth() :
                (!node.sourceLinks().isEmpty() ? calculateAdjustedDepthForCenteredSources(node) : 0);
    }

    /**
     * Calculates the adjusted depth for a node based on the minimum depth of its source links' target nodes.
     * This method is used to determine an appropriate depth for nodes that are centered and have no target links.
     * The method finds the minimum depth among the target nodes of the node's source links and subtracts one
     * to position the node slightly before these targets.
     *
     * @param node The node for which the depth adjustment is to be calculated.
     * @return The adjusted depth value for the node, which is one less than the minimum depth of its source links' target nodes.
     * If the node has no source links or if the source links have no target nodes, returns -1.
     */
    private static int calculateAdjustedDepthForCenteredSources(Node<?,?> node) {
        return node.sourceLinks().stream()
                .mapToInt(l -> l.target().depth())
                .min()
                .orElse(0) - 1;
    }


}
