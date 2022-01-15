package ru.vzotov.d3fx.sankey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * @param <I> type of node identifier
 */
public class Sankey<I> implements Graph<I> {

    public final Function<Node<I>, I> defaultId = Node::id;

    // extent
    private double x0 = 0;
    private double y0 = 0;
    private double x1 = 1;
    private double y1 = 1;

    // nodeWidth
    private double dx = 24;
    private double dy = 8;
    private double py; // nodePadding
    private Function<Node<I>, I> id = defaultId;
    private Alignment<I> align = Align.justify();
    private Comparator<Node<I>> sort;
    private Comparator<Link<I>> linkSort;
    private List<? extends Node<I>> nodes;// = defaultNodes;
    private List<Link<I>> links;// = defaultLinks;
    private int iterations = 6;

    private final Comparator<Node<I>> ascendingBreadth = (a, b) -> (int) (a.y0() - b.y0());

    private final Comparator<Link<I>> ascendingSourceBreadth = (a, b) -> {
        int r = ascendingBreadth.compare(a.source(), b.source());
        return r != 0 ? r : a.index() - b.index();
    };

    private final Comparator<Link<I>> ascendingTargetBreadth = (a, b) -> {
        int r = ascendingBreadth.compare(a.target(), b.target());
        return r != 0 ? r : a.index() - b.index();
    };

    public Sankey() {
    }

    public Sankey<I> sankey() {
        return this.sankey(Collections::emptyList, Collections::emptyList);
    }

    public Sankey<I> sankey(Supplier<? extends List<Node<I>>> nodes, Supplier<List<Link<I>>> links) {
        return this.sankey(nodes.get(), links.get());
    }

    /**
     * Computes the node and link positions for the given arguments, returning a graph representing the Sankey layout.
     * The returned graph has the following properties:
     * <p>
     * graph.nodes - the array of nodes
     * graph.links - the array of links
     *
     * @param nodes
     * @param links
     * @return
     */
    public Sankey<I> sankey(List<? extends Node<I>> nodes, List<Link<I>> links) {
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

    @Override
    public Function<Node<I>, I> id() {
        return id;
    }

    @Override
    public Comparator<Link<I>> linkSort() {
        return linkSort;
    }

    @Override
    public List<? extends Node<I>> nodes() {
        return nodes;
    }

    @Override
    public List<Link<I>> links() {
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
     *
     * @param graph
     * @return
     */
    public Graph<I> update(Graph<I> graph) {
        computeLinkBreadths(graph);
        return graph;
    }

    public Function<Node<I>, I> getNodeId() {
        return id;
    }

    public Sankey<I> setNodeId(Function<Node<I>, I> nodeId) {
        this.id = nodeId;
        return this;
    }

    public Alignment<I> getNodeAlign() {
        return align;
    }

    public Sankey<I> setNodeAlign(Alignment<I> align) {
        this.align = align;
        return this;
    }

    public Comparator<? super Node<I>> getNodeSort() {
        return sort;
    }

    public Sankey<I> setNodeSort(Comparator<Node<I>> sort) {
        this.sort = sort;
        return this;
    }

    public double getNodeWidth() {
        return dx;
    }

    public Sankey<I> setNodeWidth(double dx) {
        this.dx = dx;
        return this;
    }

    public double getNodePadding() {
        return dy;
    }

    public Sankey<I> setNodePadding(double dy) {
        this.dy = dy;
        this.py = dy;
        return this;
    }

    public List<? extends Node<I>> getNodes() {
        return nodes;
    }

    public Sankey<I> setNodes(List<? extends Node<I>> nodes) {
        this.nodes = nodes;
        return this;
    }

    public List<? super Link<I>> getLinks() {
        return links;
    }

    public Sankey<I> setLinks(List<Link<I>> links) {
        this.links = links;
        return this;
    }

    public Comparator<? super Link<I>> getLinkSort() {
        return linkSort;
    }

    public Sankey<I> setLinkSort(Comparator<Link<I>> linkSort) {
        this.linkSort = linkSort;
        return this;
    }

    public Size getSize() {
        return new Size(x1 - x0, y1 - y0);
    }

    public Sankey<I> setSize(double width, double height) {
        this.x0 = 0;
        this.y0 = 0;
        this.x1 = width;
        this.y1 = height;
        return this;
    }

    public Sankey<I> setSize(Size size) {
        return setSize(size.width(), size.height());
    }

    public Extent getExtent() {
        return new Extent(x0, y0, x1, y1);
    }

    public Sankey<I> setExtent(double x0, double y0, double x1, double y1) {
        this.x0 = x0;
        this.y0 = y1;
        this.x1 = x1;
        this.y1 = y1;
        return this;
    }

    public Sankey<I> setExtent(Extent extent) {
        return setExtent(extent.x0(), extent.y0(), extent.x1(), extent.y1());
    }

    public int getIterations() {
        return iterations;
    }

    public Sankey<I> setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    ///////////////////////////////////////////////

    private void computeNodeLinks(Graph<I> graph) {
        List<? extends Node<I>> nodes = graph.nodes();
        List<Link<I>> links = graph.links();

        int i = 0;
        for (Node<I> node : nodes) {
            node.setIndex(i++);
            node.clearLinks();
        }

        //const nodeById = new Map(nodes.map((d, i) => [id(d, i, nodes), d]));
        Map<I, Node<I>> nodeById = nodes.stream().collect(Collectors.toMap(graph.id(), Function.identity()));

        i = 0;
        for (Link<I> link : links) {
            link.index(i++);
            I sourceId = link.sourceKey();
            I targetId = link.targetKey();
            Node<I> source = link.source();
            Node<I> target = link.target();
            if (sourceId != null) source = link.source(nodeById.get(sourceId));
            if (targetId != null) target = link.target(nodeById.get(targetId));
            source.sourceLinks().add(link);
            target.targetLinks().add(link);
        }

        final Comparator<Link<I>> linkSort = graph.linkSort();
        if (linkSort != null) {
            for (Node<I> node : nodes) {
                node.sourceLinks().sort(linkSort);
                node.targetLinks().sort(linkSort);
            }
        }
    }

    private void computeNodeValues(Graph<I> graph) {
        List<? extends Node<I>> nodes = graph.nodes();
        for (var node : nodes) {
            node.value(node.fixedValue() == null ?
                    DoubleStream.of(
                            node.sourceLinks().stream().map(Link::value).mapToDouble(Number::doubleValue).sum(),
                            node.targetLinks().stream().map(Link::value).mapToDouble(Number::doubleValue).sum()
                    ).max().orElse(Double.NaN)
                    : node.fixedValue());
        }
    }

    private void computeNodeDepths(Graph<I> graph) {
        List<? extends Node<I>> nodes = graph.nodes();
        int n = nodes.size();
        Set<Node<I>> current = new HashSet<>(nodes);
        Set<Node<I>> next = new HashSet<>();
        int x = 0;
        while (!current.isEmpty()) {
            for (Node<I> node : current) {
                node.setDepth(x);
                node.sourceLinks().stream().map(Link::target).forEach(next::add);
            }
            if (++x > n) throw new RuntimeException("circular link");
            current = next;
            next = new HashSet<>();
        }
    }

    private void computeNodeHeights(Graph<I> graph) {
        List<? extends Node<I>> nodes = graph.nodes();
        int n = nodes.size();
        Set<Node<I>> current = new HashSet<>(nodes);
        Set<Node<I>> next = new HashSet<>();
        int x = 0;
        while (!current.isEmpty()) {
            for (Node<I> node : current) {
                node.setHeight(x);
                node.targetLinks().stream().map(Link::source).forEach(next::add);
            }
            if (++x > n) throw new RuntimeException("circular link");
            current = next;
            next = new HashSet<>();
        }
    }

    private List<List<Node<I>>> computeNodeLayers(Graph<I> graph) {
        List<? extends Node<I>> nodes = graph.nodes();
        int x = nodes.stream().mapToInt(Node::depth).max().orElse(0) + 1;
        double kx = (x1 - x0 - dx) / (x - 1);

        List<List<Node<I>>> columns = new ArrayList<>(Collections.nCopies(x, null));
        for (var node : nodes) {
            int i = (int) Math.max(0, Math.min(x - 1, Math.floor(align.align(node, x))));
            node.setLayer(i);

            double nx0 = x0 + i * kx;
            node.setHorizontalPosition(nx0, nx0 + dx);

            if (columns.get(i) != null) {
                columns.get(i).add(node);
            } else {
                columns.set(i, new ArrayList<>(Collections.singleton(node)));
            }
        }
        if (sort != null) {
            for (List<Node<I>> column : columns) {
                column.sort(sort);
            }
        }
        return columns;
    }

    private void initializeNodeBreadths(List<List<Node<I>>> columns) {
        double ky = columns.stream()
                .mapToDouble(c -> {
                    double sum = c.stream().map(Node::value).mapToDouble(Number::doubleValue).sum();
                    return (y1 - y0 - (c.size() - 1) * py) / sum;
                })
                .min()
                .orElse(Double.NaN);

        for (var nodes : columns) {
            double y = y0;
            for (var node : nodes) {
                node.setVerticalPosition(y, y + node.value().doubleValue() * ky);
                y = node.y1() + py;
                for (var link : node.sourceLinks()) {
                    link.width(link.value().doubleValue() * ky);
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

    private void computeNodeBreadths(Graph<I> graph) {
        List<List<Node<I>>> columns = computeNodeLayers(graph);
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
    private void relaxLeftToRight(List<List<Node<I>>> columns, double alpha, double beta) {
        for (int i = 1, n = columns.size(); i < n; ++i) {
            var column = columns.get(i);
            for (Node<I> target : column) {
                double y = 0d;
                double w = 0d;
                for (var link : target.targetLinks()) {
                    var source = link.source();
                    var value = link.value().doubleValue();
                    double v = value * (target.layer() - source.layer());
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

    // Reposition each node based on its outgoing (source) links.
    private void relaxRightToLeft(List<List<Node<I>>> columns, double alpha, double beta) {
        for (int n = columns.size(), i = n - 2; i >= 0; --i) {
            var column = columns.get(i);
            for (Node<I> source : column) {
                double y = 0;
                double w = 0;
                for (var link : source.sourceLinks()) {
                    var target = link.target();
                    var value = link.value().doubleValue();
                    var v = value * (target.layer() - source.layer());
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

    private void resolveCollisions(List<Node<I>> nodes, double alpha) {
        int i = nodes.size() >> 1;
        var subject = nodes.get(i);
        resolveCollisionsBottomToTop(nodes, subject.y0() - py, i - 1, alpha);
        resolveCollisionsTopToBottom(nodes, subject.y1() + py, i + 1, alpha);
        resolveCollisionsBottomToTop(nodes, y1, nodes.size() - 1, alpha);
        resolveCollisionsTopToBottom(nodes, y0, 0, alpha);
    }

    // Push any overlapping nodes down.
    private void resolveCollisionsTopToBottom(List<Node<I>> nodes, double y, int i, double alpha) {
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
    private void resolveCollisionsBottomToTop(List<Node<I>> nodes, double y, int i, double alpha) {
        for (; i >= 0; --i) {
            var node = nodes.get(i);
            double dy = (node.y1() - y) * alpha;
            if (dy > 1e-6) {
                node.setVerticalPosition(node.y0() - dy, node.y1() - dy);
            }
            y = node.y0() - py;
        }
    }

    private void reorderNodeLinks(Node<I> node) {
        final List<Link<I>> sourceLinks = node.sourceLinks();
        final List<Link<I>> targetLinks = node.targetLinks();
        if (linkSort == null) {
            targetLinks.stream().map(link -> link.source().sourceLinks()).forEach(links -> {
                links.sort(ascendingTargetBreadth);
            });
            sourceLinks.stream().map(link -> link.target().targetLinks()).forEach(links -> {
                links.sort(ascendingSourceBreadth);
            });
        }
    }

    private void reorderLinks(List<Node<I>> nodes) {
        if (linkSort == null) {
            for (Node<I> node : nodes) {
                node.sourceLinks().sort(ascendingTargetBreadth);
                node.targetLinks().sort(ascendingSourceBreadth);
            }
        }
    }

    // Returns the target.y0 that would produce an ideal link from source to target.
    private double targetTop(Node<I> source, Node<I> target) {
        double y = source.y0() - (source.sourceLinks().size() - 1) * py / 2;

        for (var link : source.sourceLinks()) {
            var node = link.target();
            double width = link.width();

            if (node == target) break;
            y += width + py;
        }

        for (var link : target.targetLinks()) {
            var node = link.source();
            double width = link.width();

            if (node == source) break;
            y -= width;
        }
        return y;
    }

    // Returns the source.y0 that would produce an ideal link from source to target.
    private double sourceTop(Node<I> source, Node<I> target) {
        double y = target.y0() - (target.targetLinks().size() - 1) * py / 2;

        for (var link : target.targetLinks()) {
            var node = link.source();
            double width = link.width();

            if (node == source) break;
            y += width + py;
        }

        for (var link : source.sourceLinks()) {
            var node = link.target();
            double width = link.width();

            if (node == target) break;
            y -= width;
        }

        return y;
    }

    private void computeLinkBreadths(Graph<I> graph) {
        List<? extends Node<I>> nodes = graph.nodes();
        for (Node<I> node : nodes) {
            double y0 = node.y0();
            double y1 = y0;
            for (Link<I> link : node.sourceLinks()) {
                link.y0(y0 + link.width() / 2);
                y0 += link.width();
            }
            for (var link : node.targetLinks()) {
                link.y1(y1 + link.width() / 2);
                y1 += link.width();
            }
        }
    }

    public static class Size {
        private final double width;
        private final double height;

        public Size(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public double width() {
            return width;
        }

        public double height() {
            return height;
        }
    }

    public static class Extent {
        private final double x0;
        private final double y0;
        private final double x1;
        private final double y1;

        public Extent(double x0, double y0, double x1, double y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        public double x0() {
            return x0;
        }

        public double x1() {
            return x1;
        }

        public double y0() {
            return y0;
        }

        public double y1() {
            return y1;
        }
    }
}
