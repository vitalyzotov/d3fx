package ru.vzotov.d3fx.force;

import javafx.collections.ObservableList;

public class CenterForce<N extends ForcedNode<?>> extends Force<N> {

    private final double centerX;

    private final double centerY;

    protected CenterForce(ObservableList<N> nodes, double centerX, double centerY) {
        super(nodes);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    @Override
    public void force(double alpha) {
        int n = nodes.size();
        double sx = 0;
        double sy = 0;

        for (N value : nodes) {
            sx += value.getX();
            sy += value.getY();
        }

        sx = sx / n - centerX;
        sy = sy / n - centerY;

        for (N value : nodes) {
            double tx = value.getX() - sx;
            double ty = value.getY() - sy;
            value.setX(tx);
            value.setY(ty);
        }
    }
}
