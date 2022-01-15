package ru.vzotov.d3fx.hierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

// Node-link tree diagram using the Reingold-Tilford "tidy" algorithm
public final class TreeBuilder<D extends NodeData, N extends PointNode<D, N>> {

    private final BiFunction<N, N, Double> defaultSeparation = (a, b) -> a.getParent() == b.getParent() ? 1d : 2d;

    private BiFunction<N, N, Double> separation = defaultSeparation;
    private double dx = 1d;
    private double dy = 1d;
    private boolean nodeSize = false;

    public N tree(N root) {
        var t = treeRoot(root);

        // Compute the layout using Buchheim et al.’s algorithm.
        t.eachAfter(this::firstWalk);
        t.getParent().m = -t.z;
        t.eachBefore(this::secondWalk);

        // If a fixed node size is specified, scale x and y.
        if (nodeSize) root.eachBefore(this::sizeNode);
            // If a fixed tree size is specified, scale x and y based on the extent.
            // Compute the left-most, right-most, and depth-most nodes for extents.
        else {
            var nodes = new PointNodes<D, N>(root, root, root);
            root.eachBefore(node -> {
                if (node.getX() < nodes.left.getX()) nodes.left = node;
                if (node.getX() > nodes.right.getX()) nodes.right = node;
                if (node.getDepth() > nodes.bottom.getDepth()) nodes.bottom = node;
            });
            double s = nodes.left == nodes.right ? 1d : separation.apply(nodes.left, nodes.right) / 2d,
                    tx = s - nodes.left.getX(),
                    kx = dx / (nodes.right.getX() + s + tx),
                    ky = dy / (double) (nodes.bottom.getDepth() > 0 ? nodes.bottom.getDepth() : 1);
            root.eachBefore(node -> {
                node.setX((node.getX() + tx) * kx);
                node.setY((double) node.getDepth() * ky);
            });
        }

        return root;
    }

    public BiFunction<N, N, Double> separation() {
        return separation;
    }

    public TreeBuilder<D, N> separation(BiFunction<N, N, Double> separation) {
        this.separation = separation;
        return this;
    }

    public double[] size() {
        return nodeSize ? null : new double[]{dx, dy};
    }

    public TreeBuilder<D, N> size(double dx, double dy) {
        this.nodeSize = false;
        this.dx = dx;
        this.dy = dy;
        return this;
    }

    public double[] nodeSize() {
        return nodeSize ? new double[]{dx, dy} : null;
    }

    public TreeBuilder<D, N> nodeSize(double dx, double dy) {
        this.nodeSize = true;
        this.dx = dx;
        this.dy = dy;
        return this;
    }

    // This function is used to traverse the left contour of a subtree (or
    // subforest). It returns the successor of v on this contour. This successor is
    // either given by the leftmost child of v or by the thread of v. The function
    // returns null if and only if v is on the highest level of its subtree.
    private TreeNode<D, N> nextLeft(TreeNode<D, N> v) {
        var children = v.getChildren();
        return children != null ? children.get(0) : v.t;
    }

    // This function works analogously to nextLeft.
    private TreeNode<D, N> nextRight(TreeNode<D, N> v) {
        var children = v.getChildren();
        return children != null ? children.get(children.size() - 1) : v.t;
    }

    // Shifts the current subtree rooted at w+. This is done by increasing
    // prelim(w+) and mod(w+) by shift.
    private void moveSubtree(TreeNode<D, N> wm, TreeNode<D, N> wp, double shift) {
        double change = shift / (wp.i - wm.i);
        wp.c -= change;
        wp.s += shift;
        wm.c += change;
        wp.z += shift;
        wp.m += shift;
    }

    // All other shifts, applied to the smaller subtrees between w- and w+, are
    // performed by this function. To prepare the shifts, we have to adjust
    // change(w+), shift(w+), and change(w-).
    private void executeShifts(TreeNode<D, N> v) {
        double shift = 0.0,
                change = 0.0;
        List<TreeNode<D, N>> children = v.getChildren();
        int i = children.size();
        TreeNode<D, N> w;
        while (--i >= 0) {
            w = children.get(i);
            w.z += shift;
            w.m += shift;
            shift += w.s + (change += w.c);
        }
    }

    // If vi-’s ancestor is a sibling of v, returns vi-’s ancestor. Otherwise,
    // returns the specified (default) ancestor.
    private TreeNode<D, N> nextAncestor(TreeNode<D, N> vim, TreeNode<D, N> v, TreeNode<D, N> ancestor) {
        return vim.a.getParent() == v.getParent() ? vim.a : ancestor;
    }


    // Computes a preliminary x-coordinate for v. Before that, FIRST WALK is
    // applied recursively to the children of v, as well as the function
    // APPORTION. After spacing out the children by calling EXECUTE SHIFTS, the
    // node v is placed to the midpoint of its outermost children.
    private void firstWalk(TreeNode<D, N> v) {
        List<TreeNode<D, N>> children = v.getChildren();
        List<TreeNode<D, N>> siblings = v.getParent().getChildren();
        TreeNode<D, N> w = v.i > 0 ? siblings.get(v.i - 1) : null;
        if (children != null) {
            executeShifts(v);
            var midpoint = (children.get(0).z + children.get(children.size() - 1).z) / 2;
            if (w != null) {
                v.z = w.z + separation.apply(v.node, w.node);
                v.m = v.z - midpoint;
            } else {
                v.z = midpoint;
            }
        } else if (w != null) {
            v.z = w.z + separation.apply(v.node, w.node);
        }
        v.getParent().A = apportion(v, w, v.getParent().A != null ? v.getParent().A : siblings.get(0));
    }
    // Computes all real x-coordinates by summing up the modifiers recursively.

    private void secondWalk(TreeNode<D, N> v) {
        v.node.setX(v.z + v.getParent().m);
        v.m += v.getParent().m;
    }

    // The core of the algorithm. Here, a new subtree is combined with the
    // previous subtrees. Threads are used to traverse the inside and outside
    // contours of the left and right subtree up to the highest common level. The
    // vertices used for the traversals are vi+, vi-, vo-, and vo+, where the
    // superscript o means outside and i means inside, the subscript - means left
    // subtree and + means right subtree. For summing up the modifiers along the
    // contour, we use respective variables si+, si-, so-, and so+. Whenever two
    // nodes of the inside contours conflict, we compute the left one of the
    // greatest uncommon ancestors using the function ANCESTOR and call MOVE
    // SUBTREE to shift the subtree and prepare the shifts of smaller subtrees.
    // Finally, we add a new thread (if necessary).
    private TreeNode<D, N> apportion(TreeNode<D, N> v, TreeNode<D, N> w, TreeNode<D, N> ancestor) {
        if (w != null) {
            var vip = v;
            var vop = v;
            var vim = w;
            var vom = vip.getParent().getChildren().get(0);
            double sip = vip.m;
            double sop = vop.m;
            double sim = vim.m;
            double som = vom.m;
            double shift;
            for (vim = nextRight(vim), vip = nextLeft(vip); vim != null && vip != null; vim = nextRight(vim), vip = nextLeft(vip)) {
                vom = nextLeft(vom);
                vop = nextRight(vop);
                vop.a = v;
                shift = vim.z + sim - vip.z - sip + separation.apply(vim.node, vip.node);
                if (shift > 0) {
                    moveSubtree(nextAncestor(vim, v, ancestor), v, shift);
                    sip += shift;
                    sop += shift;
                }
                sim += vim.m;
                sip += vip.m;
                som += vom.m;
                sop += vop.m;
            }
            if (vim != null && nextRight(vop) == null) {
                vop.t = vim;
                vop.m += sim - sop;
            }
            if (vip != null && nextLeft(vom) == null) {
                vom.t = vip;
                vom.m += sip - som;
                ancestor = v;
            }
        }
        return ancestor;
    }

    private void sizeNode(N node) {
        node.setX(node.getX() * dx);
        node.setY((double) node.getDepth() * dy);
    }


    private TreeNode<D, N> treeRoot(N root) {
        TreeNode<D, N> tree = new TreeNode<>(root, 0);
        TreeNode<D, N> node;
        LinkedList<TreeNode<D, N>> nodes = new LinkedList<>(Collections.singleton(tree));
        TreeNode<D, N> child;
        List<N> children;
        int i, n;

        while ((node = nodes.poll()) != null) {
            if ((children = node.node.getChildren()) != null) {
                node.setChildren(new ArrayList<>(Collections.nCopies(n = children.size(), null)));
                for (i = n - 1; i >= 0; --i) {
                    child = new TreeNode<>(children.get(i), i);
                    node.getChildren().set(i, child);
                    nodes.push(child);
                    child.setParent(node);
                }
            }
        }

        tree.setParent(new TreeNode<>(null, 0));
        tree.getParent().setChildren(new ArrayList<>(Collections.singleton(tree)));
        return tree;
    }

    private static class PointNodes<D extends NodeData, N extends PointNode<D, N>> {
        N left;
        N right;
        N bottom;

        public PointNodes(N left, N right, N bottom) {
            this.left = left;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
