import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public abstract class RayCasterTask {

    protected final Color[][] image;
    protected final AABB aabb;
    protected final Vector3D aabbOffset;
    protected final CountDownLatch latch;
    protected final short[][][] vol;
    protected final int startIndex;
    protected final int endIndex;


    public RayCasterTask(Color[][] image, AABB aabb, Vector3D aabbOffset, CountDownLatch latch,
                         short[][][] vol, int startIndex, int endIndex) {
        this.image = image;
        this.aabb = aabb;
        this.aabbOffset = aabbOffset;
        this.latch = latch;
        this.vol = vol;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

//    @Override
//    public void run() {
//        for (int y = 0; y < Camera.VIEW_PLANE_HEIGHT; y++) {
//            Vector3D curStep = startStepX;
//            for (int x = startIndex; x < endIndex; x++) {
//                Vector3D rayOrigin = new Vector3D(Camera.getViewPortCorner0().add(curStepX));
//                adjust for the current pixel
//                rayOrigin.setY(y);
//                curStep = curStep.add(stepX);
//
//
//                rayOrigin = TrackballPane.getLastQuat().rotate(rayOrigin, WorldOld.DATASET_CENTRE);
//
//                Ray ray = new Ray(rayOrigin, Camera.getViewPortNormal());
//                Vector3D[] intersectionPoints = VolumeRenderer.translateToVolumeCoordinates(
//                        aabb.getIntersections(ray, 0, Float.MAX_VALUE),
//                        aabbOffset);
//                Color color;
//                if (intersectionPoints != null) {
//                    color = VolumeRenderer.sampleCompositeShade(
//                            intersectionPoints[0],
//                            intersectionPoints[1],
//                            vol
//                    );
//                } else {
//                    color = Color.BLACK;
//                }
//
//                image[y][x] = color;
//            }
//        }
//        latch.countDown();
//    }
//
//    private void setStartStepX() {
//        this.startStepX = this.curStepX.add(this.stepX.mult(startIndex));
//    }
}
