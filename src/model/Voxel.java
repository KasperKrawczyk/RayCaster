package model;

public class Voxel extends Vector3D {

    private Vector3D gradient;
    private short materialValue;

    public Voxel(double x, double y, double z, Vector3D gradient, short materialValue) {
        super(x, y, z);
        this.gradient = gradient;
        this.materialValue = materialValue;
    }

    public Vector3D getGradient() {
        return gradient;
    }

    public void setGradient(Vector3D gradient) {
        this.gradient = gradient;
    }

    public short getMaterialValue() {
        return materialValue;
    }

    public void setMaterialValue(short materialValue) {
        this.materialValue = materialValue;
    }
}
