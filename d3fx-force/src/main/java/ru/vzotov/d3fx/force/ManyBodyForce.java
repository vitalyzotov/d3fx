package ru.vzotov.d3fx.force;

import ru.vzotov.d3fx.quadtree.QuadNode;
import ru.vzotov.d3fx.quadtree.QuadTree;
import javafx.collections.ObservableList;

import java.util.function.Function;

public class ManyBodyForce<N extends ForcedNode<?>> extends Force<N> {
    private Function<N, Double> strength = (node) -> -30d;
    private double theta2 = 0.81;
    private double distanceMin2 = 1;
    private double distanceMax2 = Double.POSITIVE_INFINITY;
    private double[] strengths;

    public ManyBodyForce(ObservableList<N> nodes, Function<N, Double> strength, double distanceMin, double distanceMax) {
        super(nodes);
        this.strength = strength;
        this.distanceMin2 = distanceMin;
        this.distanceMax2 = distanceMax;

        this.strengths = new double[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            strengths[i] = strength.apply(nodes.get(i));
        }
    }

    @Override
    public void force(double alpha) {
        QuadTree<N, ManyForceQuadNode<N>> tree = QuadTree.quadTree(nodes,
                N::getX, N::getY, ManyForceQuadNode::new, ManyForceQuadNode::new);
        tree.visitAfter(this::accumulate);
        for (N node : nodes) {
            tree.visit((quad, x1, y1, x2, y2) -> {
                return apply(node, alpha, quad, x1, y1, x2, y2);
            });
        }
    }

    private boolean apply(N node, double alpha, ManyForceQuadNode<N> quad, double x1, double y1, double x2, double y2) {
        if (Double.isNaN(quad.value) || Double.compare(quad.value, 0) == 0) return true;

        double x = quad.x - node.getX(),
                y = quad.y - node.getY(),
                w = x2 - x1,
                l = x * x + y * y;

        // Apply the Barnes-Hut approximation if possible.
        // Limit forces for very close nodes; randomize direction if coincident.
        if (w * w / theta2 < l) {
            if (l < distanceMax2) {
                if (Double.compare(x, 0) == 0) {
                    x = ForceAnimation.jiggle();
                    l += x * x;
                }
                if (Double.compare(y, 0) == 0) {
                    y = ForceAnimation.jiggle();
                    l += y * y;
                }
                if (l < distanceMin2) {
                    l = Math.sqrt(distanceMin2 * l);
                }
                node.vx += x * quad.value * alpha / l;
                node.vy += y * quad.value * alpha / l;
            }
            return true;
        }
        // Otherwise, process points directly.
        else if (quad.hasChildren() || l >= distanceMax2) return false;

        // Limit forces for very close nodes; randomize direction if coincident.
        if ((quad.data != node) || (quad.next != null)) {
            if (Double.compare(x, 0) == 0) {
                x = ForceAnimation.jiggle();
                l += x * x;
            }
            if (Double.compare(y, 0) == 0) {
                y = ForceAnimation.jiggle();
                l += y * y;
            }
            if (l < distanceMin2) {
                l = Math.sqrt(distanceMin2 * l);
            }
        }

        do if (quad.data != node) {
            w = strengths[quad.data.index] * alpha / l;
            node.vx += x * w;
            node.vy += y * w;
        } while ((quad = quad.next) != null);

        return false;
    }

    private boolean accumulate(ManyForceQuadNode<N> quad, double x1, double y1, double x2, double y2) {
        //System.out.println(quad);

        ManyForceQuadNode<N> q;
        double strength = 0, c, weight = 0, x, y;
        int i;

        // For internal nodes, accumulate forces from child quadrants.
        if (quad.hasChildren()) {
            x = y = 0;
            for (i = 0; i < 4; ++i) {
                if ((q = quad.get(i)) != null && Double.compare(c = Math.abs(q.value), 0) != 0) {
                    strength += q.value;
                    weight += c;
                    x += c * q.x;
                    y += c * q.y;
                }
            }
            quad.x = x / weight;
            quad.y = y / weight;
        }
        // For leaf nodes, accumulate forces from coincident quadrants.
        else {
            q = quad;
            q.x = q.data.getX();
            q.y = q.data.getY();
            do {
                strength += strengths[q.data.index];
            } while ((q = q.next) != null);
        }

        quad.value = strength;

        return false;
    }

    private static class ManyForceQuadNode<N extends ForcedNode> extends QuadNode<N, ManyForceQuadNode<N>> {
        private double value = Double.NaN;
        private double x;
        private double y;

        public ManyForceQuadNode() {
        }

        public ManyForceQuadNode(N data) {
            super(data);
        }
    }
}
