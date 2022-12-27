import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class VolumeRenderer {

    public static final String NUM_OF_THREADS_ERR_MSG = "The number of threads should divide the image width with no remainder";

    public static ArrayList<Voxel> collectSamples(Vector3D intersection0, Vector3D intersection1, short[][][] vol) {
        ArrayList<Voxel> list = new ArrayList<>();
        int depth = vol.length;
        int height = vol[0].length;
        int width = vol[0][0].length;

        Point3D point0 = Point3D.Point3DfromVector(intersection0);
        Point3D point1 = Point3D.Point3DfromVector(intersection1);
        Vector3D step = intersection1.sub(intersection0).normalize();
        Point3D curSamplePoint = new Point3D(point0);
        double distance = point0.distance(point1);
        int distanceInt = (int) distance;

        for (int i = 0; i < distanceInt; i++) {

            //clip
            curSamplePoint.setX(Math.min(Math.max(0, curSamplePoint.getX()), width - 1));
            curSamplePoint.setY(Math.min(Math.max(0, curSamplePoint.getY()), height - 1));
            curSamplePoint.setZ(Math.min(Math.max(0, curSamplePoint.getZ()), depth - 1));


            short interpolatedSampleShort = Gradients.tlerp(
                    curSamplePoint.getX(),
                    curSamplePoint.getY(),
                    curSamplePoint.getZ(),
                    vol
            );

            Vector3D gradient = Gradients.get3DGradientInterpolated3D(
                    curSamplePoint.getX(),
                    curSamplePoint.getY(),
                    curSamplePoint.getZ(),
                    vol).flip();

            Voxel voxel = new Voxel(
                    curSamplePoint.getX(),
                    curSamplePoint.getY(),
                    curSamplePoint.getZ(),
                    gradient,
                    interpolatedSampleShort
            );

            list.add(voxel);
            curSamplePoint.moveByVector(step);
        }
        return list;
    }

    public static Color compositeSamples(ArrayList<Voxel> list) {
        double r = 0;
        double g = 0;
        double b = 0;
        double opacity = 0;
        double rAcc = 0;
        double gAcc = 0;
        double bAcc = 0;
        double transparencyAcc = 1;


        for (Voxel sample : list) {
            short sampleValue = sample.getMaterialValue();
            //opacity = DataSet.getOpacityLUT()[(int) sample.getGradient().magnitude()];



            if (sampleValue >= -100 && sampleValue < 300) {
                r = 1.0;
                g = 0.79;
                b = 0.6;
                opacity = DataSet.getOpacityLUT()[(int) sample.getGradient().magnitude()];
            } else if (sampleValue >= 300 && sampleValue <= 1900) {
                r = 1.0;
                g = 1.0;
                b = 1.0;

                // nicer bone?
//                r = 0.8902;
//                g = 0.8549;
//                b = 0.7882;
                opacity = DataSet.getOpacityLUT()[(int) sample.getGradient().magnitude()];
            } else {
                r = 1.0;
                g = 1.0;
                b = 1.0;
                opacity = 0.0;
            }

            Color shadedSample = Reflections.applyLambertianReflection(
                    Camera.getLight(),
                    sample,
                    sample.getGradient(),
                    new Color(r, g, b, opacity)
            );

            rAcc += transparencyAcc * opacity * shadedSample.getRed();
            gAcc += transparencyAcc * opacity * shadedSample.getGreen();
            bAcc += transparencyAcc * opacity * shadedSample.getBlue();
            transparencyAcc *= (1 - shadedSample.getOpacity());

            //no use iterating over the list any more as a fully opaque material has been hit
            if (opacity == 1) {
                break;
            }

        }

        rAcc = Math.min(1, rAcc);
        gAcc = Math.min(1, gAcc);
        bAcc = Math.min(1, bAcc);

        return Color.color(rAcc, gAcc, bAcc, 1 - transparencyAcc);
    }


    protected static Color sampleCompositeShade(Vector3D intersectionVector0, Vector3D intersectionVector1,
                                                short[][][] vol) {
        ArrayList<Voxel> list = collectSamples(intersectionVector0, intersectionVector1, vol);
        return compositeSamples(list);


    }

    public static Image volumeRayCastParallelized(short[][][] vol, int numOfThreads) {
        Vector3D aabbOffset = new Vector3D(0, 0, 0);


        WritableImage renderedImage = new WritableImage(Camera.VIEW_PLANE_WIDTH, Camera.VIEW_PLANE_HEIGHT);
        Color[][] colorMat = new Color[Camera.VIEW_PLANE_HEIGHT][Camera.VIEW_PLANE_WIDTH];
        PixelWriter pixelWriter = renderedImage.getPixelWriter();
        AABB aabb = new AABB(
                new Vector3D(255, 112, 255).add(aabbOffset),
                new Vector3D(0, 0, 0).add(aabbOffset)
        );

        runRotatedRayCasterTasks(numOfThreads, colorMat, aabb, aabbOffset, vol);
        matToImg(colorMat, pixelWriter);

        return renderedImage;
    }

//    public static Image volumeRayCastParallelized(short[][][] vol, int numOfThreads) {
//        WorldOld.initWorld();
//        Vector3D aabbOffset = new Vector3D(0, 0, 0);
//
//
//        WritableImage renderedImage = new WritableImage(WorldOld.VIEW_PLANE_WIDTH, WorldOld.VIEW_PLANE_HEIGHT);
//        Color[][] colorMat = new Color[WorldOld.VIEW_PLANE_HEIGHT][WorldOld.VIEW_PLANE_WIDTH];
//        PixelWriter pixelWriter = renderedImage.getPixelWriter();
//        AABB aabb = new AABB(
//                new Vector3D(255, 112, 255).add(aabbOffset),
//                new Vector3D(0, 0, 0).add(aabbOffset)
//        );
//
//        WorldOld.moveViewPlaneByAngleDegrees(WorldOld.getViewPlaneAngle());
//        runRayCasterTasks(numOfThreads, colorMat, aabb, aabbOffset, vol);
//        matToImg(colorMat, pixelWriter);
//
//        return renderedImage;
//    }


    /**
     * Translates the entering and exit intersection points from the world coordinates
     * to 3D matrix coordinates.
     *
     * @param intersections the two real-world coordinate intersections,
     *                      [0] is the entry intersection,
     *                      [1] is the leave intersection
     * @return the 3D matrix coordinates of the volume for the entry and the leave intersection
     */
    protected static Vector3D[] translateToVolumeCoordinates(Vector3D[] intersections, Vector3D offset) {
        if (intersections == null) {
            return null;
        }
        Vector3D[] volumeMatrixIntersections = new Vector3D[2];
        volumeMatrixIntersections[0] = new Vector3D(
                intersections[0].getZ(),
                113 - intersections[0].getY(),
                intersections[0].getX()
        );

        volumeMatrixIntersections[1] = new Vector3D(
                intersections[1].getZ(),
                113 - intersections[1].getY(),
                intersections[1].getX()
        );

        return volumeMatrixIntersections;

    }

    private static void matToImg(Color[][] colorMat, PixelWriter pixelWriter) {
        for (int y = 0; y < Camera.VIEW_PLANE_HEIGHT; y++) {
            for (int x = 0; x < Camera.VIEW_PLANE_WIDTH; x++) {
                    pixelWriter.setColor(x, y, colorMat[y][x]);
            }
        }
    }

    private static int[][] getBoundingIndices(int sectionWidth) {
        int numOfSections = Camera.VIEW_PLANE_WIDTH / sectionWidth;
        //[sectionNum][0] for start, [sectionNum][1] for end
        int[][] indices = new int[numOfSections][2];
        int curStart;
        int curEnd;
        for (int sectionNum = 0; sectionNum < numOfSections; sectionNum++) {
            curStart = sectionWidth * sectionNum;
            curEnd = curStart + sectionWidth;
            indices[sectionNum][0] = curStart;
            indices[sectionNum][1] = curEnd;
        }
        return indices;
    }

//    private static void runRayCasterTasks(int numOfThreads, Color[][] colorMat, AABB aabb,
//                                          Vector3D aabbOffset, short[][][] vol) {
//        if (!isCorrectNumThreads(numOfThreads)) {
//            throw new IllegalArgumentException(NUM_OF_THREADS_ERR_MSG);
//        }
//        Thread[] taskThreads = new Thread[numOfThreads];
//        CountDownLatch latch = new CountDownLatch(numOfThreads);
//        Vector3D step = WorldOld.getStep();
//        int sectionWidth = WorldOld.VIEW_PLANE_WIDTH / numOfThreads;
//        int[][] boundingIndices = getBoundingIndices(sectionWidth);
//        int startIndex;
//        int endIndex;
//        for (int numOfSection = 0; numOfSection < numOfThreads; numOfSection++) {
//            startIndex = boundingIndices[numOfSection][0];
//            endIndex = boundingIndices[numOfSection][1];
//            RayCasterTask task = new RayCasterTask(
//                    colorMat, aabb, step, aabbOffset, latch, vol, startIndex, endIndex
//            );
//            taskThreads[numOfSection] = new Thread(task);
//            taskThreads[numOfSection].start();
//        }
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


    private static void runRotatedRayCasterTasks(int numOfThreads, Color[][] colorMat, AABB aabb,
                                                Vector3D aabbOffset, short[][][] vol) {
        if (!isCorrectNumThreads(numOfThreads)) {
            throw new IllegalArgumentException(NUM_OF_THREADS_ERR_MSG);
        }
        Thread[] taskThreads = new Thread[numOfThreads];
        CountDownLatch latch = new CountDownLatch(numOfThreads);
        int sectionWidth = Camera.VIEW_PLANE_WIDTH / numOfThreads;
        int[][] boundingIndices = getBoundingIndices(sectionWidth);
        int startIndex;
        int endIndex;
        for (int numOfSection = 0; numOfSection < numOfThreads; numOfSection++) {
            startIndex = boundingIndices[numOfSection][0];
            endIndex = boundingIndices[numOfSection][1];
            RotatedRayCasterTask task = new RotatedRayCasterTask(
                    colorMat, aabb, aabbOffset, latch, vol, startIndex, endIndex
            );
            taskThreads[numOfSection] = new Thread(task);
            taskThreads[numOfSection].start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean isCorrectNumThreads(int numOfThreads) {
        return Camera.VIEW_PLANE_WIDTH % numOfThreads == 0;
    }

}


