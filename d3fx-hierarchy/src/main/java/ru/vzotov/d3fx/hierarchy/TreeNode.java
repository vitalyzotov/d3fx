package ru.vzotov.d3fx.hierarchy;

public class TreeNode<D extends NodeData, N extends PointNode<D,N>> extends AbstractNode<D, TreeNode<D,N>> {

    N node;

    // default ancestor
    TreeNode<D,N> A;

    // ancestor
    TreeNode<D,N> a;

    // prelim
    double z;

    // mod
    double m;

    // change
    double c;

    // shift
    double s;

    // thread
    TreeNode<D,N> t;

    // number
    int i;

    public TreeNode(N node, int i) {
        super(node == null? null: node.getData());
        this.node = node;
        this.A = null;
        this.a = this;
        this.z = 0.0;
        this.m = 0.0;
        this.c = 0.0;
        this.s = 0.0;
        this.t = null;
        this.i = i;
    }
}
