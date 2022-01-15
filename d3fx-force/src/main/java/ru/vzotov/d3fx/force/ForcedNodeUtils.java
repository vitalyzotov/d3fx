package ru.vzotov.d3fx.force;

import javafx.scene.Node;

public abstract class ForcedNodeUtils {

    public static <N extends ForcedNode<?>> N makeDraggable(ForceAnimation<N> animation, final N fn) {
        final Node control = fn.getControl();

        control.setOnMousePressed(event -> {
            fn.mouseX = event.getSceneX();
            fn.mouseY = event.getSceneY();

            fn.fx = fn.getX();
            fn.fy = fn.getY();
            animation.alphaTarget(0.3).playFromStart();
        });

        control.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - fn.mouseX;
            double deltaY = event.getSceneY() - fn.mouseY;

            fn.fx += deltaX;
            fn.fy += deltaY;

            fn.mouseX = event.getSceneX();
            fn.mouseY = event.getSceneY();
        });

        control.setOnMouseReleased(event -> {
            fn.fx = null;
            fn.fy = null;
            animation.alphaTarget(0);
        });

        return fn;
    }

}
