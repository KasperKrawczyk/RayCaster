import java.util.Objects;

public class Point3D {
    protected double x;
    protected double y;
    protected double z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(Point3D other) {
        this.x = other.getX();
        this.y = other.getY();
        this.z = other.getZ();
    }

    public static Point3D Point3DfromVector(Vector3D vector) {
        return new Point3D(
                vector.getX(),
                vector.getY(),
                vector.getZ()
        );
    }

    @Override
    public String toString() {
        return "Point3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public double distance(Point3D other) {
        return Math.sqrt(
                Math.pow(this.x - other.getX(), 2) +
                Math.pow(this.y - other.getY(), 2) +
                Math.pow(this.z - other.getZ(), 2)
        );
    }

    public void moveThisByVector(Vector3D v)  {
        this.x += v.getX();
        this.y += v.getY();
        this.z += v.getZ();
    }

    public Vector3D newMovedByVector(Vector3D v)  {
        return new Vector3D(this.x + v.getX(), this.y + v.getY(), this.z + v.getZ());
    }

    public void moveByVectorMatrix(Vector3D v)  {
        this.x += v.getX();
        this.y -= v.getY();
        this.z += v.getZ();
    }

    public void translate(double dX, double dY, double dZ)  {
        this.x += dX;
        this.y += dY;
        this.z += dZ;
    }

    public void moveTo(double x, double y, double z)  {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point3D point3D = (Point3D) o;
        return Double.compare(point3D.getX(), getX()) == 0 && Double.compare(point3D.getY(), getY()) == 0 && Double.compare(point3D.getZ(), getZ()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getZ());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
