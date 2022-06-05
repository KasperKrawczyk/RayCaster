import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public class RotatedRayCasterTask extends RayCasterTask {

    private final Quaternion rotator;

    public RotatedRayCasterTask(Color[][] image, AABB aabb, Vector3D step, Vector3D aabbOffset, Quaternion rotator,
                                CountDownLatch latch, short[][][] vol, int startIndex, int endIndex) {
        super(image, aabb, step, aabbOffset, latch, vol, startIndex, endIndex);
        this.rotator = rotator;
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

//                Vector3D rotatedNormal = rotator.rotate(World.getViewPlaneNormal(), World.DATASET_CENTRE);
//                Ray ray = new Ray(rayOrigin, rotatedNormal); //TODO for rotating the rays

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

}
