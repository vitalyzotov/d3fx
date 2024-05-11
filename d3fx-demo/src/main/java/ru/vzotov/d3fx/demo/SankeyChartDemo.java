package ru.vzotov.d3fx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import ru.vzotov.d3fx.sankey.Link;
import ru.vzotov.d3fx.sankey.Sankey;
import ru.vzotov.d3fx.sankeychart.SankeyPlot;
import ru.vzotov.d3fx.sankeychart.SankeyPlot.Data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SankeyChartDemo extends Application {

    public static final Sankey.ValueCalculator<Double> DOUBLE_CALCULATOR = new Sankey.NumberValueCalculator<>(Double::sum);

    private static final double SCENE_HEIGHT = 1080;
    private static final double SCENE_WIDTH = 1920;
    private static final Supplier<Label> GLOW_LABEL = () -> {
        Label label = new Label();
        label.setEffect(new Glow(1.0));
        return label;
    };

    private static List<Data<String, Double>> nodes;

    private static List<Link<String, Double>> links;

    public static void main(String[] args) throws IOException {
        try (Reader in = new InputStreamReader(Objects.requireNonNull(SankeyChartDemo.class.getResourceAsStream("/energy.csv")), StandardCharsets.UTF_8)) {
            final CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.Predefined.Default.getFormat())
                    .setDelimiter(',')
                    .setTrailingDelimiter(false)
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();
            Iterable<CSVRecord> records = csvFormat.parse(in);
            final DecimalFormat decimals = new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            links = StreamSupport.stream(records.spliterator(), false).map(record -> {
                try {
                    return (Link<String, Double>) new SankeyPlot.DataLink<>(
                            record.get("source"),
                            record.get("target"),
                            decimals.parse(record.get("value")).doubleValue()
                    );
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        }

        nodes = links.stream().<Data<String, Double>>mapMulti((link, consumer) -> {
                    consumer.accept(new DemoNode(
                            link.sourceKey(),
                            link.value(),
                            link.sourceKey()
                    ));
                    consumer.accept(new DemoNode(
                            link.targetKey(),
                            link.value(),
                            link.targetKey()
                    ));
                })
                .collect(Collectors.groupingBy(Data::id,
                        Collectors.reducing((a, b) ->
                                new DemoNode(a.id(), a.value().doubleValue() + b.value().doubleValue(), a.getName()))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sankey demo");

        final Pane rootPane = new StackPane();

        final SankeyPlot<String, Double> chart =
                new SankeyPlot<>(String::concat, DOUBLE_CALCULATOR, GLOW_LABEL, GLOW_LABEL, Label::new);
        chart.setData(nodes, links);

        final BorderPane borderPane = new BorderPane(chart);
        rootPane.getChildren().add(borderPane);

        final Scene scene = new Scene(rootPane, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add("/sankey.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private static class DemoNode extends Data<String, Double> {
        public DemoNode(String id, Double value, String name) {
            super(id, value, name);
        }
    }
}
