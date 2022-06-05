import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public class RotatedRayCasterTask extends RayCasterTask {

    protected Vector3D stepY;


    public RotatedRayCasterTask(Color[][] image, AABB aabb, Vector3D aabbOffset,
                                CountDownLatch latch, short[][][] vol, int startIndex, int endIndex) {
        super(image, aabb, aabbOffset, latch, vol, startIndex, endIndex);

        this.stepY = World2.getStepY();
        this.curStepY = World2.getViewPortCorner0();
//        System.out.println("World2.getStepY() = " + World2.getStepY());
//        System.out.println("World2.getStepX() = " + World2.getStepX());
    }

    @Override
    public void run() {
        Vector3D rayOrigin;
        for (int y = 0; y < World.VIEW_PLANE_HEIGHT; y++) {

            curStepX = startStepX;
            for (int x = startIndex; x < endIndex; x++) {
                rayOrigin = new Vector3D(World2.getViewPortCorner0().add(curStepX));
                //adjust for the current pixel
                rayOrigin.setY(curStepY.getY());
//                System.out.println("curStepY = " + curStepY);
                curStepX = curStepX.add(stepX);
                //

//                Vector3D rotatedNormal = rotator.rotate(World.getViewPlaneNormal(), World.DATASET_CENTRE);
//                Ray ray = new Ray(rayOrigin, rotatedNormal); //TODO for rotating the rays

                Ray ray = new Ray(rayOrigin, World2.getViewPortNormal());
//                System.out.println("ray = " + ray);

//                System.out.println("VPN = " + World2.getViewPortNormal());
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

                image[World.VIEW_PLANE_HEIGHT - y - 1][x] = color;
            }
            curStepY = curStepY.add(stepY);
        }
        latch.countDown();
    }

}
