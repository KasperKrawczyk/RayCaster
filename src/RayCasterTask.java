import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public class RayCasterTask implements Runnable {

    protected final Color[][] image;
    protected final AABB aabb;
    protected final Vector3D stepX;
    protected final Vector3D aabbOffset;
    protected final CountDownLatch latch;
    protected final short vol[][][];
    protected final int startIndex;
    protected final int endIndex;

    protected Vector3D startStepX;
    protected Vector3D curStepX;
    protected Vector3D curStepY;

    public RayCasterTask(Color[][] image, AABB aabb, Vector3D aabbOffset, CountDownLatch latch,
                         short[][][] vol, int startIndex, int endIndex) {
        this.image = image;
        this.aabb = aabb;
        this.stepX = World2.getStepX();
        this.curStepX = new Vector3D(stepX);
        this.aabbOffset = aabbOffset;
        this.latch = latch;
        this.vol = vol;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.setStartStepX();
    }

    @Override
    public void run() {
        for (int y = 0; y < World.VIEW_PLANE_HEIGHT; y++) {
            Vector3D curStep = startStepX;
            for (int x = startIndex; x < endIndex; x++) {
                Vector3D rayOrigin = new Vector3D(World.getViewPlaneCorner0().add(curStep));
                //adjust for the current pixel
                rayOrigin.setY(y);
                curStep = curStep.add(stepX);
                //

                //rayOrigin = TrackballPane.getLastQuat().rotate(rayOrigin, World.DATASET_CENTRE);

                Ray ray = new Ray(rayOrigin, World.getViewPlaneNormal());
                Vector3D[] intersectionPoints = VolumeRenderer.translateToVolumeCoordinates(
                        aabb.getIntersections(ray, 0, Float.MAX_VALUE),
                        aabbOffset);
                Color color;
                if (intersectionPoints != null) {
                    color = VolumeRenderer.sampleCompositeShade(
                            intersectionPoints[0],
                            intersectionPoints[1],
                            vol
                    );
                } else {
                    color = Color.BLACK;
                }

                image[y][x] = color;
            }
        }
        latch.countDown();
    }

    private void setStartStepX() {
        this.startStepX = this.curStepX.add(this.stepX.mult(startIndex));
    }
}
