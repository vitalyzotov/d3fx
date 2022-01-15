package ru.vzotov.d3fx.hierarchy;

public interface PointNode<D extends NodeData, N> extends Node<D, N> {

    double getX();

    double getY();

    void setX(double x);

    void setY(double y);
}
