package ru.vzotov.d3fx.sankeychart;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import ru.vzotov.d3fx.sankey.Align;
import ru.vzotov.d3fx.sankey.Alignment;
import ru.vzotov.d3fx.sankey.Link;
import ru.vzotov.d3fx.sankey.Node;
import ru.vzotov.d3fx.sankey.Sankey;
import ru.vzotov.d3fx.sankey.Sankey.ValueCalculator;
import ru.vzotov.d3fx.sankey.SimpleNode;
import ru.vzotov.d3fx.selection.FxSelector;
import ru.vzotov.d3fx.shape.LinkBuilder;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * The SankeyPlot class is a custom JavaFX component designed for visualizing data flow between different stages
 * or entities using a Sankey diagram.
 * This class extends JavaFX's Region and integrates various features such as dynamic node and link creation,
 * tooltips, and customizable styling and layout properties.
 * <p>
 * The class can dynamically handle changes in data, allowing for updates to nodes and links in the Sankey diagram.
 * <p>
 * Tooltips appear when hovering over nodes and links, providing additional information dynamically.
 * <p>
 * Users can customize node width, padding, and alignment through properties.
 * <p>
 * The layout updates automatically in response to changes in the component's size.
 *
 * @param <I> type of node identifier
 */
@SuppressWarnings("unused")
public class SankeyPlot<I, V extends Comparable<V>> extends Region {

    private static final String DEFAULT_STYLE_CLASS = "sankey-plot";
    private static final String CHART_STYLE_CLASS = "sankey-chart";
    private static final String STATUS_BAR_STYLE_CLASS = "sankey-status";
    private static final String HEADER_STYLE_CLASS = "sankey-header";

    private final Label statusBar = new Label();

    private final Pane header = new Pane();
    private final Pane chartContent = new Pane();

    private final Sankey<I, V> sankey;

    /**
     * Cache for the totals of all columns.
     */
    private final Map<Integer, V> columnTotals = new HashMap<>();
    private double columnWidth = 0.0;

    private final ObservableList<Data<I, V>> nodes = FXCollections.observableArrayList();
    private final ObservableList<Link<I, V>> links = FXCollections.observableArrayList();
    private final ObservableList<Column<V>> columns = FXCollections.observableArrayList();

    private final FxSelector<javafx.scene.Node, Data<I, V>, I> nodeSelector;
    private final FxSelector<javafx.scene.Node, Link<I, V>, I> linkSelector;
    private final FxSelector<javafx.scene.Node, Column<V>, Integer> columnSelector;

    private final ValueCalculator<V> calculator;

    public Map<Integer, V> getColumnTotals() {
        return columnTotals;
    }

    public ObservableList<Data<I, V>> getNodes() {
        return nodes;
    }

    public ObservableList<Link<I, V>> getLinks() {
        return links;
    }


    public void setData(List<Data<I, V>> nodes, List<Link<I, V>> links) {
        getLinks().clear();
        getNodes().setAll(nodes);
        getLinks().setAll(links);
        updateSankey();
    }

    private void onMouseEnteredNode(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof javafx.scene.Node sourceNode) {
            @SuppressWarnings("unchecked")
            Data<I, V> data = (Data<I, V>) sourceNode.getProperties().get("data");
            if (data != null) {
                statusBar.setText(String.valueOf(data.id()));
            }
        }
    }

    private void onMouseEnteredLink(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof javafx.scene.Node) {
            //noinspection unchecked
            DataLink<I, V> link = (DataLink<I, V>) ((javafx.scene.Node) source).getProperties().get("data");
            if (link != null) {
                statusBar.setText(String.format("%s -> %s; %.2f",
                        link.source() == null ? link.sourceKey() : ((Data<I, V>) link.source()).getName(),
                        link.target() == null ? link.targetKey() : ((Data<I, V>) link.target()).getName(),
                        link.value() == null ? null : calculator().toDouble(link.value())));
            }
        }
    }

    private void updateSankey() {
        final double top = chartContent.snappedTopInset();
        final double left = chartContent.snappedLeftInset();
        final double bottom = chartContent.snappedBottomInset();
        final double right = chartContent.snappedRightInset();
        final double width = chartContent.getWidth();
        final double height = chartContent.getHeight();

        sankey.setSize(width - left - right, height - top - bottom);
        if (!getNodes().isEmpty() && !getLinks().isEmpty()) {
            sankey.sankey(getNodes(), getLinks());
            columnWidth = sankey.computeLayerWidth();
            columnTotals.clear();
            var total = Collectors.<Data<I, V>, V>reducing(null,
                    node -> node == null ? null : node.value(), calculator()::sum);
            final Map<Integer, V> newColumns = getNodes().stream().collect(groupingBy(Node::layer, total));

            ListIterator<Column<V>> itr = this.columns.listIterator();
            while (itr.hasNext()) {
                Column<V> column = itr.next();
                V newValue = newColumns.computeIfAbsent(column.index(), i -> {
                    itr.remove();
                    return null;
                });
                if (newValue != null) {
                    itr.set(new Column<>(column.index(), newValue));
                    this.columnTotals.put(column.index(), newValue);
                }
                newColumns.remove(column.index());
            }
            newColumns.forEach((i, v) -> {
                Column<V> column = new Column<>(i, v);
                this.columns.add(column);
                this.columnTotals.put(column.index(), v);
            });
        }
        nodeSelector.updateAll();
        linkSelector.updateAll();
    }

    public SankeyPlot(BinaryOperator<I> idAggregator, ValueCalculator<V> calculator) {
        this(idAggregator, calculator, Label::new, Label::new, Label::new);
    }

    public SankeyPlot(BinaryOperator<I> idAggregator,
                      ValueCalculator<V> calculator,
                      Supplier<Label> nameLabelFactory,
                      Supplier<Label> valueLabelFactory,
                      Supplier<Label> columnLabelFactory) {
        this.calculator = calculator;
        this.sankey = new Sankey<>(calculator);
        Group chart = new Group();

        // configure style and areas
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        chartContent.getStyleClass().add(CHART_STYLE_CLASS);
        chartContent.setManaged(false);
        chartContent.getChildren().add(chart);
        getChildren().add(chartContent);

        statusBar.getStyleClass().add(STATUS_BAR_STYLE_CLASS);
        statusBar.setManaged(false);
        getChildren().add(statusBar);

        header.getStyleClass().add(HEADER_STYLE_CLASS);
        header.setManaged(false);
        getChildren().add(header);

        layoutBoundsProperty().addListener(it -> updateSankey());

        // configure selectors
        nodeSelector = new FxSelector<>(chart.getChildren(), Data<I, V>::id)
                .enter((node, d, idx, children) -> {
                    Rectangle rect = new Rectangle(d.x0(), d.y0(), d.x1() - d.x0(), d.y1() - d.y0());
                    rect.getStyleClass().setAll("node", "default-color" + (d.index() % 8));
                    rect.getProperties().put("data", d);
                    children.add(rect);
                    rect.setOnMouseEntered(this::onMouseEnteredNode);

                    boolean isFirstLayer = d.layer() == 0;

                    Label nameLabel = nameLabelFactory.get();
                    nameLabel.textProperty().bind(d.nameProperty());
                    bindLabelX(nameLabel, rect, !isFirstLayer);
                    bindLabelY(nameLabel, rect);
                    rect.getProperties().put("label", nameLabel);
                    children.add(nameLabel);

                    Label valueLabel = valueLabelFactory.get();
                    valueLabel.setText(getValueLabelMapping().apply(d));
                    bindLabelX(valueLabel, rect, isFirstLayer);
                    bindLabelY(valueLabel, rect);
                    rect.getProperties().put("valueLabel", valueLabel);
                    children.add(valueLabel);

                    return rect;
                })
                .update((node, d, idx, children) -> {
                    Rectangle rect = (Rectangle) node;
                    rect.getStyleClass().setAll("node", "default-color" + (d.index() % 8));

                    boolean isFirstLayer = d.layer() == 0;

                    Label nameLabel = (Label) node.getProperties().get("label");
                    bindLabelX(nameLabel, rect, !isFirstLayer);

                    Label valueLabel = (Label) node.getProperties().get("valueLabel");
                    valueLabel.setText(getValueLabelMapping().apply(d));
                    bindLabelX(valueLabel, rect, isFirstLayer);

                    rect.setX(d.x0());
                    rect.setY(d.y0());
                    rect.setWidth(d.x1() - d.x0());
                    rect.setHeight(d.y1() - d.y0());

                    return rect;
                })
                .exit((node, d, idx, children) -> {
                    javafx.scene.Node label = (javafx.scene.Node) node.getProperties().get("label");
                    if (label != null) {
                        children.remove(label);
                    }
                    label = (javafx.scene.Node) node.getProperties().get("valueLabel");
                    if (label != null) {
                        children.remove(label);
                    }
                    children.remove(node);
                    return null;
                });

        final LinkBuilder linkBuilder = new LinkBuilder();
        final Function<Link<I, V>, I> linkKeyMapper = (Link<I, V> d) -> idAggregator.apply(d.sourceKey(), d.targetKey());
        linkSelector = new FxSelector<>(chart.getChildren(), linkKeyMapper)
                .enter((node, d, idx, children) -> {
                    var link = (DataLink<I, V>) d;
                    final CubicCurve curve = linkBuilder
                            .from(link.startXProperty(), link.startYProperty())
                            .to(link.endXProperty(), link.endYProperty())
                            .linkHorizontal();
                    curve.getProperties().put("data", link);
                    curve.setOnMouseEntered(this::onMouseEnteredLink);
                    curve.setStrokeLineCap(StrokeLineCap.BUTT);
                    curve.setStroke(Color.TOMATO);
                    curve.setStrokeWidth(Math.max(1, d.width()));
                    children.add(0, curve);
                    return curve;
                })
                .update((node, d, idx, children) -> {
                    var link = (DataLink<I, V>) d;
                    link.setStartX(d.source().x1());
                    link.setStartY(d.y0());
                    link.setEndX(d.target().x0());
                    link.setEndY(d.y1());

                    CubicCurve curve = (CubicCurve) node;
                    curve.setStrokeWidth(Math.max(1, d.width()));
                    curve.toBack();

                    return node;
                })
                .exit((node, d, idx, children) -> {
                    children.remove(node);
                    return null;
                });

        columnSelector = new FxSelector<>(header.getChildren(), (Column<V> d) -> d.index())
                .enter((node, d, idx, children) -> {
                    Label ctl = columnLabelFactory.get();
                    getUpdateColumnLabelCommand().accept(d, ctl);
                    children.add(ctl);
                    ctl.relocate(d.index() * columnWidth, header.snappedTopInset());
                    return ctl;
                })
                .update((node, d, idx, children) -> {
                    var ctl = (Label) node;
                    getUpdateColumnLabelCommand().accept(d, ctl);
                    ctl.relocate(d.index() * columnWidth, header.snappedTopInset());
                    return node;
                })
                .exit((node, d, idx, children) -> {
                    children.remove(node);
                    return null;
                });

        nodeSelector.data(nodes);
        linkSelector.data(links);
        columnSelector.data(columns);

        nodeWidth.setValue(36);
        nodePadding.setValue(8);
        nodeAlign.setValue(Align.left());
    }

    private void bindLabelX(Label label, Rectangle rect, boolean alignLeft) {
        if (label != null) {
            label.translateXProperty().unbind();
            if (alignLeft) {
                label.translateXProperty().bind(rect.xProperty().subtract(label.widthProperty()).subtract(20.0));
            } else {
                label.translateXProperty().bind(rect.xProperty().add(rect.widthProperty()).add(20.0));
            }
        }
    }

    private void bindLabelY(Label label, Rectangle rect) {
        label.translateYProperty().bind(rect.yProperty().add(rect.heightProperty().subtract(label.heightProperty()).divide(2.0)));
    }

    private ValueCalculator<V> calculator() {
        return calculator;
    }

    /**
     * Maps value to its label
     */
    private final ObjectProperty<Function<Data<I, V>, String>> valueLabelMapping =
            new SimpleObjectProperty<>(this, "valueLabelMapping", data -> {
                V sum = getColumnTotals().computeIfAbsent(0, i -> null);
                Double percent = Optional.ofNullable(calculator().divide(data.value(), sum))
                        .map(f -> f * 100.0).orElse(null);
                return String.format("%.2f (%.2f%%)", calculator().toDouble(data.value()), percent);
            });

    public Function<Data<I, V>, String> getValueLabelMapping() {
        return valueLabelMapping.get();
    }

    public ObjectProperty<Function<Data<I, V>, String>> valueLabelMappingProperty() {
        return valueLabelMapping;
    }

    public void setValueLabelMapping(Function<Data<I, V>, String> valueLabelMapping) {
        this.valueLabelMapping.set(valueLabelMapping);
    }

    /**
     * Command to update column label
     */
    private final ObjectProperty<BiConsumer<Column<V>, Label>> updateColumnLabelCommand =
            new SimpleObjectProperty<>((column, label) -> label.setText(String.valueOf(column.value()))) {
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };

    public BiConsumer<Column<V>, Label> getUpdateColumnLabelCommand() {
        return updateColumnLabelCommand.get();
    }

    public ObjectProperty<BiConsumer<Column<V>, Label>> updateColumnLabelCommandProperty() {
        return updateColumnLabelCommand;
    }

    public void setUpdateColumnLabelCommand(BiConsumer<Column<V>, Label> updateColumnLabelCommand) {
        this.updateColumnLabelCommand.set(updateColumnLabelCommand);
    }

    /**
     * Nodes alignment
     */
    private final ObjectProperty<Alignment> nodeAlign = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            sankey.setNodeAlign(get());
        }
    };

    public Alignment getNodeAlign() {
        return nodeAlign.get();
    }

    public ObjectProperty<Alignment> nodeAlignProperty() {
        return nodeAlign;
    }

    public void setNodeAlign(Alignment nodeAlign) {
        this.nodeAlign.set(nodeAlign);
    }

    /**
     * Node width
     */
    private final DoubleProperty nodeWidth = new SimpleDoubleProperty(this, "nodeWidth", 0) {
        @Override
        protected void invalidated() {
            sankey.setNodeWidth(get());
        }
    };

    public double getNodeWidth() {
        return nodeWidth.get();
    }

    public DoubleProperty nodeWidthProperty() {
        return nodeWidth;
    }

    public void setNodeWidth(double nodeWidth) {
        this.nodeWidth.set(nodeWidth);
    }

    /**
     * Node padding
     */
    private final DoubleProperty nodePadding = new SimpleDoubleProperty(this, "nodePadding", 0) {
        @Override
        protected void invalidated() {
            sankey.setNodePadding(get());
        }
    };

    public double getNodePadding() {
        return nodePadding.get();
    }

    public DoubleProperty nodePaddingProperty() {
        return nodePadding;
    }

    public void setNodePadding(double nodePadding) {
        this.nodePadding.set(nodePadding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        final double top = snappedTopInset();
        final double left = snappedLeftInset();
        final double bottom = snappedBottomInset();
        final double right = snappedRightInset();
        final double width = getWidth();
        final double height = getHeight();
        final double contentWidth = width - left - right;

        double y = top;
        double h = statusBar.prefHeight(contentWidth);
        statusBar.resizeRelocate(left, y, contentWidth, h);
        y += h;

        h = header.prefHeight(contentWidth);
        header.resizeRelocate(left, y, contentWidth, h);
        y+=h;

        chartContent.resizeRelocate(left, y, contentWidth, height - bottom - y);
        updateSankey();
    }

    public static class Data<I, V> extends SimpleNode<I, V> {
        public Data(I id, V value, String name) {
            super(id, value);
            setName(name);
        }

        private final StringProperty name = new SimpleStringProperty(this, "name");

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }
    }

    public static class DataLink<I, V> extends Link<I, V> {

        private final DoubleProperty startX = new SimpleDoubleProperty(this, "startX", 0);
        private final DoubleProperty startY = new SimpleDoubleProperty(this, "startY", 0);
        private final DoubleProperty endX = new SimpleDoubleProperty(this, "endX", 0);
        private final DoubleProperty endY = new SimpleDoubleProperty(this, "endY", 0);

        public double getStartX() {
            return startX.get();
        }

        public DoubleProperty startXProperty() {
            return startX;
        }

        public void setStartX(double startX) {
            this.startX.set(startX);
        }

        public double getStartY() {
            return startY.get();
        }

        public DoubleProperty startYProperty() {
            return startY;
        }

        public void setStartY(double startY) {
            this.startY.set(startY);
        }

        public double getEndX() {
            return endX.get();
        }

        public DoubleProperty endXProperty() {
            return endX;
        }

        public void setEndX(double endX) {
            this.endX.set(endX);
        }

        public double getEndY() {
            return endY.get();
        }

        public DoubleProperty endYProperty() {
            return endY;
        }

        public void setEndY(double endY) {
            this.endY.set(endY);
        }

        public DataLink(I source, I target, V value) {
            super(source, target, value);
        }
    }

    public record Column<V>(int index, V value) {
    }
}
