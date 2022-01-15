package ru.vzotov.d3fx.hierarchy;

import java.util.List;
import java.util.function.Consumer;

public interface Node<D, N> extends NodeData {

    List<N> getChildren();

    void setChildren(List<N> children);

    D getData();

    N getParent();

    void setParent(N parent);

    int getDepth();

    void setDepth(int depth);

    int getHeight();

    void setHeight(int height);

    N eachBefore(Consumer<N> callback);
}
