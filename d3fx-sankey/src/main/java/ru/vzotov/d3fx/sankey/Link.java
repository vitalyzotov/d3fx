package ru.vzotov.d3fx.sankey;

/**
 * For convenience, a link’s source and target may be initialized using numeric or string identifiers
 * rather than object references; see sankey.nodeId.
 *
 * @param <K> тип ссылки на ноду
 */
@SuppressWarnings("UnusedReturnValue")
public class Link<K,V> {

    /**
     * the link’s source node key
     */
    private final K sourceKey;

    private Node<K,V> source;

    /**
     * the link’s target node key
     */
    private final K targetKey;

    private Node<K,V> target;

    /**
     * the link’s value
     */
    private final V value;

    /**
     * the link’s vertical starting position (at source node)
     */
    private double y0;

    /**
     * the link’s vertical end position (at target node)
     */
    private double y1;

    /**
     * the link’s width (proportional to link.value)
     */
    private double width;

    /**
     * the zero-based index of link within the array of links
     */
    private int index;

    public Link(K source, K target, V value) {
        this.sourceKey = source;
        this.targetKey = target;
        this.value = value;
    }

    public K sourceKey() {
        return sourceKey;
    }

    public K targetKey() {
        return targetKey;
    }

    public Node<K,V> source() {
        return source;
    }

    public Node<K,V> source(Node<K,V> source) {
        this.source = source;
        return source;
    }

    public Node<K,V> target() {
        return target;
    }

    public Node<K,V> target(Node<K,V> target) {
        this.target = target;
        return target;
    }

    public V value() {
        return value;
    }

    public double y0() {
        return y0;
    }

    public double y0(double y0) {
        this.y0 = y0;
        return y0;
    }

    public double y1() {
        return y1;
    }

    @SuppressWarnings("unused")
    public double y1(double y1) {
        this.y1 = y1;
        return y1;
    }

    public double width() {
        return width;
    }

    @SuppressWarnings("unused")
    public double width(double width) {
        this.width = width;
        return width;
    }

    public int index() {
        return index;
    }

    public int index(int i) {
        index = i;
        return index;
    }

    @Override
    public String toString() {
        return "Link{" +
                "sourceKey=" + sourceKey +
                ", targetKey=" + targetKey +
                ", value=" + value +
                '}';
    }
}
