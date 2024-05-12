# d3fx
Java port of D3.js library

## Getting Started

To include `d3fx` in your project, add the following dependency to your project's build file:

```xml
<!-- For Maven-based projects -->
<dependency>
    <groupId>ru.vzotov</groupId>
    <artifactId>d3fx</artifactId>
    <version>1.1</version>
</dependency>
```

## Examples

For detailed examples, please look `d3fx-demo` module.

## Available Modules

### d3fx-selection

The `d3fx-selection` module is designed to provide a flexible and efficient way to manipulate and bind data to JavaFX nodes, inspired by the D3.js's selection mechanism. This module allows developers to apply data-driven transformations to JavaFX scene graph elements, enabling dynamic and interactive visualizations.

**Key Features:**
- **Data Binding**: Easily bind data to JavaFX nodes and automatically manage updates, inserts, and removals based on data changes.
- **Transformation and Manipulation**: Apply transformations to JavaFX nodes based on bound data, facilitating complex visual effects and GUI manipulations.
- **Efficient Updates**: Minimize the overhead of updating the GUI by optimizing the changes to the scene graph, only modifying elements when their corresponding data changes.
- **Bulk Operations Support**: Includes functionality for delaying and batching updates, which can improve performance in scenarios with frequent data changes.
- **Integration with JavaFX Properties**: Seamlessly integrates with JavaFX properties and collections, making it easier to develop reactive applications.

This module is particularly useful for developers looking to create data-intensive and dynamic JavaFX applications where the UI needs to respond to underlying data changes efficiently and effectively.

```java
// Example of using d3fx-selection in a JavaFX application
FxSelector<Rectangle, DataPoint, String> selector = new FxSelector<>(pane.getChildren(), DataPoint::getId);
selector.enter((node, data, index, nodes) -> {
    Rectangle rect = new Rectangle();
    rect.setWidth(data.getWidth());
    rect.setHeight(data.getHeight());
    return rect;
});
selector.update((node, data, index, nodes) -> {
    node.setX(data.getX());
    node.setY(data.getY());
    return node;
});
selector.exit((node, data, index, nodes) -> {
    pane.getChildren().remove(node);
    return node;
});
selector.data(FXCollections.observableArrayList(dataPoints));
```

This example demonstrates how to set up a FxSelector for a list of Rectangle nodes where each rectangle represents a DataPoint. It shows how to handle new data, update existing nodes, and remove nodes no longer represented in the data.

Please find an advanced example demonstrating dynamic data selection in the `d3fx-demo` module. 
This example showcases how to effectively use transitions with `d3fx-selection` features:
![](/assets/images/selection_demo.gif)

### d3fx-sankey

The `d3fx-sankey` module provides a backend implementation for constructing Sankey diagrams without the need for JavaFX components. This module is ideal for applications that require Sankey diagram computations or data preparations without rendering them on a GUI.

**Key Features:**
- **Flexible Data Input**: Supports various forms of data input, making it versatile for different use cases.
- **High Performance**: Optimized for performance, ensuring quick computations even with large datasets.
- **Ease of Integration**: Can be easily integrated with other Java systems or frameworks for further processing or visualization.

### d3fx-sankey-chart

The `d3fx-sankey-chart` module is designed to facilitate the visualization of Sankey diagrams, which are used 
to depict the flow of data between different nodes in a network. These diagrams are particularly useful for 
representing energy or material transfers in a system.

**Key Features:**
- **SankeyPlot**: A JavaFX component that can be integrated into JavaFX applications to display Sankey diagrams with ease.
- **Interactive Elements**: Users can interact with the diagram to explore different paths and flows.
- **Customizable Styles**: Offers extensive customization options for colors, node sizes, and more to match the style of your application.

![](/assets/images/sankey_demo.png)

### d3fx-force
The d3fx-force module is a JavaFX library designed to simulate physical forces on nodes and their interactions within a graphical environment. This module is inspired by the force-directed graph layout capabilities of D3.js, enabling the creation of dynamic, interactive, and visually appealing layouts in JavaFX applications.

**Key Features:**

- **Force Simulation**: Provides a variety of forces such as gravitational, electrical, and collision forces to simulate realistic interactions among nodes.
- **Customizable Forces**: Allows developers to define and customize their own forces, adjusting parameters like strength, distance, and direction to achieve desired behaviors.
- **Integration with JavaFX**: Seamlessly integrates with JavaFX, making it easy to apply forces to any JavaFX Node. This integration allows for the direct manipulation of graphical elements based on the simulation of physical forces.
- **Dynamic Interaction**: Supports dynamic updates to the forces and nodes, enabling interactive applications where users can influence the behavior of the system in real-time.
- **Performance Optimized**: Utilizes efficient algorithms and data structures, such as quad-trees for collision detection, to ensure high performance even with complex simulations involving many nodes.

**Usage Scenarios:**

- **Interactive Graphs:** Ideal for creating interactive graphs where nodes can be dynamically added, removed, or rearranged by user interactions.
- **Educational Tools:** Useful in educational software to visually demonstrate concepts in physics, such as gravitational fields or electrical forces.
- **Animated Visualizations:** Can be used to create animated visualizations where elements organically move and settle based on simulated forces, providing a natural and engaging user experience.

Please find an example in the `d3fx-demo` module:

![](/assets/images/forces_demo.gif)