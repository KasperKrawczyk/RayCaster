import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public class RotatedRayCasterTask extends RayCasterTask implements Runnable {

    public static final AABB ORIGIN_AABB = new AABB(
            new Vector3D(10, 10, 10),
            new Vector3D(-10, -40, -10)
    );

    public RotatedRayCasterTask(Color[][] image, AABB aabb, Vector3D aabbOffset,
                                CountDownLatch latch, short[][][] vol, int startIndex, int endIndex) {
        super(image, aabb, aabbOffset, latch, vol, startIndex, endIndex);

    }

    @Override
    public void run() {
        Vector3D passThroughPixel;
        Ray ray;
        for (int y = 0; y < Camera.VIEW_PLANE_HEIGHT; y++) {

            for (int x = startIndex; x < endIndex; x++) {
                passThroughPixel = getCurRayOrigin(x, y);
                ray = getCurRay(Camera.getEye(), passThroughPixel);

                Vector3D[] intersectionPoints = VolumeRenderer.translateToVolumeCoordinates(
                        aabb.getIntersections(ray, 0, Float.MAX_VALUE),
                        aabbOffset);

//                Vector3D[] origin = ORIGIN_AABB.getIntersections(ray, 0, Float.MAX_VALUE);

                Color color;
                if (intersectionPoints != null) {
                    color = VolumeRenderer.sampleCompositeShade(
                            intersectionPoints[0],
                            intersectionPoints[1],
                            vol
                    );
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
     * @return a <code>Vector3D</code> with the 3D coordinates from which to shoot a ray
     */
    private Vector3D getCurRayOrigin(int curX, int curY) {
        float quotientX = curX / (float) Camera.VIEW_PLANE_WIDTH;
        float quotientY = curY / (float) Camera.VIEW_PLANE_HEIGHT;

        return Gradients.blerp(
                Camera.getViewPortCorner0(),
                Camera.getViewPortCorner1(),
                Camera.getViewPortCorner2(),
                Camera.getViewPortCorner3(),
                quotientX,
                quotientX,
                quotientY
        );
    }

    private Ray getCurRay(Vector3D eye, Vector3D passThroughPixel) {
        Vector3D direction = passThroughPixel.sub(eye); // will be normalised in Ray::ctor
        return new Ray(eye, direction);
    }

}
