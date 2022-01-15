package ru.vzotov.d3fx.demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.vzotov.d3fx.selection.FxSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SelectionDemo extends Application {
    public static final double SCENE_WIDTH = 1024;
    public static final double SCENE_HEIGHT = 768;

    private static final Supplier<List<String>> DATA_FACTORY = () -> IntStream.rangeClosed('a', 'z')
            .mapToObj(i -> String.valueOf((char) i)).collect(Collectors.toCollection(ArrayList::new));

    private Pane rootPane;

    private Label infoLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Selection demo");

        rootPane = new Pane();
        rootPane.setStyle("-fx-font-size: 16;");

        infoLabel = new Label();
        infoLabel.setManaged(false);
        rootPane.getChildren().add(infoLabel);
        infoLabel.resizeRelocate(10, 10, 100, 50);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        final List<String> data = DATA_FACTORY.get();
        double startY = 100;

        final FxSelector<Node, String, String> selection = new FxSelector<>(rootPane.getChildren(), (String d) -> d)
                .enter((node, d, idx, nodes) -> {
                    Label label = new Label(d);
                    label.getStyleClass().add("my");

                    label.setStyle("-fx-text-fill: lime");
                    int i = d.charAt(0) - 'a';
                    label.setTranslateX(i * 32);
                    label.setTranslateY(startY - 30);

                    nodes.add(label);

                    TranslateTransition transition = new TranslateTransition(Duration.millis(750), label);
                    transition.setFromY(startY - 30);
                    transition.setToY(startY);
                    transition.play();

                    return label;
                })
                .update((node, d, idx, nodes) -> {
                    node.setStyle("-fx-text-fill: whitesmoke");
                    return node;
                })
                .exit((node, d, idx, nodes) -> {
                    node.setStyle("-fx-text-fill: brown");
                    TranslateTransition transition = new TranslateTransition(Duration.millis(750), node);
                    transition.setToY(startY + 30);
                    transition.setOnFinished(e -> {
                        nodes.remove(node);
                    });
                    transition.play();
                    return null;
                });
        selection.data().addAll(data);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(new KeyFrame(Duration.millis(3000), ae -> {
            selection.data().setAll(randomLetters());
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Scene scene = new Scene(rootPane, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add("/selection.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<String> randomLetters() {
        List<String> strings = DATA_FACTORY.get();
        Collections.shuffle(strings);

        strings = strings.subList(0, (int) Math.floor(6 + Math.random() * 20));
        strings.sort(String::compareTo);

        return strings;
    }
}
