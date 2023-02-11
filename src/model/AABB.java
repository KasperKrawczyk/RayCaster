package model;

public class AABB {

    private final Vector3D max;
    private final Vector3D min;
    private final Point3D centre;


    public AABB(Vector3D max, Vector3D min) {
        this.max = max;
        this.min = min;
        this.centre = new Point3D(
                (min.getX() + max.getX()) / 2,
                (min.getY() + max.getY()) / 2,
                (min.getZ() + max.getZ()) / 2
        );
    }

    /**
     * Calculates intersection with the given ray between a certain distance
     * interval.
     * <p>
     * model.Ray-box intersection is using IEEE numerical properties to ensure the
     * test is both robust and efficient, as described in:
     * <br>
     * <code>Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
     * Efficient and Robust model.Ray-Box Intersection Algorithm" Journal of graphics
     * tools, 10(1):49-54, 2005</code>
     *
     * @param ray     incident ray
     * @param minDist minimum distance
     * @param maxDist maximum distance
     * @return intersection point on the bounding box (only the first is
     * returned) or null if no intersection
     */
    public Vector3D getCloserIntersection(Ray ray, float minDist, float maxDist) {
        Vector3D invDir = new Vector3D(
                1f / ray.getDirection().getX(),
                1f / ray.getDirection().getY(),
                1f / ray.getDirection().getZ()
        );

        boolean signDirX = invDir.getX() < 0;
        boolean signDirY = invDir.getY() < 0;
        boolean signDirZ = invDir.getZ() < 0;

        Vector3D boundingBox = signDirX ? max : min;
        double tMin = (boundingBox.getX() - ray.getX()) * invDir.getX();
        boundingBox = signDirX ? min : max;
        double tMax = (boundingBox.getX() - ray.getX()) * invDir.getX();
        boundingBox = signDirY ? max : min;
        double tYMin = (boundingBox.getY() - ray.getY()) * invDir.getY();
        boundingBox = signDirY ? min : max;
        double tYMax = (boundingBox.getY() - ray.getY()) * invDir.getY();

        if ((tMin > tYMax) || (tYMin > tMax)) {
            return null;
        }
        if (tYMin > tMin) {
            tMin = tYMin;
        }
        if (tYMax < tMax) {
            tMax = tYMax;
        }

        boundingBox = signDirZ ? max : min;
        double tZMin = (boundingBox.getZ() - ray.getZ()) * invDir.getZ();
        boundingBox = signDirZ ? min : max;
        double tZMax = (boundingBox.getZ() - ray.getZ()) * invDir.getZ();

        if ((tMin > tZMax) || (tZMin > tMax)) {
            return null;
        }
        if (tZMin > tMin) {
            tMin = tZMin;
        }
        if (tZMax < tMax) {
            tMax = tZMax;
        }
        if ((tMin < maxDist) && (tMax > minDist)) {
            return ray.getPointAtDistance((float) tMin);
        }
        return null;
    }

    /**
     * Calculates intersection with the given ray between a certain distance
     * interval.
     * <p>
     * Ray-box intersection is using IEEE numerical properties to ensure the
     * test is both robust and efficient, as described in:
     * <br>
     * <code>Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
     * Efficient and Robust model.Ray-Box Intersection Algorithm" Journal of graphics
     * tools, 10(1):49-54, 2005</code>
     *
     * @param ray     incident ray
     * @param minDist minimum distance
     * @param maxDist maximum distance
     * @return intersection points on the bounding box (the first is the closer one
     * the farther one is the second) or null if no intersection
     */
    public AABBIntersectionPoints getIntersections(Ray ray, float minDist, float maxDist) {
        Vector3D invDir = new Vector3D(
                1f / ray.getDirection().getX(),
                1f / ray.getDirection().getY(),
                1f / ray.getDirection().getZ()
        );

        boolean signDirX = invDir.getX() < 0;
        boolean signDirY = invDir.getY() < 0;
        boolean signDirZ = invDir.getZ() < 0;

        Vector3D boundingBox = signDirX ? max : min;
        double tMin = (boundingBox.getX() - ray.getX()) * invDir.getX();
        boundingBox = signDirX ? min : max;
        double tMax = (boundingBox.getX() - ray.getX()) * invDir.getX();
        boundingBox = signDirY ? max : min;
        double tYMin = (boundingBox.getY() - ray.getY()) * invDir.getY();
        boundingBox = signDirY ? min : max;
        double tYMax = (boundingBox.getY() - ray.getY()) * invDir.getY();

        if ((tMin > tYMax) || (tYMin > tMax)) {
            return null;
        }
        if (tYMin > tMin) {
            tMin = tYMin;
        }
        if (tYMax < tMax) {
            tMax = tYMax;
        }

        boundingBox = signDirZ ? max : min;
        double tZMin = (boundingBox.getZ() - ray.getZ()) * invDir.getZ();
        boundingBox = signDirZ ? min : max;
        double tZMax = (boundingBox.getZ() - ray.getZ()) * invDir.getZ();

        if ((tMin > tZMax) || (tZMin > tMax)) {
            return null;
        }
        if (tZMin > tMin) {
            tMin = tZMin;
        }
        if (tZMax < tMax) {
            tMax = tZMax;
        }
        if ((tMin < maxDist) && (tMax > minDist)) {
            return new AABBIntersectionPoints(this, ray, tMin, tMax);
        }
        return null;
    }

    public Vector3D getMax() {
        return max;
    }

    public Vector3D getMin() {
        return min;
    }

    public Point3D getCentre() {
        return centre;
    }
}
