package model;

import javafx.scene.paint.Color;

public class ColorVoxel extends AbstractVoxel {

    private final Color color;

    public ColorVoxel(double x, double y, double z, Vector3D gradient, Color color) {
        super(x, y, z, gradient);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
