public class Ray extends Vector3D {

    private Vector3D direction;

    public Ray(Vector3D origin, Vector3D direction) {
        super(origin);
        this.direction = direction.normalize();
    }

    @Override
    public String toString() {
        return "Ray{" +
                "origin=" + super.toString() +
                "direction=" + direction +
                '}';
    }

    public Vector3D getPointAtDistance(double distance) {
        return this.add(direction.mult(distance));
    }

    public Vector3D getDirection() {
        return direction;
    }
}
