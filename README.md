# d3fx
Java port of D3.js library

## Available Modules

### d3fx-sankey-chart

The `d3fx-sankey-chart` module is designed to facilitate the visualization of Sankey diagrams, which are used 
to depict the flow of data between different nodes in a network. These diagrams are particularly useful for 
representing energy or material transfers in a system.

**Key Features:**
- **SankeyPlot**: A JavaFX component that can be integrated into JavaFX applications to display Sankey diagrams with ease.
- **Interactive Elements**: Users can interact with the diagram to explore different paths and flows.
- **Customizable Styles**: Offers extensive customization options for colors, node sizes, and more to match the style of your application.

### d3fx-sankey

The `d3fx-sankey` module provides a backend implementation for constructing Sankey diagrams without the need for JavaFX components. This module is ideal for applications that require Sankey diagram computations or data preparations without rendering them on a GUI.

**Key Features:**
- **Flexible Data Input**: Supports various forms of data input, making it versatile for different use cases.
- **High Performance**: Optimized for performance, ensuring quick computations even with large datasets.
- **Ease of Integration**: Can be easily integrated with other Java systems or frameworks for further processing or visualization.

## Getting Started

To include `d3fx` in your project, add the following dependency to your project's build file:

```xml
<!-- For Maven-based projects -->
<dependency>
    <groupId>io.github.vitalyzotov</groupId>
    <artifactId>d3fx</artifactId>
    <version>1.1</version>
</dependency>
```

## Examples

For detailed examples, please visit look `d3fx-demo` module.