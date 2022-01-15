module d3fx.demo {
    requires javafx.graphics;
    requires javafx.controls;
    requires d3fx.sankey;
    requires d3fx.sankeychart;
    requires commons.csv;
    opens ru.vzotov.d3fx.demo to javafx.graphics;
}
