package ru.vzotov.d3fx.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import ru.vzotov.d3fx.force.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The {@code ForcesDemo} class demonstrates the application of simulated physical forces on elements
 * in a JavaFX application to create dynamic and interactive visualizations. This class showcases
 * the use of force-directed graphs and animations to represent complex systems such as gravitational
 * fields or electrical forces in an educational context.
 * <p>
 * Key features demonstrated include:
 * - Collision detection and resolution using {@code CollideForce}.
 * - Simulated physical links between nodes with {@code LinkForce}.
 * - Attractive and repulsive forces among nodes using {@code ManyBodyForce}.
 * - Stabilization of nodes at specific positions using {@code XForce} and {@code YForce}.
 */
public class ForcesDemo extends Application {

    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;

    private Pane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Forces demo");

        root = new SimpleCanvas();

        final AtomicReference<ForceAnimation<ForcedNode<Node>>> animation = new AtomicReference<>();

        final List<ForcedNode<Node>> controls = IntStream.range(0, 10)
                .mapToObj(i -> createNode(i, animation::get))
                .toList();

        final ObservableList<ForcedNode<Node>> linked = IntStream.rangeClosed(1, 5).mapToObj(controls::get)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final ObservableList<ForcedNode<Node>> notLinked = IntStream.of(0, 6, 7, 8, 9).mapToObj(controls::get)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        animation.set(buildAnimation(controls, linked, notLinked));

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.show();
        animation.get().playFromStart();

    }

    private ForceAnimation<ForcedNode<Node>> buildAnimation(List<ForcedNode<Node>> controls,
                                                            ObservableList<ForcedNode<Node>> linked,
                                                            ObservableList<ForcedNode<Node>> notLinked) {
        final ForceAnimation<ForcedNode<Node>> result = new ForceAnimation<>(controls);
        result.getForces().add(new CollideForce<>(result.getNodes(), (node) -> 30d));
        result.getForces().add(new LinkForce<>(
                result.getNodes(),
                List.of(
                        createLink(controls, 0, 1),
                        createLink(controls, 0, 2),
                        createLink(controls, 0, 3),
                        createLink(controls, 0, 4),
                        createLink(controls, 0, 5)
                ), (link) -> 100d
        ));

        result.getForces().add(new LinkForce<>(
                result.getNodes(),
                List.of(
                        createLink(controls, 1, 2),
                        createLink(controls, 2, 3),
                        createLink(controls, 3, 4),
                        createLink(controls, 4, 5)
                ),
                (link) -> 100d
        ));
        result.getForces().add(new ManyBodyForce<>(
                result.getNodes(),
                (node) -> -200d,
                1, Double.POSITIVE_INFINITY
        ));
        result.getForces().add(new XForce<>(notLinked, (node) -> SCENE_WIDTH / 2));
        result.getForces().add(new YForce<>(notLinked, (node) -> SCENE_HEIGHT / 2));
        result.getForces().add(new YForce<>(linked, (node) -> SCENE_HEIGHT / 2 + 100, (node) -> 2d));
        return result;
    }

    private static final double[] INIT_X = new double[]{
            1316,
            252,
            1175,
            268,
            125,
            252,
            -325,
            378,
            130,
            -144
    };

    private static final double[] INIT_Y = new double[]{
            649,
            450,
            296,
            358,
            57,
            706,
            550,
            42,
            -80,
            351
    };

    private Link<Node, ForcedNode<Node>> createLink(List<ForcedNode<Node>> controls, int sourceIndex, int targetIndex) {
        final Line line = new Line();
        final ForcedNode<Node> source = controls.get(sourceIndex);
        final ForcedNode<Node> target = controls.get(targetIndex);
        line.startXProperty().bind(source.xProperty());
        line.startYProperty().bind(source.yProperty());
        line.endXProperty().bind(target.xProperty());
        line.endYProperty().bind(target.yProperty());
        root.getChildren().add(line);
        return new Link<>(line, source, target);
    }

    public ForcedNode<Node> createNode(int index, Supplier<ForceAnimation<ForcedNode<Node>>> animation) {
        final Circle control = new Circle(15);
        control.getStyleClass().add("forced-circle");

        final ForcedNode<Node> node = new ForcedNode<>(control);
        control.setManaged(false);
        final double x = INIT_X[index];
        final double y = INIT_Y[index];

        control.setTranslateX(x);
        control.setTranslateY(y);

        ForcedNodeUtils.makeDraggable(animation, node);
        root.getChildren().add(control);
        return node;
    }

    public static class SimpleCanvas extends Pane {

    }
}
