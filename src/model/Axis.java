package model;

import model.Vector3D;

public enum Axis {
    X(new Vector3D(1, 0, 0)),
    Y(new Vector3D(0, 1, 0)),
    Z(new Vector3D(0, 0, 1));


    private final Vector3D axis;

    Axis(Vector3D axis) {
        this.axis = axis;
    }

    public Vector3D getVector() {
        return axis;
    }
}
