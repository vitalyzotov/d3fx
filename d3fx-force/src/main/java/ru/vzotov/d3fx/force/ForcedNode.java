package ru.vzotov.d3fx.force;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;

public class ForcedNode<D extends Node> {

    private final D control;

    public int index = 0;
    double vx = 0d;
    double vy = 0d;
    public Double fx = null;
    public Double fy = null;
    public double mouseX = 0d;
    public double mouseY = 0d;

    /**
     * X coord
     */
    private final DoubleProperty x = new SimpleDoubleProperty(this, "x", 0d);

    public double getX() {
        return x.get();
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public void setX(double x) {
        this.x.set(x);
    }

    /**
     * Y coord
     */
    private final DoubleProperty y = new SimpleDoubleProperty(this, "y", 0d);

    public double getY() {
        return y.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public ForcedNode(D control) {
        this.control = control;
        this.control.getProperties().put(ForcedNode.class.getName(), this);
        this.xProperty().bindBidirectional(this.control.translateXProperty());
        this.yProperty().bindBidirectional(this.control.translateYProperty());
    }

    public D getControl() {
        return control;
    }

    public static <D extends Node,T extends ForcedNode<D>> T fromControl(D control) {
        return (T) control.getProperties().get(ForcedNode.class.getName());
    }
}
