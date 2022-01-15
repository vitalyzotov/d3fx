package ru.vzotov.d3fx.quadtree;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Inspired by D3.js
 *
 * @param <E> type of tree elements
 */
public class QuadTree<E, Q extends QuadNode<E, Q>> {

    public static <E, Q extends QuadNode<E, Q>> QuadTree<E, Q> quadTree(List<E> nodes,
                                                                        Function<E, Double> x, Function<E, Double> y,
                                                                        Supplier<Q> internalSupplier,
                                                                        Function<E, Q> leafSupplier) {
        QuadTree<E, Q> tree = new QuadTree<>(x, y, Double.NaN, Double.NaN, Double.NaN, Double.NaN, internalSupplier, leafSupplier);
        tree.addAll(nodes);
        return tree;
    }

    private final Function<E, Double> _x;
    private final Function<E, Double> _y;
    private double _x0;
    private double _y0;
    private double _x1;
    private double _y1;
    private Supplier<Q> internalSupplier;
    private Function<E, Q> leafSupplier;
    private Q _root;

    private static int bool(boolean b) {
        return b ? 1 : 0;
    }

    public QuadTree(Function<E, Double> x, Function<E, Double> y,
                    double x0, double y0, double x1, double y1,
                    Supplier<Q> internalSupplier,
                    Function<E, Q> leafSupplier) {
        this._x = x;
        this._y = y;
        this._x0 = x0;
        this._y0 = y0;
        this._x1 = x1;
        this._y1 = y1;
        this.internalSupplier = internalSupplier;
        this.leafSupplier = leafSupplier;
    }

    public QuadTree<E, Q> visitAfter(Visitor<E, Q> callback) {
        final Deque<Quad<E, Q>> quads = new LinkedList<>();
        final Deque<Quad<E, Q>> next = new LinkedList<>();
        Quad<E, Q> q;
        if (this._root != null) {
            quads.push(new Quad<>(this._root, this._x0, this._y0, this._x1, this._y1));
        }
        while ((q = quads.poll()) != null) {
            Q node = q.node;
            if (node.hasChildren()) {
                Q child;
                double x0 = q.x0, y0 = q.y0, x1 = q.x1, y1 = q.y1, xm = (x0 + x1) / 2, ym = (y0 + y1) / 2;
                if ((child = node.get(0)) != null) quads.push(new Quad<>(child, x0, y0, xm, ym));
                if ((child = node.get(1)) != null) quads.push(new Quad<>(child, xm, y0, x1, ym));
                if ((child = node.get(2)) != null) quads.push(new Quad<>(child, x0, ym, xm, y1));
                if ((child = node.get(3)) != null) quads.push(new Quad<>(child, xm, ym, x1, y1));
            }
            next.push(q);
        }
        while ((q = next.poll()) != null) {
            callback.visit(q.node, q.x0, q.y0, q.x1, q.y1);
        }
        return this;
    }

    public QuadTree<E, Q> visit(Visitor<E, Q> callback) {
        final Deque<Quad<E, Q>> quads = new LinkedList<>();
        Quad<E, Q> q;
        Q node = this._root;
        Q child;
        double x0, y0, x1, y1;
        if (node != null) quads.push(new Quad<>(node, this._x0, this._y0, this._x1, this._y1));
        while ((q = quads.poll()) != null) {
            if (!callback.visit(node = q.node, x0 = q.x0, y0 = q.y0, x1 = q.x1, y1 = q.y1) && node.hasChildren()) {
                double xm = (x0 + x1) / 2, ym = (y0 + y1) / 2;
                if ((child = node.get(3)) != null) quads.push(new Quad<>(child, xm, ym, x1, y1));
                if ((child = node.get(2)) != null) quads.push(new Quad<>(child, x0, ym, xm, y1));
                if ((child = node.get(1)) != null) quads.push(new Quad<>(child, xm, y0, x1, ym));
                if ((child = node.get(0)) != null) quads.push(new Quad<>(child, x0, y0, xm, ym));
            }
        }
        return this;
    }

    public QuadTree<E, Q> cover(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) return this; // ignore invalid points

        double x0 = this._x0;
        double y0 = this._y0;
        double x1 = this._x1;
        double y1 = this._y1;

        // If the quadtree has no extent, initialize them.
        // Integer extent are necessary so that if we later double the extent,
        // the existing quadrant boundaries don’t change due to floating point error!
        if (Double.isNaN(x0)) {
            x0 = Math.floor(x);
            y0 = Math.floor(y);
            x1 = (x0) + 1;
            y1 = (y0) + 1;
        }
        // Otherwise, double repeatedly to cover.
        else {
            double z = x1 - x0;
            Q node = this._root;
            Q parent;
            int i;

            while (x0 > x || x >= x1 || y0 > y || y >= y1) {
                i = bool(y < y0) << 1 | bool(x < x0);
                parent = internalSupplier.get(); //new QuadNode<>();
                parent.set(i, node);
                node = parent;
                z *= 2;
                switch (i) {
                    case 0 -> {
                        x1 = x0 + z;
                        y1 = y0 + z;
                    }
                    case 1 -> {
                        x0 = x1 - z;
                        y1 = y0 + z;
                    }
                    case 2 -> {
                        x1 = x0 + z;
                        y0 = y1 - z;
                    }
                    case 3 -> {
                        x0 = x1 - z;
                        y0 = y1 - z;
                    }
                }
            }

            if (this._root != null && this._root.hasChildren()) {
                this._root = node;
            }
        }

        this._x0 = x0;
        this._y0 = y0;
        this._x1 = x1;
        this._y1 = y1;
        return this;
    }

    public QuadTree<E, Q> addAll(List<E> data) {
        int n = data.size();
        double[] xz = new double[n];
        double[] yz = new double[n];
        double x0 = Double.POSITIVE_INFINITY,
                y0 = Double.POSITIVE_INFINITY,
                x1 = Double.NEGATIVE_INFINITY,
                y1 = Double.NEGATIVE_INFINITY;


        // Compute the points and their extent.
        for (int i = 0; i < n; ++i) {
            E d = data.get(i);
            double x = this._x.apply(d);
            double y = this._y.apply(d);
            if (Double.isNaN(x) || Double.isNaN(y)) continue;

            xz[i] = x;
            yz[i] = y;
            if (x < x0) x0 = x;
            if (x > x1) x1 = x;
            if (y < y0) y0 = y;
            if (y > y1) y1 = y;
        }

        // If there were no (valid) points, abort.
        if (x0 > x1 || y0 > y1) return this;

        // Expand the tree to cover the new points.
        this.cover(x0, y0).cover(x1, y1);

        // Add the new points.
        for (int i = 0; i < n; ++i) {
            add(this, xz[i], yz[i], data.get(i));
        }

        return this;
    }

    public QuadTree<E, Q> add(E data) {
        final double x = _x.apply(data);
        final double y = _y.apply(data);
        return add(this.cover(x, y), x, y, data);
    }

    private static <E, Q extends QuadNode<E, Q>> QuadTree<E, Q> add(QuadTree<E, Q> tree, double x, double y, E d) {
        if (Double.isNaN(x) || Double.isNaN(y)) return tree; // ignore invalid points

        Q parent = null;
        Q node = tree._root;
        Q leaf = tree.leafSupplier.apply(d);// new QuadNode<>(d);
        double x0 = tree._x0;
        double y0 = tree._y0;
        double x1 = tree._x1;
        double y1 = tree._y1;
        double xm;
        double ym;
        double xp;
        double yp;
        boolean right;
        boolean bottom;
        int i = -1;
        int j;

        // If the tree is empty, initialize the root as a leaf.
        if (node == null) {
            tree._root = leaf;
            return tree;
        }

        // Find the existing leaf for the new point, or add it.
        while (node.hasChildren()) {
            xm = (x0 + x1) / 2;
            right = x >= xm;
            if (right) {
                x0 = xm;
            } else {
                x1 = xm;
            }
            ym = (y0 + y1) / 2;
            bottom = y >= ym;
            if (bottom) {
                y0 = ym;
            } else {
                y1 = ym;
            }
            parent = node;
            i = bool(bottom) << 1 | bool(right);
            node = node.get(i);
            if (node == null) {
                parent.set(i, leaf);
                return tree;
            }
        }

        // Is the new point is exactly coincident with the existing point?
        xp = tree._x.apply(node.data);
        yp = tree._y.apply(node.data);

        if (Double.compare(x, xp) == 0 && Double.compare(y, yp) == 0) {
            leaf.next = node;
            if (parent != null) {
                parent.set(i, leaf);
            } else {
                tree._root = leaf;
            }
            return tree;
        }

        // Otherwise, split the leaf node until the old and new point are separated.
        do {
            parent = (parent != null) ? (parent.set(i, tree.internalSupplier.get())) : (tree._root = tree.internalSupplier.get());

            xm = (x0 + x1) / 2;
            right = (x >= xm);
            if (right) {
                x0 = xm;
            } else {
                x1 = xm;
            }

            ym = (y0 + y1) / 2;
            bottom = (y >= ym);
            if (bottom) {
                y0 = ym;
            } else {
                y1 = ym;
            }
        } while ((i = bool(bottom) << 1 | bool(right)) == (j = bool(yp >= ym) << 1 | bool(xp >= xm)));
        parent.set(j, node);
        parent.set(i, leaf);
        return tree;
    }

}
