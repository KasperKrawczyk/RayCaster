package component;

import component.camera.AbstractCamera;
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

public class SceneVolumeRenderer extends AbstractVolumeRenderer {

    private final Scene scene;

    public SceneVolumeRenderer(SceneCamera sceneCamera, Scene scene) {
        super(sceneCamera);
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

    public Color compositeSamples(ArrayList<? extends AbstractVoxel> list) {
        double r = 0;
        double g = 0;
        double b = 0;
        double opacity = 0;
        double rAcc = 0;
        double gAcc = 0;
        double bAcc = 0;
        double transparencyAcc = 1;


        for (ColorVoxel sample : (ArrayList<ColorVoxel>) list) {
            Color color = sample.getColor();
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            if (!color.equals(Color.WHITE)) {
                opacity = DataSet.getOpacityLUT()[(int) Math.min(DataSet.getOpacityLUT().length - 1, sample.getGradient().magnitude())];
            } else {
                opacity = 0;
            }

            Color shadedSample = Reflections.applyLambertianReflection(
                    this.camera.getLight(),
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


        WritableImage renderedImage = new WritableImage(AbstractCamera.VIEW_PLANE_WIDTH, AbstractCamera.VIEW_PLANE_HEIGHT);
        Color[][] colorMat = new Color[AbstractCamera.VIEW_PLANE_HEIGHT][AbstractCamera.VIEW_PLANE_WIDTH];
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
        int sectionWidth = AbstractCamera.VIEW_PLANE_WIDTH / numOfThreads;
        int[][] boundingIndices = getBoundingIndices(sectionWidth);
        int startIndex;
        int endIndex;
        for (int numOfSection = 0; numOfSection < numOfThreads; numOfSection++) {
            startIndex = boundingIndices[numOfSection][0];
            endIndex = boundingIndices[numOfSection][1];
            SceneRotatedRayCasterTask task = new SceneRotatedRayCasterTask(
                    colorMat, scene, (SceneCamera) camera, latch, this, startIndex, endIndex
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

    @Override
    public ArrayList<? extends AbstractVoxel> collectSamples(Vector3D intersection0, Vector3D intersection1, short[][][] vol) {
        return null;
    }

    @Override
    public Color sampleCompositeShade(Vector3D intersectionVector0, Vector3D intersectionVector1, short[][][] vol) {
        return null;
    }

}


