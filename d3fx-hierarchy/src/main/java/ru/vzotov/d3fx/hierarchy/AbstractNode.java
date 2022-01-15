package ru.vzotov.d3fx.hierarchy;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractNode<D extends NodeData, N extends AbstractNode<D, N>> implements Node<D, N> {

    /**
     * the associated data, as specified to the constructor.
     */
    private D data;

    /**
     * zero for the root node, and increasing by one for each descendant generation.
     */
    private int depth;

    /**
     * zero for leaf nodes, and the greatest distance from any descendant leaf for internal nodes.
     */
    private int height;

    /**
     * the parent node, or null for the root node.
     */
    private N parent;

    /**
     * the summed value of the node and its descendants; optional, see node.sum and node.count.
     */
    private Double value;

    /**
     * an array of child nodes, if any; undefined for leaf nodes.
     */
    private List<N> children = null;

    public AbstractNode(D data) {
        this.data = data;
        depth = 0;
        height = 0;
        parent = null;
    }

    @Override
    public List<N> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<N> children) {
        this.children = children;
    }

    @Override
    public D getData() {
        return data;
    }

    void setData(D data) {
        this.data = data;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public N getParent() {
        return parent;
    }

    @Override
    public void setParent(N parent) {
        this.parent = parent;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    private void count(N node) {
        double sum = 0;

        List<N> children = node.getChildren();
        if (children == null || children.size() == 0) {
            sum = 1;
        } else {
            int i = children.size();
            while (--i >= 0) sum += children.get(i).getValue();
        }

        node.setValue(sum);
    }

    /**
     * Computes the number of leaves under this node and assigns it to node.value, and similarly for every descendant of node.
     * If this node is a leaf, its count is one.
     *
     * @return Returns this node.
     * @see #sum(Function)
     */
    public N count() {
        return this.eachAfter(this::count);
    }

    /**
     * Invokes the specified function for node and each descendant in breadth-first order,
     * such that a given node is only visited if all nodes of lesser depth have already been visited,
     * as well as all preceding nodes of the same depth.
     * The specified function is passed the current node.
     */
    public N each(Consumer<N> callback) {
        N node = (N) this;
        LinkedList<N> current;
        LinkedList<N> next = new LinkedList<>(Collections.singleton(node));
        List<N> children;
        int i, n;
        do {
            current = new LinkedList<>(next);
            Collections.reverse(current);

            next = new LinkedList<>();
            while ((node = current.poll()) != null) {
                callback.accept(node);
                children = node.getChildren();
                if (children != null) for (i = 0, n = children.size(); i < n; ++i) {
                    next.push(children.get(i));
                }
            }
        } while (!next.isEmpty());

        return (N) this;
    }

    /**
     * Invokes the specified function for node and each descendant in post-order traversal,
     * such that a given node is only visited after all of its descendants have already been visited.
     * The specified function is passed the current node.
     */
    public N eachAfter(Consumer<N> callback) {
        N node = (N) this;
        LinkedList<N> nodes = new LinkedList<>(Collections.singleton(node));
        LinkedList<N> next = new LinkedList<>();
        List<N> children;
        int i, n;
        while ((node = nodes.poll()) != null) {
            next.push(node);
            children = node.getChildren();
            if (children != null) for (i = 0, n = children.size(); i < n; ++i) {
                nodes.push(children.get(i));
            }
        }
        while ((node = next.poll()) != null) {
            callback.accept(node);
        }

        return (N) this;
    }

    /**
     * Invokes the specified function for node and each descendant in pre-order traversal,
     * such that a given node is only visited after all of its ancestors have already been visited.
     * The specified function is passed the current node.
     */
    public N eachBefore(Consumer<N> callback) {
        N node = (N) this;
        LinkedList<N> nodes = new LinkedList<>(Collections.singleton(node));
        List<N> children;
        int i;
        while ((node = nodes.poll()) != null) {
            callback.accept(node);
            children = node.getChildren();
            if (children != null) for (i = children.size() - 1; i >= 0; --i) {
                nodes.push(children.get(i));
            }
        }

        return (N) this;
    }

    /**
     * Evaluates the specified value function for this node and each descendant in post-order traversal,
     * and returns this node. The node.value property of each node is set to the numeric value returned
     * by the specified function plus the combined value of all children.
     * The function is passed the node’s data, and must return a non-negative number.
     * The value accessor is evaluated for node and every descendant, including internal nodes;
     * if you only want leaf nodes to have internal value, then return zero for any node with children.
     */
    public N sum(Function<D, Double> value) {
        return this.eachAfter((node) -> {
            Double v = value.apply(node.getData());
            double sum = v == null ? 0 : v;
            List<N> children = node.getChildren();
            if (children != null) {
                int i = children.size();
                while (--i >= 0) sum += children.get(i).getValue();
            }
            node.setValue(sum);
        });
    }

    /**
     * Sorts the children of this node, if any, and each of this node’s descendants’ children, in pre-order traversal
     * using the specified compare function, and returns this node.
     * The specified function is passed two nodes a and b to compare.
     * If a should be before b, the function must return a value less than zero;
     * if b should be before a, the function must return a value greater than zero;
     * otherwise, the relative order of a and b are not specified. See array.sort for more.
     * <p>
     * Unlike node.sum, the compare function is passed two nodes rather than two nodes’ data.
     * For example, if the data has a value property, this sorts nodes by the descending aggregate value
     * of the node and all its descendants, as is recommended for circle-packing
     */
    public N sort(Comparator<N> comparator) {
        return this.eachBefore((node) -> {
            if (node.getChildren() != null) {
                node.getChildren().sort(comparator);
            }
        });
    }

    private N leastCommonAncestor(N a, N b) {
        if (a == b) return a;
        LinkedList<N> aNodes = new LinkedList<>(a.ancestors()),
                bNodes = new LinkedList<>(b.ancestors());
        N c = null;
        a = aNodes.poll();
        b = bNodes.poll();
        while (a == b) {
            c = a;
            a = aNodes.poll();
            b = bNodes.poll();
        }
        return c;
    }

    /**
     * Returns the shortest path through the hierarchy from this node to the specified target node.
     * The path starts at this node, ascends to the least common ancestor of this node and the target node,
     * and then descends to the target node. This is particularly useful for hierarchical edge bundling.
     */
    public List<N> path(N target) {
        N start = (N) this;
        N ancestor = leastCommonAncestor(start, target);
        LinkedList<N> nodes = new LinkedList<>(Collections.singleton(start));
        while (start != ancestor) {
            start = start.getParent();
            nodes.push(start);
        }
        int k = nodes.size();
        while (target != ancestor) {
            nodes.addAll(k, Collections.singleton(target));
            target = target.getParent();
        }
        return nodes;
    }

    /**
     * Returns the array of ancestors nodes, starting with this node, then followed by each parent up to the root.
     */
    public List<N> ancestors() {
        N node = (N) this;
        LinkedList<N> nodes = new LinkedList<>(Collections.singleton(node));
        while ((node = node.getParent()) != null) {
            nodes.push(node);
        }
        return nodes;
    }

    /**
     * Returns the array of descendant nodes, starting with this node, then followed by each child in topological order.
     */
    public List<N> descendants() {
        final LinkedList<N> nodes = new LinkedList<>();
        this.each(nodes::push);
        return nodes;
    }

    /**
     * Returns the array of leaf nodes in traversal order; leaves are nodes with no children.
     */
    public List<N> leaves() {
        final LinkedList<N> leaves = new LinkedList<>();
        this.eachBefore(node -> {
            if (node.getChildren() == null) {
                leaves.push(node);
            }
        });
        return leaves;
    }

    /**
     * Returns an array of links for this node and its descendants,
     * where each link is an object that defines source and target properties.
     * The source of each link is the parent node, and the target is a child node.
     */
    public List<Link<D, N>> links() {
        N root = (N) this;
        LinkedList<Link<D, N>> links = new LinkedList<>();
        root.each(node -> {
            if (node != root) { // Don’t include the root’s parent, if any.
                links.push(new Link<>(node.getParent(), node));
            }
        });
        return links;
    }

    /**
     * Return a deep copy of the subtree starting at this node. (The returned deep copy shares the same data, however.)
     * The returned node is the root of a new tree; the returned node’s parent is always null and its depth is always zero.
     *
     * @return the root of a new tree
     */
    @SuppressWarnings("unchecked")
    public N copy(Function<D, N> nodeFactory) {
        return (N) new HierarchyBuilder<>(nodeFactory).hierarchy((D)this).eachBefore((node) -> {
            ((N) node).setData(((N) ((N) node).getData()).getData());
        });
    }

}
