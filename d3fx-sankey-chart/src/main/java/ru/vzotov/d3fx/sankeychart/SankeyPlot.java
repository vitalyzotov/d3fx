package ru.vzotov.d3fx.sankeychart;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import ru.vzotov.d3fx.sankey.SimpleNode;
import ru.vzotov.d3fx.selection.FxSelector;
import ru.vzotov.d3fx.shape.LinkBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SankeyPlot extends Region {

    private static final String DEFAULT_STYLE_CLASS = "sankey-plot";

    private final Label tooltip = new Label();

    private final Pane chartContent = new Pane();

    private final Group chart = new Group();

    private Sankey<String> sankey = new Sankey<>();

    private final Map<Integer, Double> columns = new HashMap<>();

    private final ObservableList<Data> nodes = FXCollections.observableArrayList();

    private final FxSelector<javafx.scene.Node, Data, String> nodeSelector;

    private final FxSelector<javafx.scene.Node, Link<String>, String> linkSelector;

    public Map<Integer, Double> getColumns() {
        return columns;
    }

    public ObservableList<Data> getNodes() {
        return nodes;
    }

    private final ObservableList<Link<String>> links = FXCollections.observableArrayList();

    public ObservableList<Link<String>> getLinks() {
        return links;
    }

    private final ObjectProperty<Function<Data, String>> valueLabelMapping =
            new SimpleObjectProperty<>(this, "valueLabelMapping", data -> {
                double sum = getColumns().computeIfAbsent(0, i -> 0.0);
                return String.format("%.2f (%.2f%%)", data.value().doubleValue(), (data.value().doubleValue() / sum) * 100.0);
            });

    public Function<Data, String> getValueLabelMapping() {
        return valueLabelMapping.get();
    }

    public ObjectProperty<Function<Data, String>> valueLabelMappingProperty() {
        return valueLabelMapping;
    }

    public void setValueLabelMapping(Function<Data, String> valueLabelMapping) {
        this.valueLabelMapping.set(valueLabelMapping);
    }

    public void setData(List<Data> nodes, List<Link<String>> links) {
        getLinks().clear();
        getNodes().setAll(nodes);
        getLinks().setAll(links);
        updateSankey();
    }

    private void onMouseEnteredNode(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof javafx.scene.Node) {
            Data data = (Data) ((javafx.scene.Node) source).getProperties().get("data");
            if (data != null) {
                tooltip.setText(data.id());
            }
        }
    }

    private void onMouseEnteredLink(MouseEvent e) {
        Object source = e.getSource();
        if (source instanceof javafx.scene.Node) {
            DataLink link = (DataLink) ((javafx.scene.Node) source).getProperties().get("data");
            if (link != null) {
                tooltip.setText(String.format("%s -> %s; %.2f",
                        link.source() == null ? link.sourceKey() : ((Data) link.source()).getName(),
                        link.target() == null ? link.targetKey() : ((Data) link.target()).getName(),
                        link.value() == null ? null : link.value().doubleValue()));
            }
        }
    }

    private void updateSankey() {
        final double top = snappedTopInset();
        final double left = snappedLeftInset();
        final double bottom = snappedBottomInset();
        final double right = snappedRightInset();
        final double width = getWidth();
        final double height = getHeight();

        sankey.setSize(width - left - right, height - top - bottom);
        if (!getNodes().isEmpty() && !getLinks().isEmpty()) {
            sankey.sankey(getNodes(), getLinks());
            columns.clear();
            final Map<Integer, Double> columns = getNodes().stream()
                    .collect(Collectors.groupingBy(
                            Node::layer,
                            Collectors.summingDouble(d -> d.value().doubleValue())
                    ));
            this.columns.putAll(columns);
        }
        nodeSelector.updateAll();
        linkSelector.updateAll();
    }

    public SankeyPlot() {
        this(Label::new, Label::new);
    }

    public SankeyPlot(Supplier<Label> nameLabelConstructor, Supplier<Label> valueLabelConstructor) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        chartContent.getChildren().add(chart);

        layoutBoundsProperty().addListener(it -> updateSankey());

        getChildren().add(tooltip);

        nodeSelector = new FxSelector<>(chart.getChildren(), Data::id)
                .enter((node, d, idx, children) -> {
                    Rectangle rect = new Rectangle(d.x0(), d.y0(), d.x1() - d.x0(), d.y1() - d.y0());
                    rect.getStyleClass().setAll("node", "default-color" + (d.index() % 8));
                    rect.getProperties().put("data", d);
                    children.add(rect);
                    rect.setOnMouseEntered(this::onMouseEnteredNode);

                    boolean isFirstLayer = d.layer() == 0;

                    Label nameLabel = nameLabelConstructor.get();
                    nameLabel.textProperty().bind(d.nameProperty());
                    bindLabelX(nameLabel, rect, d, !isFirstLayer);
                    bindLabelY(nameLabel, rect, d);
                    rect.getProperties().put("label", nameLabel);
                    children.add(nameLabel);

                    Label valueLabel = valueLabelConstructor.get();
                    valueLabel.setText(getValueLabelMapping().apply(d));
                    bindLabelX(valueLabel, rect, d, isFirstLayer);
                    bindLabelY(valueLabel, rect, d);
                    rect.getProperties().put("valueLabel", valueLabel);
                    children.add(valueLabel);

                    return rect;
                })
                .update((node, d, idx, children) -> {
                    Rectangle rect = (Rectangle) node;
                    rect.getStyleClass().setAll("node", "default-color" + (d.index() % 8));

                    boolean isFirstLayer = d.layer() == 0;

                    Label nameLabel = (Label) node.getProperties().get("label");
                    bindLabelX(nameLabel, rect, d, !isFirstLayer);

                    Label valueLabel = (Label) node.getProperties().get("valueLabel");
                    valueLabel.setText(getValueLabelMapping().apply(d));
                    bindLabelX(valueLabel, rect, d, isFirstLayer);

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
        linkSelector = new FxSelector<>(chart.getChildren(), (Link<String> d) -> d.sourceKey() + d.targetKey())
                .enter((node, d, idx, children) -> {
                    DataLink link = (DataLink) d;
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
                    DataLink link = (DataLink) d;
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

        nodeSelector.data(nodes);
        linkSelector.data(links);

        nodeWidth.setValue(36);
        nodePadding.setValue(8);
        nodeAlign.setValue(Align.left());

        getChildren().add(chartContent);
    }

    private void bindLabelX(Label label, Rectangle rect, Data d, boolean alignLeft) {
        if (label != null) {
            label.translateXProperty().unbind();
            if (alignLeft) {
                label.translateXProperty().bind(rect.xProperty().subtract(label.widthProperty()).subtract(20.0));
            } else {
                label.translateXProperty().bind(rect.xProperty().add(rect.widthProperty()).add(20.0));
            }
        }
    }

    private void bindLabelY(Label label, Rectangle rect, Data d) {
        label.translateYProperty().bind(rect.yProperty().add(rect.heightProperty().subtract(label.heightProperty()).divide(2.0)));
    }

    /**
     * Nodes alignment
     */
    private final ObjectProperty<Alignment<String>> nodeAlign = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            sankey.setNodeAlign(get());
        }
    };

    public Alignment<String> getNodeAlign() {
        return nodeAlign.get();
    }

    public ObjectProperty<Alignment<String>> nodeAlignProperty() {
        return nodeAlign;
    }

    public void setNodeAlign(Alignment<String> nodeAlign) {
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
        updateSankey();
        final double top = snappedTopInset();
        final double left = snappedLeftInset();
        final double bottom = snappedBottomInset();
        final double right = snappedRightInset();
        final double width = getWidth();
        final double height = getHeight();

        chartContent.resizeRelocate(left, top, width - left - right, height - top - bottom);
        tooltip.resizeRelocate(0, 0, width, top);
    }

    public static class Data extends SimpleNode<String> {


        public Data(String id, Number value, String name) {
            super(id, value);
            setName(name);
        }

        private StringProperty name = new SimpleStringProperty(this, "name");

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

    public static class DataLink extends Link<String> {

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

        public DataLink(String source, String target, Number value) {
            super(source, target, value);
        }
    }
}
