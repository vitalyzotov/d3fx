package ru.vzotov.d3fx.sankey;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param <I> type of node identifier
 * @param <V> type of node value
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Sankey<I, V extends Comparable<V>> implements Graph<I, V> {

    public final Function<Node<I, V>, I> defaultId = Node::id;

    // extent
    private double x0 = 0;
    private double y0 = 0;
    private double x1 = 1;
    private double y1 = 1;

    /**
     * node width
     */
    private double dx = 24;

    /**
     * node height
     */
    private double dy = 8;

    /**
     * node padding
     */
    private double py; // nodePadding
    private Function<Node<I, V>, I> id = defaultId;
    private Alignment align = Align.justify();
    private Comparator<Node<I, V>> sort;
    private Comparator<Link<I, V>> linkSort;
    private List<? extends Node<I, V>> nodes;// = defaultNodes;
    private List<Link<I, V>> links;// = defaultLinks;
    private int iterations = 6;
    private int layers = 1;

    private final Comparator<Node<I, V>> ascendingBreadth = (a, b) -> (int) (a.y0() - b.y0());

    private final Comparator<Link<I, V>> ascendingSourceBreadth = (a, b) -> {
        int r = ascendingBreadth.compare(a.source(), b.source());
        return r != 0 ? r : a.index() - b.index();
    };

    private final Comparator<Link<I, V>> ascendingTargetBreadth = (a, b) -> {
        int r = ascendingBreadth.compare(a.target(), b.target());
        return r != 0 ? r : a.index() - b.index();
    };

    private final ValueCalculator<V> calculator;

    public Sankey(ValueCalculator<V> calculator) {
        this.calculator = calculator;
    }

    public Sankey<I, V> sankey() {
        return this.sankey(Collections::emptyList, Collections::emptyList);
    }

    public Sankey<I, V> sankey(Supplier<? extends List<Node<I, V>>> nodes, Supplier<List<Link<I, V>>> links) {
        return this.sankey(nodes.get(), links.get());
    }

    /**
     * Computes and updates the positions and properties of nodes and links for a Sankey diagram.
     * This method initializes and configures the internal state of the Sankey diagram based on the provided nodes and links.
     * It applies several layout computations to determine node depths, heights, breadths, and the corresponding breadths for links.
     * <p>
     * The method modifies the state of this {@code Sankey} object and returns it with updated layout information.
     * <ul>
     * <li>{@code graph.nodes} - An array of nodes, each node having updated layout properties.</li>
     * <li>{@code graph.links} - An array of links, each link having updated layout properties related to its source and target nodes.</li>
     * </ul>
     *
     * @param nodes the list of nodes to be laid out in the Sankey diagram
     * @param links the list of links between the nodes in the Sankey diagram
     * @return this {@code Sankey} object after computing the layout
     */
    public Sankey<I, V> sankey(List<? extends Node<I, V>> nodes, List<Link<I, V>> links) {
        this.nodes = nodes;
        this.links = links;
        var graph = this;
        computeNodeLinks(graph);
        computeNodeValues(graph);
        computeNodeDepths(graph);
        computeNodeHeights(graph);
        computeNodeBreadths(graph);
        computeLinkBreadths(graph);
        return this;
    }

    public int layers() {
        return layers;
    }

    @Override
    public Function<Node<I, V>, I> id() {
        return id;
    }

    @Override
    public Comparator<Link<I, V>> linkSort() {
        return linkSort;
    }

    @Override
    public List<? extends Node<I, V>> nodes() {
        return nodes;
    }

    @Override
    public List<Link<I, V>> links() {
        return links;
    }

    ///////////////////////////////////////////////

    /**
     * Recomputes the specified graph’s links’ positions, updating the following properties of each link:
     * <p>
     * link.y0 - the link’s vertical starting position (at source node)
     * link.y1 - the link’s vertical end position (at target node)
     * This method is intended to be called after computing the initial Sankey layout,
     * for example when the diagram is repositioned interactively.
     */
    public Graph<I, V> update(Graph<I, V> graph) {
        computeLinkBreadths(graph);
        return graph;
    }

    public Function<Node<I, V>, I> getNodeId() {
        return id;
    }

    public Sankey<I, V> setNodeId(Function<Node<I, V>, I> nodeId) {
        this.id = nodeId;
        return this;
    }

    public Alignment getNodeAlign() {
        return align;
    }

    public Sankey<I, V> setNodeAlign(Alignment align) {
        this.align = align;
        return this;
    }

    public Comparator<? super Node<I, V>> getNodeSort() {
        return sort;
    }

    public Sankey<I, V> setNodeSort(Comparator<Node<I, V>> sort) {
        this.sort = sort;
        return this;
    }

    public double getNodeWidth() {
        return dx;
    }

    public Sankey<I, V> setNodeWidth(double dx) {
        this.dx = dx;
        return this;
    }

    public double getNodePadding() {
        return dy;
    }

    public Sankey<I, V> setNodePadding(double dy) {
        this.dy = dy;
        this.py = dy;
        return this;
    }

    public List<? extends Node<I, V>> getNodes() {
        return nodes;
    }

    public Sankey<I, V> setNodes(List<? extends Node<I, V>> nodes) {
        this.nodes = nodes;
        return this;
    }

    public List<? super Link<I, V>> getLinks() {
        return links;
    }

    public Sankey<I, V> setLinks(List<Link<I, V>> links) {
        this.links = links;
        return this;
    }

    public Comparator<? super Link<I, V>> getLinkSort() {
        return linkSort;
    }

    public Sankey<I, V> setLinkSort(Comparator<Link<I, V>> linkSort) {
        this.linkSort = linkSort;
        return this;
    }

    public Size getSize() {
        return new Size(x1 - x0, y1 - y0);
    }

    public Sankey<I, V> setSize(double width, double height) {
        this.x0 = 0;
        this.y0 = 0;
        this.x1 = width;
        this.y1 = height;
        return this;
    }

    public Sankey<I, V> setSize(Size size) {
        return setSize(size.width(), size.height());
    }

    public Extent getExtent() {
        return new Extent(x0, y0, x1, y1);
    }

    public Sankey<I, V> setExtent(double x0, double y0, double x1, double y1) {
        this.x0 = x0;
        this.y0 = y1;
        this.x1 = x1;
        this.y1 = y1;
        return this;
    }

    public Sankey<I, V> setExtent(Extent extent) {
        return setExtent(extent.x0(), extent.y0(), extent.x1(), extent.y1());
    }

    public int getIterations() {
        return iterations;
    }

    public Sankey<I, V> setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    ///////////////////////////////////////////////

    private void computeNodeLinks(Graph<I, V> graph) {
        List<? extends Node<I, V>> nodes = graph.nodes();
        List<Link<I, V>> links = graph.links();

        int i = 0;
        for (Node<I, V> node : nodes) {
            node.setIndex(i++);
            node.clearLinks();
        }

        //const nodeById = new Map(nodes.map((d, i) => [id(d, i, nodes), d]));
        Map<I, Node<I, V>> nodeById = nodes.stream().collect(Collectors.toMap(graph.id(), Function.identity()));

        i = 0;
        for (Link<I, V> link : links) {
            link.index(i++);
            I sourceId = link.sourceKey();
            I targetId = link.targetKey();
            Node<I, V> source = link.source();
            Node<I, V> target = link.target();
            if (sourceId != null) source = link.source(nodeById.get(sourceId));
            if (targetId != null) target = link.target(nodeById.get(targetId));
            source.sourceLinks().add(link);
            target.targetLinks().add(link);
        }

        final Comparator<Link<I, V>> linkSort = graph.linkSort();
        if (linkSort != null) {
            for (Node<I, V> node : nodes) {
                node.sourceLinks().sort(linkSort);
                node.targetLinks().sort(linkSort);
            }
        }
    }

    private void computeNodeValues(Graph<I, V> graph) {
        List<? extends Node<I, V>> nodes = graph.nodes();
        for (var node : nodes) {
            node.value(node.fixedValue() == null ?
                    Stream.of(
                                    node.sourceLinks().stream().map(Link::value).reduce(calculator::sum).orElse(null),
                                    node.targetLinks().stream().map(Link::value).reduce(calculator::sum).orElse(null))
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(null)
                    : node.fixedValue());
        }
    }

    /**
     * Computes the depth of each node.
     */
    private void computeNodeDepths(Graph<I, V> graph) {
        List<? extends Node<I, V>> nodes = graph.nodes();
        int n = nodes.size();
        Set<Node<I, V>> current = new HashSet<>(nodes);
        Set<Node<I, V>> next = new HashSet<>();
        int x = 0;
        while (!current.isEmpty()) {
            for (var node : current) {
                node.setDepth(x);
                node.sourceLinks().stream().map(Link::target).forEach(next::add);
            }
            if (++x > n) throw new RuntimeException("circular link");
            current = next;
            next = new HashSet<>();
        }
    }

    private void computeNodeHeights(Graph<I, V> graph) {
        List<? extends Node<I, V>> nodes = graph.nodes();
        int n = nodes.size();
        Set<Node<I, V>> current = new HashSet<>(nodes);
        Set<Node<I, V>> next = new HashSet<>();
        int i = 0;
        while (!current.isEmpty()) {
            for (var node : current) {
                node.setHeight(i);
                node.targetLinks().stream().map(Link::source).forEach(next::add);
            }
            if (++i > n) throw new RuntimeException("circular link");
            current = next;
            next = new HashSet<>();
        }
    }

    private List<List<Node<I, V>>> computeNodeLayers(Graph<I, V> graph) {
        var nodes = graph.nodes();
        this.layers = nodes.stream().mapToInt(Node::depth).max().orElse(0) + 1;
        double layerWidth = computeLayerWidth();

        List<List<Node<I, V>>> columns = new ArrayList<>(Collections.nCopies(layers, null));
        for (var node : nodes) {
            int i = Math.max(0, Math.min(layers - 1, align.align(node, layers)));
            node.setLayer(i);

            double nx0 = x0 + i * layerWidth;
            node.setHorizontalPosition(nx0, nx0 + dx);

            if (columns.get(i) != null) {
                columns.get(i).add(node);
            } else {
                columns.set(i, new ArrayList<>(Collections.singleton(node)));
            }
        }
        if (sort != null) {
            for (var column : columns) {
                column.sort(sort);
            }
        }
        return columns;
    }

    public double computeLayerWidth() {
        return (x1 - x0 - dx) / (layers - 1);
    }

    private void initializeNodeBreadths(List<List<Node<I, V>>> columns) {
        double ky = columns.stream()
                .mapToDouble(c -> {
                    double sum = c.stream().map(Node::value).mapToDouble(calculator::toDouble).sum();
                    return (y1 - y0 - (c.size() - 1) * py) / sum;
                })
                .min()
                .orElse(Double.NaN);

        for (var nodes : columns) {
            double y = y0;
            for (var node : nodes) {
                node.setVerticalPosition(y, y + calculator.toDouble(node.value()) * ky);
                y = node.y1() + py;
                for (var link : node.sourceLinks()) {
                    link.width(calculator.toDouble(link.value()) * ky);
                }
            }
            y = (y1 - y + py) / (nodes.size() + 1);
            for (int i = 0; i < nodes.size(); ++i) {
                var node = nodes.get(i);
                node.setVerticalPosition(node.y0() + y * (i + 1), node.y1() + y * (i + 1));
            }
            reorderLinks(nodes);
        }
    }

    private void computeNodeBreadths(Graph<I, V> graph) {
        List<List<Node<I, V>>> columns = computeNodeLayers(graph);
        py = Math.min(dy,
                (y1 - y0) / (
                        columns.stream().mapToInt(List::size).max().orElse(0) - 1
                ));
        initializeNodeBreadths(columns);
        for (int i = 0; i < iterations; ++i) {
            double alpha = Math.pow(0.99, i);
            double beta = Math.max(1 - alpha, (double) (i + 1) / iterations);
            relaxRightToLeft(columns, alpha, beta);
            relaxLeftToRight(columns, alpha, beta);
        }
    }

    // Reposition each node based on its incoming (target) links.
    private void relaxLeftToRight(List<List<Node<I, V>>> columns, double alpha, double beta) {
        for (int i = 1, n = columns.size(); i < n; ++i) {
            var column = columns.get(i);
            for (Node<I, V> target : column) {
                double y = 0d;
                double w = 0d;
                for (var link : target.targetLinks()) {
                    var source = link.source();
                    double v = calculator.toDouble(link.value()) * (target.layer() - source.layer());
                    y += targetTop(source, target) * v;
                    w += v;
                }
                if (!(w > 0)) continue;
                double dy = (y / w - target.y0()) * alpha;
                target.setVerticalPosition(target.y0() + dy, target.y1() + dy);
                reorderNodeLinks(target);
            }
            if (sort == null) column.sort(ascendingBreadth);
            resolveCollisions(column, beta);
        }
    }


    /**
     * Reposition each node based on its outgoing (source) links.
     */
    private void relaxRightToLeft(List<List<Node<I, V>>> columns, double alpha, double beta) {
        for (int n = columns.size(), i = n - 2; i >= 0; --i) {
            var column = columns.get(i);
            for (var source : column) {
                double y = 0;
                double w = 0;
                for (var link : source.sourceLinks()) {
                    var target = link.target();
                    var v = calculator.toDouble(link.value()) * (target.layer() - source.layer());
                    y += sourceTop(source, target) * v;
                    w += v;
                }
                if (!(w > 0)) continue;
                var dy = (y / w - source.y0()) * alpha;
                source.setVerticalPosition(source.y0() + dy, source.y1() + dy);
                reorderNodeLinks(source);
            }
            if (sort == null) column.sort(ascendingBreadth);
            resolveCollisions(column, beta);
        }
    }

    private void resolveCollisions(List<Node<I, V>> nodes, double alpha) {
        int i = nodes.size() >> 1;
        var subject = nodes.get(i);
        resolveCollisionsBottomToTop(nodes, subject.y0() - py, i - 1, alpha);
        resolveCollisionsTopToBottom(nodes, subject.y1() + py, i + 1, alpha);
        resolveCollisionsBottomToTop(nodes, y1, nodes.size() - 1, alpha);
        resolveCollisionsTopToBottom(nodes, y0, 0, alpha);
    }

    // Push any overlapping nodes down.
    private void resolveCollisionsTopToBottom(List<Node<I, V>> nodes, double y, int i, double alpha) {
        for (; i < nodes.size(); ++i) {
            var node = nodes.get(i);
            double dy = (y - node.y0()) * alpha;
            if (dy > 1e-6) {
                node.setVerticalPosition(node.y0() + dy, node.y1() + dy);
            }
            y = node.y1() + py;
        }
    }

    // Push any overlapping nodes up.
    private void resolveCollisionsBottomToTop(List<Node<I, V>> nodes, double y, int i, double alpha) {
        for (; i >= 0; --i) {
            var node = nodes.get(i);
            double dy = (node.y1() - y) * alpha;
            if (dy > 1e-6) {
                node.setVerticalPosition(node.y0() - dy, node.y1() - dy);
            }
            y = node.y0() - py;
        }
    }

    private void reorderNodeLinks(Node<I, V> node) {
        final var sourceLinks = node.sourceLinks();
        final var targetLinks = node.targetLinks();
        if (linkSort == null) {
            targetLinks.stream().map(link -> link.source().sourceLinks()).forEach(links -> {
                links.sort(ascendingTargetBreadth);
            });
            sourceLinks.stream().map(link -> link.target().targetLinks()).forEach(links -> {
                links.sort(ascendingSourceBreadth);
            });
        }
    }

    private void reorderLinks(List<Node<I, V>> nodes) {
        if (linkSort == null) {
            for (var node : nodes) {
                node.sourceLinks().sort(ascendingTargetBreadth);
                node.targetLinks().sort(ascendingSourceBreadth);
            }
        }
    }

    // Returns the target.y0 that would produce an ideal link from source to target.
    private double targetTop(Node<I, V> source, Node<I, V> target) {
        double i = source.y0() - (source.sourceLinks().size() - 1) * py / 2;

        for (var link : source.sourceLinks()) {
            var node = link.target();
            double width = link.width();

            if (node == target) break;
            i += width + py;
        }

        for (var link : target.targetLinks()) {
            var node = link.source();
            double width = link.width();

            if (node == source) break;
            i -= width;
        }
        return i;
    }

    /**
     * Returns the source.y0 that would produce an ideal link from source to target.
     */
    private double sourceTop(Node<I, V> source, Node<I, V> target) {
        double i = target.y0() - (target.targetLinks().size() - 1) * py / 2;

        for (var link : target.targetLinks()) {
            var node = link.source();
            double width = link.width();

            if (node == source) break;
            i += width + py;
        }

        for (var link : source.sourceLinks()) {
            var node = link.target();
            double width = link.width();

            if (node == target) break;
            i -= width;
        }

        return i;
    }

    private void computeLinkBreadths(Graph<I, V> graph) {
        var nodes = graph.nodes();
        for (var node : nodes) {
            double y0 = node.y0();
            double y1 = y0;
            for (var link : node.sourceLinks()) {
                link.y0(y0 + link.width() / 2);
                y0 += link.width();
            }
            for (var link : node.targetLinks()) {
                link.y1(y1 + link.width() / 2);
                y1 += link.width();
            }
        }
    }

    public record Size(double width, double height) {
    }

    public record Extent(double x0, double y0, double x1, double y1) {
    }

    public interface ValueCalculator<V> {
        /**
         * Calculates the sum of two values.
         *
         * @param v1 The first value. Nullable.
         * @param v2 The second value. Nullable.
         * @return The sum of the two values.
         */
        V sum(V v1, V v2);

        /**
         * Calculates the fraction of two values.
         *
         * @param dividend the dividend
         * @param divisor  the divisor
         * @return The fraction of the two values. {@code null} if the dividend is null.
         * {@code NaN} if the divisor is null.
         */
        Double divide(V dividend, V divisor);

        /**
         * Converts a value to a double. This is used for relative positioning.
         *
         * @param v The value
         * @return The double value. {@code null} if the value is null.
         */
        Double toDouble(V v);
    }

    public static class NumberValueCalculator<V extends Number> implements ValueCalculator<V> {

        private final BinaryOperator<V> plus;

        public NumberValueCalculator(BinaryOperator<V> plus) {
            this.plus = plus;
        }

        @Override
        public V sum(V v1, V v2) {
            return v1 == null ? v2 : v2 == null ? v1 : plus.apply(v1, v2);
        }

        @Override
        public Double divide(Number dividend, Number divisor) {
            return dividend == null ? null :
                    divisor == null ? Double.NaN :
                            dividend.doubleValue() / divisor.doubleValue();
        }

        @Override
        public Double toDouble(Number number) {
            return number == null ? null : number.doubleValue();
        }
    }

}
