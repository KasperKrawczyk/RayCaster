package model;

public class AABBIntersectionPoints implements Comparable<AABBIntersectionPoints> {
    private final AABB aabb;
    private final Ray ray;
    private final Point3D min;
    private final Point3D max;
    private final double minDist;
    private final double maxDist;

    private SceneObject sceneObject;

    public AABBIntersectionPoints(AABB aabb, Ray ray, Point3D min, Point3D max, double minDist, double maxDist) {
        this.aabb = aabb;
        this.ray = ray;
        this.min = min;
        this.max = max;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    public AABBIntersectionPoints(AABB aabb, Ray ray, double minDist, double maxDist) {
        this.aabb = aabb;
        this.ray = ray;
        this.min = ray.getPointAtDistance((float) minDist);
        this.max = ray.getPointAtDistance((float) maxDist);
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    @Override
    public int compareTo(AABBIntersectionPoints other) {
        if (this.minDist > other.minDist) {
            return 1;
        } else if (this.minDist < other.minDist) {
            return -1;
        }
        return 0;
    }

    public AABB getAabb() {
        return aabb;
    }

    public Ray getRay() {
        return ray;
    }

    public Point3D getMin() {
        return min;
    }

    public Vector3D getMinVec() {
        return new Vector3D(min);
    }

    public Point3D getMax() {
        return max;
    }

    public Vector3D getMaxVec() {
        return new Vector3D(max);
    }

    public double getMinDist() {
        return minDist;
    }

    public double getMaxDist() {
        return maxDist;
    }

    public SceneObject getSceneObject() {
        return sceneObject;
    }

    public void setSceneObject(SceneObject sceneObject) {
        this.sceneObject = sceneObject;
    }
}
