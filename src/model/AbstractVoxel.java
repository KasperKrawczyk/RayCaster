package model;

public abstract class AbstractVoxel extends Vector3D {

    private Vector3D gradient;

    public AbstractVoxel(double x, double y, double z, Vector3D gradient) {
        super(x, y, z);
        this.gradient = gradient;
    }

    public Vector3D getGradient() {
        return gradient;
    }

    public void setGradient(Vector3D gradient) {
        this.gradient = gradient;
    }

}
