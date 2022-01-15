package ru.vzotov.d3fx.hierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public final class HierarchyBuilder<D extends NodeData, N extends Node<D, N>> {

    private final Function<D, ? super N> nodeFactory;

    public HierarchyBuilder(Function<D, ? super N> nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public N hierarchy(D data) {
        return hierarchy(data, HierarchyBuilder::defaultChildren);
    }

    public N hierarchy(D data, Function<D, List<D>> children) {
        N root = (N) nodeFactory.apply(data);
        boolean valued = data.getValue() != null;
        if (valued) root.setValue(data.getValue());

        N node;
        Deque<N> nodes = new LinkedList<>(Collections.singletonList(root));
        N child;
        List<D> childs;
        int i, n;

        while ((node = nodes.poll()) != null) {
            if (valued) node.setValue(node.getData().getValue());
            if ((childs = children.apply(node.getData())) != null && (n = childs.size()) != 0) {
                node.setChildren(new ArrayList<>(Collections.nCopies(n, null)));
                for (i = n - 1; i >= 0; --i) {
                    child = (N)nodeFactory.apply(childs.get(i));
                    node.getChildren().set(i, child);
                    nodes.push(child);
                    child.setParent(node);
                    child.setDepth(node.getDepth() + 1);
                }
            }
        }

        return root.eachBefore(HierarchyBuilder::computeHeight);
    }

    private static <D extends NodeData> List<D> defaultChildren(D d) {
        return d.children();
    }

    private static <D extends NodeData, N extends Node<D, N>> void computeHeight(N node) {
        int height = 0;
        do node.setHeight(height);
        while ((node = node.getParent()) != null && (node.getHeight() < ++height));
    }
}
