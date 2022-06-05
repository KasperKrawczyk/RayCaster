import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public class RayCasterTask implements Runnable {

    protected final Color[][] image;
    protected final AABB aabb;
    protected final Vector3D step;
    protected final Vector3D aabbOffset;
    protected final CountDownLatch latch;
    protected final short vol[][][];
    protected final int startIndex;
    protected final int endIndex;

    protected Vector3D startStep;
    protected Vector3D curStep;

    public RayCasterTask(Color[][] image, AABB aabb, Vector3D step, Vector3D aabbOffset, CountDownLatch latch,
                         short[][][] vol, int startIndex, int endIndex) {
        this.image = image;
        this.aabb = aabb;
        this.step = step;
        this.curStep = new Vector3D(step);
        this.aabbOffset = aabbOffset;
        this.latch = latch;
        this.vol = vol;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.setStartStep();
    }

    @Override
    public void run() {
        for (int y = 0; y < World.VIEW_PLANE_HEIGHT; y++) {
            Vector3D curStep = startStep;
            for (int x = startIndex; x < endIndex; x++) {
                Vector3D rayOrigin = new Vector3D(World.getViewPlaneCorner0().add(curStep));
                //adjust for the current pixel
                rayOrigin.setY(y);
                curStep = curStep.add(step);
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

    private void setStartStep() {
        this.startStep = this.curStep.add(this.step.mult(startIndex));
    }
}
