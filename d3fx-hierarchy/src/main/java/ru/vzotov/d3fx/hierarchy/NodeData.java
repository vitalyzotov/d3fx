package ru.vzotov.d3fx.hierarchy;

import java.util.Collections;
import java.util.List;

public interface NodeData {
    default <D extends NodeData> List<D> children() {
        return Collections.emptyList();
    }

    default Double getValue() {
        return null;
    }

    default void setValue(Double value) {
        throw new RuntimeException("not valued");
    }
}
