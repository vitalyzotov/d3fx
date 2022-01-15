package ru.vzotov.d3fx.shape;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.CubicCurve;

public class LinkBuilder {

    private ObservableDoubleValue startX;
    private ObservableDoubleValue startY;
    private ObservableDoubleValue endX;
    private ObservableDoubleValue endY;

    public LinkBuilder from(ObservableDoubleValue startX, ObservableDoubleValue startY) {
        this.startX = startX;
        this.startY = startY;
        return this;
    }

    public LinkBuilder to(ObservableDoubleValue endX, ObservableDoubleValue endY) {
        this.endX = endX;
        this.endY = endY;
        return this;
    }

    public LinkBuilder from(double startX, double startY) {
        return from(new SimpleDoubleProperty(startX), new SimpleDoubleProperty(startY));
    }

    public LinkBuilder to(double endX, double endY) {
        this.endX = new SimpleDoubleProperty(endX);
        this.endY = new SimpleDoubleProperty(endY);
        return this;
    }

    public CubicCurve linkHorizontal() {
        CubicCurve link = new CubicCurve();
        link.getStyleClass().setAll("link");
        if (startX != null) link.startXProperty().bind(startX);
        if (startY != null) link.startYProperty().bind(startY);

        link.controlX1Property().bind(link.startXProperty().add(link.endXProperty()).divide(2d));
        link.controlY1Property().bind(link.startYProperty());
        link.controlX2Property().bind(link.controlX1Property());
        link.controlY2Property().bind(link.endYProperty());

        if (endX != null) link.endXProperty().bind(endX);
        if (endY != null) link.endYProperty().bind(endY);
        return link;
    }
}
