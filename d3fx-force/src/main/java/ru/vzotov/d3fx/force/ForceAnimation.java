package ru.vzotov.d3fx.force;

import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.util.Collection;

public class ForceAnimation<N extends ForcedNode<?>> extends Transition {

    private static final double INITIAL_RADIUS = 10;
    private static final double INITIAL_ANGLE = Math.PI * (3 - Math.sqrt(5));
    private static final double VELOCITY_DECAY = 0.6;

    private static final double ALPHA_MIN = 0.001d;
    private static final double ALPHA_DECAY = 1 - Math.pow(ALPHA_MIN, 1d / 300d);

    private double alphaTarget = 0d;

    /**
     * Alpha
     */
    private double alpha = .99d;

    /**
     * @return small random value
     */
    static double jiggle() {
        return (Math.random() - 0.5) * 1e-6;
    }

    /**
     * Nodes
     */
    private final ObservableList<N> nodes;

    public ObservableList<N> getNodes() {
        return nodes;
    }

    /**
     * Forces
     */
    private final ObservableList<Force<N>> forces = FXCollections.observableArrayList();

    public ObservableList<Force<N>> getForces() {
        return forces;
    }

    public ForceAnimation() {
        this(FXCollections.observableArrayList());
    }

    /**
     * Default constructor
     */
    public ForceAnimation(ObservableList<N> nodes) {
        this.nodes = nodes;
        setRate(1);
        setCycleCount(INDEFINITE);
        setCycleDuration(Duration.millis(5000));

        this.nodes.addListener((ListChangeListener<? super N>) (c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        N node = c.getList().get(i);
                        initializeNode(node, i);
                    }
                }
            }
        });
    }

    public ForceAnimation(Collection<N> nodes) {
        this();
        this.nodes.setAll(nodes);
    }

    private void initializeNode(N node, int i) {
        node.index = i;
        if (node.fx != null) node.setX(node.fx);
        if (node.fy != null) node.setY(node.fy);
        if (Double.isNaN(node.getX()) || Double.isNaN(node.getY())) {
            double radius = INITIAL_RADIUS * Math.sqrt(0.5 + i), angle = i * INITIAL_ANGLE;
            node.setX(radius * Math.cos(angle));
            node.setY(radius * Math.sin(angle));
        }
        if (Double.isNaN(node.vx) || Double.isNaN(node.vy)) {
            node.vx = node.vy = 0;
        }
    }

    public double alphaTarget() {
        return alphaTarget;
    }

    public <F extends Force<N>> F force(F force) {
        getForces().add(force);
        return force;
    }

    public ForceAnimation<N> alpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public ForceAnimation<N> alphaTarget(double alphaTarget) {
        setDelay(Duration.ZERO);
        this.alphaTarget = alphaTarget;
        return this;
    }

    public void tick(int iterations) {
        for (var k = 0; k < iterations; ++k) {
            alpha += (alphaTarget - alpha) * ALPHA_DECAY;

            for (Force<N> force : getForces()) {
                force.force(alpha);
            }

            for (N node : nodes) {
                node.vx *= VELOCITY_DECAY;
                node.vy *= VELOCITY_DECAY;
                node.setX(node.fx != null ? node.fx : node.getX() + node.vx);
                node.setY(node.fy != null ? node.fy : node.getY() + node.vy);
            }
        }
    }

    @Override
    protected void interpolate(double frac) {
        tick(1);
        if (alpha < ALPHA_MIN) {
            stop();
        }
    }


}
