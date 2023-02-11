package component;

import component.camera.SingleObjectCamera;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mathutil.Gradients;
import mathutil.Reflections;
import model.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class SingleObjectVolumeRenderer extends AbstractVolumeRenderer {

    public SingleObjectVolumeRenderer(SingleObjectCamera singleObjectCamera) {
        super(singleObjectCamera);
    }

    public SingleObjectVolumeRenderer(SingleObjectCamera singleObjectCamera, HashMap<Short, Color> huToColorMap) {
        super(singleObjectCamera, huToColorMap);
    }

    public static final String NUM_OF_THREADS_ERR_MSG = "The number of threads should divide the image width with no remainder";

    public ArrayList<? extends AbstractVoxel> collectSamples(Vector3D intersection0, Vector3D intersection1, short[][][] vol) {
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


        for (Voxel sample : (List<Voxel>) list) {
            short sampleValue = sample.getMaterialValue();

            Color color = huToColorMap.ceilingEntry(sampleValue).getValue();
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            if (!color.equals(Color.WHITE)) {
                opacity = DataSet.getOpacityLUT()[(int) sample.getGradient().magnitude()];
            } else {
                opacity = 0;
            }

            Color shadedSample = Reflections.applyLambertianReflection(
                    camera.getLight(),
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



    public Color sampleCompositeShade(Vector3D intersectionVector0, Vector3D intersectionVector1,
                                      short[][][] vol) {
        ArrayList<Voxel> list = (ArrayList<Voxel>) collectSamples(intersectionVector0, intersectionVector1, vol);
        return compositeSamples(list);


    }

    public Image volumeRayCastParallelized(short[][][] vol, int numOfThreads) {
        Vector3D aabbOffset = new Vector3D(0, 0, 0);


        WritableImage renderedImage = new WritableImage(SingleObjectCamera.VIEW_PLANE_WIDTH, SingleObjectCamera.VIEW_PLANE_HEIGHT);
        Color[][] colorMat = new Color[SingleObjectCamera.VIEW_PLANE_HEIGHT][SingleObjectCamera.VIEW_PLANE_WIDTH];
        PixelWriter pixelWriter = renderedImage.getPixelWriter();
        AABB aabb = new AABB(
                new Vector3D(255, 226, 255).add(aabbOffset),
                new Vector3D(0, 0, 0).add(aabbOffset)
        );

        runRotatedRayCasterTasks(numOfThreads, colorMat, aabb, vol);
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


    private void runRotatedRayCasterTasks(int numOfThreads, Color[][] colorMat, AABB aabb, short[][][] vol) {
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
            RotatedRayCasterTask task = new RotatedRayCasterTask(
                    colorMat, aabb, (SingleObjectCamera) camera, latch, this, vol, startIndex, endIndex
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


