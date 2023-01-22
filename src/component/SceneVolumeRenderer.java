package component;

import component.camera.SceneCamera;
import component.camera.SingleObjectCamera;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mathutil.Gradients;
import mathutil.Reflections;
import model.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class SceneVolumeRenderer {

    private final SceneCamera sceneCamera;
    private final Scene scene;

    public SceneVolumeRenderer(SceneCamera sceneCamera, Scene scene) {
        this.sceneCamera = sceneCamera;
        this.scene = scene;
    }

    public static final String NUM_OF_THREADS_ERR_MSG = "The number of threads should divide the image width with no remainder";

    public ArrayList<ColorVoxel> collectSamples(AABBIntersectionPoints aabbIntersectionPoints) {
        ArrayList<ColorVoxel> list = new ArrayList<>();
        SceneObject sceneObject = aabbIntersectionPoints.getSceneObject();
        int depth = sceneObject.getVol().length;
        int height = sceneObject.getVol()[0].length;
        int width = sceneObject.getVol()[0][0].length;

        Point3D point0 = Point3D.Point3DfromVector(aabbIntersectionPoints.getMinVec());
        Point3D point1 = Point3D.Point3DfromVector(aabbIntersectionPoints.getMaxVec());
        Vector3D step = aabbIntersectionPoints.getMaxVec().sub(aabbIntersectionPoints.getMinVec()).normalize();
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
                    sceneObject.getVol()
            );

            Vector3D gradient = Gradients.get3DGradientInterpolated3D(
                    curSamplePoint.getX(),
                    curSamplePoint.getY(),
                    curSamplePoint.getZ(),
                    sceneObject.getVol()).flip();

            ColorVoxel colorVoxel = new ColorVoxel(
                    curSamplePoint.getX(),
                    curSamplePoint.getY(),
                    curSamplePoint.getZ(),
                    gradient,
                    sceneObject.getHuToColorMap().ceilingEntry(interpolatedSampleShort).getValue()
            );

            list.add(colorVoxel);
            curSamplePoint.moveThisByVector(step);
        }
        return list;
    }

    public Color compositeSamples(ArrayList<ColorVoxel> list) {
        double r = 0;
        double g = 0;
        double b = 0;
        double opacity = 0;
        double rAcc = 0;
        double gAcc = 0;
        double bAcc = 0;
        double transparencyAcc = 1;


        for (ColorVoxel sample : list) {

            Color color = sample.getColor();
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            if (!color.equals(Color.WHITE)) {
                opacity = DataSet.getOpacityLUT()[(int) sample.getGradient().magnitude()];
            } else {
                opacity = 0;
            }

            Color shadedSample = Reflections.applyLambertianReflection(
                    sceneCamera.getLight(),
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


    public Color sampleCompositeShade(ArrayList<AABBIntersectionPoints> aabbIntersectionPoints) {
        ArrayList<ColorVoxel> list = new ArrayList<>();
        for (AABBIntersectionPoints intersectionPoints : aabbIntersectionPoints) {
            list.addAll(collectSamples(intersectionPoints));
        }
        return compositeSamples(list);


    }

    public Image volumeRayCastParallelized(short[][][] vol, int numOfThreads) {
        Vector3D aabbOffset = new Vector3D(0, 0, 0);


        WritableImage renderedImage = new WritableImage(SingleObjectCamera.VIEW_PLANE_WIDTH, SingleObjectCamera.VIEW_PLANE_HEIGHT);
        Color[][] colorMat = new Color[SingleObjectCamera.VIEW_PLANE_HEIGHT][SingleObjectCamera.VIEW_PLANE_WIDTH];
        PixelWriter pixelWriter = renderedImage.getPixelWriter();

        runRotatedRayCasterTasks(numOfThreads, colorMat);
        matToImg(colorMat, pixelWriter);

        return renderedImage;
    }

//    public static Image volumeRayCastParallelized(short[][][] vol, int numOfThreads) {
//        WorldOld.initWorld();
//        model.Vector3D aabbOffset = new model.Vector3D(0, 0, 0);
//
//
//        WritableImage renderedImage = new WritableImage(WorldOld.VIEW_PLANE_WIDTH, WorldOld.VIEW_PLANE_HEIGHT);
//        Color[][] colorMat = new Color[WorldOld.VIEW_PLANE_HEIGHT][WorldOld.VIEW_PLANE_WIDTH];
//        PixelWriter pixelWriter = renderedImage.getPixelWriter();
//        model.AABB aabb = new model.AABB(
//                new model.Vector3D(255, 112, 255).add(aabbOffset),
//                new model.Vector3D(0, 0, 0).add(aabbOffset)
//        );
//
//        WorldOld.moveViewPlaneByAngleDegrees(WorldOld.getViewPlaneAngle());
//        runRayCasterTasks(numOfThreads, colorMat, aabb, aabbOffset, vol);
//        matToImg(colorMat, pixelWriter);
//
//        return renderedImage;
//    }


    private static void matToImg(Color[][] colorMat, PixelWriter pixelWriter) {
        for (int y = 0; y < SingleObjectCamera.VIEW_PLANE_HEIGHT; y++) {
            for (int x = 0; x < SingleObjectCamera.VIEW_PLANE_WIDTH; x++) {
                    pixelWriter.setColor(x, y, colorMat[y][x]);
            }
        }
    }

    private static int[][] getBoundingIndices(int sectionWidth) {
        int numOfSections = SingleObjectCamera.VIEW_PLANE_WIDTH / sectionWidth;
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

//    private static void runRayCasterTasks(int numOfThreads, Color[][] colorMat, model.AABB aabb,
//                                          model.Vector3D aabbOffset, short[][][] vol) {
//        if (!isCorrectNumThreads(numOfThreads)) {
//            throw new IllegalArgumentException(NUM_OF_THREADS_ERR_MSG);
//        }
//        Thread[] taskThreads = new Thread[numOfThreads];
//        CountDownLatch latch = new CountDownLatch(numOfThreads);
//        model.Vector3D step = WorldOld.getStep();
//        int sectionWidth = WorldOld.VIEW_PLANE_WIDTH / numOfThreads;
//        int[][] boundingIndices = getBoundingIndices(sectionWidth);
//        int startIndex;
//        int endIndex;
//        for (int numOfSection = 0; numOfSection < numOfThreads; numOfSection++) {
//            startIndex = boundingIndices[numOfSection][0];
//            endIndex = boundingIndices[numOfSection][1];
//            component.RayCasterTask task = new component.RayCasterTask(
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


    private void runRotatedRayCasterTasks(int numOfThreads, Color[][] colorMat) {
        if (!isCorrectNumThreads(numOfThreads)) {
            throw new IllegalArgumentException(NUM_OF_THREADS_ERR_MSG);
        }
        Thread[] taskThreads = new Thread[numOfThreads];
        CountDownLatch latch = new CountDownLatch(numOfThreads);
        int sectionWidth = SingleObjectCamera.VIEW_PLANE_WIDTH / numOfThreads;
        int[][] boundingIndices = getBoundingIndices(sectionWidth);
        int startIndex;
        int endIndex;
        for (int numOfSection = 0; numOfSection < numOfThreads; numOfSection++) {
            startIndex = boundingIndices[numOfSection][0];
            endIndex = boundingIndices[numOfSection][1];
            SceneRotatedRayCasterTask task = new SceneRotatedRayCasterTask(
                    colorMat, scene, sceneCamera, latch, this, startIndex, endIndex
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
        return SingleObjectCamera.VIEW_PLANE_WIDTH % numOfThreads == 0;
    }

}


