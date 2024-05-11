package ru.vzotov.d3fx.selection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Provides functionality of data-driven transformation of JavaFX nodes.
 *
 * @param <E> type of JavaFx nodes
 * @param <D> data element type
 * @param <K> type of data identifiers
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FxSelector<E extends Node, D, K> {

    private static final String PROP_DATUM = FxSelector.class.getName() + ".DATUM";
    private final ObservableList<E> children;
    private final Function<D, K> keyMapper;
    private final ListChangeListener<? super D> dataChangeListener;
    private Timer bulkTimer;
    private final AtomicReference<TimerTask> future = new AtomicReference<>();

    private final ObjectProperty<ObservableList<D>> data = new ObjectPropertyBase<>() {
        private ObservableList<D> old;

        @Override
        protected void invalidated() {
            final ObservableList<D> current = getValue();
            // add remove listeners
            if (old != null) old.removeListener(dataChangeListener);
            if (current != null) current.addListener(dataChangeListener);
            // fire data change event if series are added or removed
            if (old != null || current != null) {
                final List<D> removed = (old != null) ? old : Collections.emptyList();
                final int toIndex = (current != null) ? current.size() : 0;
                // let data listener know all old data have been removed and new data that has been added
                if (toIndex > 0 || !removed.isEmpty()) {
                    dataChangeListener.onChanged(new NonIterableChange<>(0, toIndex, current) {
                        @Override
                        public List<D> getRemoved() {
                            return removed;
                        }
                    });
                }
            }
            old = current;
        }

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "data";
        }
    };

    private Appender<E, D> enter;

    private Appender<E, D> update;

    private Appender<E, D> exit;

    private Runnable doUpdate;

    private final Map<K, E> index = Collections.synchronizedMap(new HashMap<>());

    public FxSelector(ObservableList<E> children, Function<D, K> keyMapper) {
        this.children = children;
        this.keyMapper = keyMapper;

        dataChangeListener = c -> {
            while (c.next()) {
                if(c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //permutate
                        int p = c.getPermutation(i);
                        D datum = c.getList().get(p);
                        K key = keyMapper.apply(datum);
                        E node;
                        node = update.apply(index.get(key), datum, p, children);
                        index.put(key, node);
                    }
                } else {
                    Set<K> r = new HashSet<>();

                    if (c.wasRemoved()) {
                        for (D removed : c.getRemoved()) {
                            K key = keyMapper.apply(removed);
                            r.add(key);
                        }
                    }

                    if (c.wasAdded()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            D added = c.getList().get(i);
                            K key = keyMapper.apply(added);
                            E node;
                            if (r.contains(key)) {
                                node = update.apply(index.get(key), added, i, children);
                            } else {
                                node = enter.apply(index.get(key), added, i, children);
                            }
                            r.remove(key);
                            index.put(key, node);
                        }
                    }

                    if (c.wasRemoved()) {
                        int i = c.getFrom();
                        for (D removed : c.getRemoved()) {
                            K key = keyMapper.apply(removed);
                            if (r.contains(key)) {
                                E node = index.get(key);
                                exit.apply(node, removed, i, children);
                            }
                        }
                    }
                }
            }
        };
    }

    public FxSelector<E, D, K> bulkUpdate(Runnable doUpdate) {
        this.doUpdate = doUpdate;
        return this;
    }

    public FxSelector<E, D, K> bulkDelay(long timeout) {
        // Initialize timer to track bulk updates
        // Use daemon timer to allow application exit
        if(bulkTimer == null) bulkTimer = new Timer(true);
        var f = future.get();
        if(f != null) f.cancel();

        if(doUpdate != null) {
            f = new TimerTask() {
                @Override
                public void run() {
                    future.set(null);
                    doUpdate.run();
                    bulkTimer.purge();
                }
            };
            bulkTimer.schedule(f, timeout);

            future.set(f);
        }
        return this;
    }

    public FxSelector<E, D, K> updateAll() {
        ObservableList<D> data = this.data();
        dataChangeListener.onChanged(new NonIterableChange<D>(0, data.size(), data) {
            @Override
            public List<D> getRemoved() {
                return getAddedSubList();
            }
        });
        return this;
    }

    public List<E> nodes() {
        return children;
    }

    public ObservableList<D> data() {
        ObservableList<D> data = this.data.get();
        if (data == null) this.data(data = FXCollections.observableArrayList());
        return data;
    }

    public FxSelector<E, D, K> data(ObservableList<D> data) {
        this.data.set(data);
        return this;
    }

    public FxSelector<E, D, K> enter(Appender<E, D> enter) {
        this.enter = enter;
        return this;
    }

    public FxSelector<E, D, K> update(Appender<E, D> update) {
        this.update = update;
        return this;
    }

    public FxSelector<E, D, K> exit(Appender<E, D> remove) {
        this.exit = remove;
        return this;
    }

    @SuppressWarnings("unchecked")
    D datum(Node node) {
        return (D) node.getProperties().get(PROP_DATUM);
    }

    void datum(Node node, D datum) {
        node.getProperties().put(PROP_DATUM, datum);
    }

    public interface Appender<E extends Node, D> {
        E apply(E node, D d, int i, List<E> nodes);
    }

}
