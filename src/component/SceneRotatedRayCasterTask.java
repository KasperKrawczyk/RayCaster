package component;

import component.camera.SceneCamera;
import component.camera.SingleObjectCamera;
import javafx.scene.paint.Color;
import mathutil.Gradients;
import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class SceneRotatedRayCasterTask implements Runnable {

    protected final Color[][] image;
    protected final Scene scene;
    protected final SceneCamera sceneCamera;
    protected final CountDownLatch latch;
    protected final SceneVolumeRenderer sceneVolumeRenderer;
    protected final int startIndex;
    protected final int endIndex;
    public static final AABB ORIGIN_AABB = new AABB(
            new Vector3D(10, 10, 10),
            new Vector3D(-10, -40, -10)
    );

    public SceneRotatedRayCasterTask(Color[][] image, Scene scene, SceneCamera sceneCamera,
                                     CountDownLatch latch, SceneVolumeRenderer sceneVolumeRenderer,
                                     int startIndex, int endIndex) {
        this.image = image;
        this.scene = scene;
        this.sceneCamera = sceneCamera;
        this.latch = latch;
        this.sceneVolumeRenderer = sceneVolumeRenderer;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void run() {
        Vector3D passThroughPixel;
        Ray ray;
        for (int y = 0; y < SingleObjectCamera.VIEW_PLANE_HEIGHT; y++) {

            for (int x = startIndex; x < endIndex; x++) {
                passThroughPixel = getCurRayOrigin(x, y);
                ray = getCurRay(sceneCamera.getEye(), passThroughPixel);

                ArrayList<AABBIntersectionPoints> intersectionPoints = intersectAll(scene.getSceneObjects(), ray);

                Color color;
                if (intersectionPoints != null) {
                    color = this.sceneVolumeRenderer.sampleCompositeShade(intersectionPoints);
                } else {
                    color = Color.WHITE;
                }
                image[y][x] = color;
            }
        }
        latch.countDown();
    }

    /**
     * Returns a point in 3D space from which to shoot a ray based on current pixel location.
     * The point is bilineraly interpolated between the four corners of the view port, based on the offset quotients derived
     * from the current pixel position (integer matrix coordinates) (x, y) and four 3D points expressed as vectors <x, y, z>
     * (current 3D coordinates of the viewport corners)
     * @param curX <code>int</code> x coordinate of the current pixel to find the colour for
     * @param curY <code>int</code> y coordinate of the current pixel to find the colour for
     * @return a <code>model.Vector3D</code> with the 3D coordinates from which to shoot a ray
     */
    private Vector3D getCurRayOrigin(int curX, int curY) {
        float quotientX = curX / (float) SingleObjectCamera.VIEW_PLANE_WIDTH;
        float quotientY = curY / (float) SingleObjectCamera.VIEW_PLANE_HEIGHT;

        return Gradients.blerp(
                sceneCamera.getViewPortCorner0(),
                sceneCamera.getViewPortCorner1(),
                sceneCamera.getViewPortCorner2(),
                sceneCamera.getViewPortCorner3(),
                quotientX,
                quotientX,
                quotientY
        );
    }

    private Ray getCurRay(Vector3D eye, Vector3D passThroughPixel) {
        Vector3D direction = passThroughPixel.sub(eye); // will be normalised in model.Ray::ctor
        return new Ray(eye, direction);
    }

    /**
     * Translates the entering and exit intersection points from the world coordinates
     * to 3D matrix coordinates.
     *
     * @param intersections the two real-world coordinate intersections,
     *                      [0] is the entry intersection,
     *                      [1] is the leave intersection
     * @return the 3D matrix coordinates of the volume for the entry and the leave intersection
     */
    protected AABBIntersectionPoints translateToVolumeCoordinates(AABBIntersectionPoints intersections) {
        if (intersections == null) {
            return null;
        }
        Point3D min = intersections.getMin();
        Point3D max = intersections.getMax();
        min = new Vector3D(
                min.getZ(),
                226 - min.getY(),
                min.getX()
        );

        max = new Vector3D(
                max.getZ(),
                226 - max.getY(),
                max.getX()
        );

        return new AABBIntersectionPoints(
                intersections.getAabb(),
                intersections.getRay(),
                min,
                max,
                intersections.getMinDist(),
                intersections.getMaxDist()
        );

    }

    protected ArrayList<AABBIntersectionPoints> intersectAll(ArrayList<SceneObject> sceneObjects, Ray ray) {
        ArrayList<AABBIntersectionPoints> intersections = new ArrayList<>();
        for (SceneObject sceneObject : sceneObjects) {
            AABBIntersectionPoints intersectionPoints = translateToVolumeCoordinates(
                    sceneObject.getAabb().getIntersections(ray, 0, Float.MAX_VALUE));
            if (intersectionPoints != null) {
                intersectionPoints.setSceneObject(sceneObject);
                intersections.add(intersectionPoints);
            }
        }
        Collections.sort(intersections);
        return intersections;
    }

}
