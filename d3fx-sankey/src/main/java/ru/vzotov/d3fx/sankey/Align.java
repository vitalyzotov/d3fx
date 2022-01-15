package ru.vzotov.d3fx.sankey;

public final class Align {

    public static <I> Alignment<I> left() {
        return (node, n) -> node.depth();
    }

    public static <I> Alignment<I> right() {
        return (node, n) -> n - 1 - node.height();
    }

    public static <I> Alignment<I> justify() {
        return (node, n) -> (!node.sourceLinks().isEmpty()) ? node.depth() : n - 1;
    }

    public static <I> Alignment<I> center() {
        return (node, n) -> node.targetLinks().size() > 0 ? node.depth()
                : node.sourceLinks().size() > 0 ? node.sourceLinks().stream().mapToInt(l -> l.target().depth()).min().orElse(0) - 1
                : 0;
    }


}
