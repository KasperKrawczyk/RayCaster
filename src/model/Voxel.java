package model;

public class Voxel extends AbstractVoxel {

    private short materialValue;

    public Voxel(double x, double y, double z, Vector3D gradient, short materialValue) {
        super(x, y, z, gradient);
        this.materialValue = materialValue;
    }

    public short getMaterialValue() {
        return materialValue;
    }

    public void setMaterialValue(short materialValue) {
        this.materialValue = materialValue;
    }
}
