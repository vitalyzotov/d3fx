package ru.vzotov.d3fx.force;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.List;
import java.util.function.Function;

public class LinkForce<D extends Node,N extends ForcedNode<D>> extends Force<N> {

    private List<Link<D,N>> links;
    private int[] count;
    private double[] bias;
    private double[] strengths;
    private double[] distances;

    private Function<Link<D,N>, Double> distance = (link) -> 30d;
    private Function<Link<D,N>, Double> strength = (link) -> (double) 1d / (double) Math.min(count[link.getSource().index], count[link.getTarget().index]);


    public LinkForce(ObservableList<N> nodes, List<Link<D,N>> links, Function<Link<D,N>, Double> distance) {
        super(nodes);
        this.links = links;
        this.distance = distance;

        int n = nodes.size();
        int m = links.size();

        this.count = new int[n];

        for (int i = 0; i < links.size(); i++) {
            Link<D,N> link = links.get(i);
            link.setIndex(i);
            count[link.getSource().index] = count[link.getSource().index] + 1;
            count[link.getTarget().index] = count[link.getTarget().index] + 1;
        }

        bias = new double[m];

        for (int i = 0; i < m; ++i) {
            Link<D,N> link = links.get(i);
            bias[i] = (double) count[link.getSource().index] / (double) (count[link.getSource().index] + count[link.getTarget().index]);
        }

        strengths = new double[m];
        initializeStrength();

        distances = new double[m];
        initializeDistance();
    }

    private void initializeStrength() {
        if (nodes == null) return;
        for (int i = 0, n = links.size(); i < n; ++i) {
            strengths[i] = strength.apply(links.get(i));
        }
    }

    private void initializeDistance() {
        if (nodes == null) return;

        for (int i = 0, n = links.size(); i < n; ++i) {
            distances[i] = distance.apply(links.get(i));
        }
    }

    @Override
    public void force(double alpha) {
        for (int i = 0; i < links.size(); i++) {
            Link<D,N> link = links.get(i);
            N source = link.getSource();
            N target = link.getTarget();

            double x = target.getX() + target.vx - source.getX() - source.vx;
            if (Double.isNaN(x)) x = ForceAnimation.jiggle();

            double y = target.getY() + target.vy - source.getY() - source.vy;
            if (Double.isNaN(y)) y = ForceAnimation.jiggle();

            double l = Math.sqrt(x * x + y * y);
            l = (l - distances[i]) / l * alpha * strengths[i];
            x *= l;
            y *= l;
            double b;
            target.vx -= x * (b = bias[i]);
            target.vy -= y * b;
            source.vx += x * (b = 1 - b);
            source.vy += y * b;
        }
    }
}
