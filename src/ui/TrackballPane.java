package ui;

import component.AbstractVolumeRenderer;
import component.camera.AbstractCamera;
import component.camera.SingleObjectCamera;
import component.SingleObjectVolumeRenderer;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import mathutil.Gradients;
import mathutil.Reflections;
import mathutil.ImageUtil;
import mathutil.TrigUtil;
import model.DataSet;
import model.Point3D;
import model.Quaternion;
import model.Vector3D;

public class TrackballPane extends StackPane {
    public static final int SIDE = 256;
    public static final float RADIUS = 1;

    private WritableImage trackballImage = new WritableImage(SIDE, SIDE);
    private final ImageView mainView;
    private final AbstractVolumeRenderer volumeRenderer;
    private final AbstractCamera singleObjectCamera;
    private final DataSet dataSet;
    private ImageView trackballView = new ImageView(trackballImage);
    private Quaternion lastQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
    private Quaternion curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
    private Point2D start;

    public TrackballPane(ImageView view, AbstractVolumeRenderer volumeRenderer, AbstractCamera camera, DataSet dataSet) {
        super();
        paintImage3();
        this.mainView = view;
        this.volumeRenderer = volumeRenderer;
        this.getChildren().add(trackballView);
        this.trackballView.setImage(trackballImage);
        this.singleObjectCamera = camera;
        this.dataSet = dataSet;

        // for debugging purposes
//        this.trackballView.setOnMouseMoved(event -> {
//            double[] can = getCanonical(event.getX(), event.getY());
//
//            double canX2 = Gradients.mapToNewRange(event.getX(), 0, 255, -1, 1, 5);
//            double canY2 = -Gradients.mapToNewRange(event.getY(), 0, 255, -1, 1, 5);
//
//            System.out.println("canX1 = " + can[0] + " || canY1 = " + -can[1]);
//            System.out.println("canX2 = " + canX2 + " || canY2 = " + canY2);
////            if (isWithinCircle(can[0], can[1], RADIUS)) {
////                System.out.println(can[0] + " | " + can[1]);
////
////            }
//        });

        this.trackballView.setOnMousePressed(event -> {
            this.start = new Point2D(event.getX(), event.getY());
        });

        this.trackballView.setOnMouseDragged(event -> {
            if (this.start == null) {
                return;
            }
            Vector3D startVector = getProjection(this.start.getX(), this.start.getY(), RADIUS);
            Vector3D endVector = getProjection(event.getX(), event.getY(), RADIUS);
            this.curQuat = Quaternion.getQuatBetweenVectors(startVector, endVector);

        });

        this.trackballView.setOnMouseReleased(event -> {
            if (this.start == null) {
                return;
            }
            this.lastQuat = curQuat.mult(this.lastQuat).normalize();
            this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
            this.start = null;
            camera.moveViewPortByRotator(this.lastQuat);

            Image renderedImage = (volumeRenderer.volumeRayCastParallelized(
                    dataSet.getBytes(),
                    Trackball.NUM_OF_THREADS)
            );
            this.mainView.setImage(renderedImage);

        });

    }


    private double hyperbolicDepth(double x, double y, double radius) {
        return (Math.pow(radius, 2) / 2) / (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
    }

    private double sphereDepth(double x, double y, double radius) {
        return Math.sqrt(Math.pow(radius, 2) - Math.pow(x, 2) - Math.pow(y, 2));
    }

    private double[] getCanonical(double x, double y) {
        double[] canonCoords = new double[2]; //[x, y]

        canonCoords[0] = (2 * x - SIDE - 1) / (SIDE - 1);
        canonCoords[1] = (2 * y - SIDE - 1) / (SIDE - 1);
//        canonCoords[0] = ImageUtil.normalizeWithConstraint(x, 0, SIDE - 1, -1, 1);
//        canonCoords[1] = ImageUtil.normalizeWithConstraint(y, 0, SIDE - 1, -1, 1);

        return canonCoords;
    }

    private Vector3D getProjection(double x, double y, double radius) {
        double[] canonCoords = getCanonical(x, y);
        double z;
        x = canonCoords[0];
        y = canonCoords[1];
        if (isInHemisphere(x, y, radius)) {
            z = sphereDepth(x, y, radius);
        } else {
            z = hyperbolicDepth(x, y, radius);
        }
        return new Vector3D(canonCoords[0], -canonCoords[1], z);
    }

    private boolean isInHemisphere(double x, double y, double radius) {
        return (Math.pow(x, 2) + Math.pow(y, 2)) <= (Math.pow(radius, 2) / 2);
    }

    private boolean isWithinCircle(double x, double y, double radius) {
        return (Math.pow(x, 2) + Math.pow(y, 2)) <= (Math.pow(radius, 2) / 2);
    }

    private void paintImage() {
        Vector3D light = new Vector3D(0, 0, 75);
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                Vector3D proj = getProjection(x, y, RADIUS);
//                proj.setY(-proj.getY());
                Color shadedPoint = Reflections.applyLambertianReflection(
                        light,
                        proj,
                        proj.normalize(),
                        Color.color(0.8, 0.2, 0.3)
                );
                trackballImage.getPixelWriter().setColor(x, y, shadedPoint);
            }
        }
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {

                if ((x % 16 == 0) || (y % 16 == 0)) {
                    putCirclePixelAt(x, y);


                    double[] canonCoords = getCanonical(x, SIDE - 1 - y);
                    double canonX = canonCoords[0];
                    double canonY = canonCoords[1];

//                    if (!isWithinCircle(canonX, canonY, RADIUS)) {
                    int xAxisAdjust = canonX >= 0 ? -1 : 1;
                    int yAxisAdjust = canonY >= 0 ? -1 : 1;
                    double resX = getSechCurve(canonY, Math.abs(canonY), 1, 1, canonX);
                    double resY = getSechCurve(canonX, Math.abs(canonX), 1, 1, canonY);
                    System.out.print("canonX = " + canonX + " || canonY = " + canonY + " || resX = " + resX + " || resY = " + resY);
                    double rescaledX = ImageUtil.normalizeWithConstraint(resX, 0, 2, 0, 255);
                    double rescaledY = ImageUtil.normalizeWithConstraint(resY, 0, 2, 0, 255);
                    System.out.print(" || rescaledX = " + rescaledX + " || rescaledY = " + rescaledY + " || x = " + x + " || y = " + y);
//                        System.out.println("resX normalised = " + resX);


//                        trackballImage.getPixelWriter().setColor(y, SIDE - 1 - (int) rescaledY, Color.color(0.47, 0.47, 0.5));
//                        trackballImage.getPixelWriter().setColor(y, (int) rescaledY, Color.color(0.47, 0.47, 0.5));
//                        trackballImage.getPixelWriter().setColor((int) rescaledY, y, Color.color(0.47, 0.47, 0.5));
//                        trackballImage.getPixelWriter().setColor(SIDE - 1 - (int) rescaledY, y, Color.color(0.47, 0.47, 0.5));
//                    }

                }
            }
        }

    }

    private void paintImage2() {
        Vector3D light = new Vector3D(0, 0, 75);
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                Vector3D proj = getProjection(x, y, RADIUS);
//                proj.setY(-proj.getY());
                Color shadedPoint = Reflections.applyLambertianReflection(
                        light,
                        proj,
                        proj.normalize(),
                        Color.color(0.8, 0.2, 0.3)
                );
                trackballImage.getPixelWriter().setColor(x, y, shadedPoint);
            }
        }
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        Color grey = Color.color(0.47, 0.47, 0.5);
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                double[] canonCoords = getCanonical(x, y);
                double canonX = canonCoords[0];
                double canonY = canonCoords[1];
                double absCanX = Math.abs(canonX);
                double absCanY = Math.abs(canonY);
                int tailCoeffOffset = absCanX >= 0 ? 1 : 0;
                System.out.println("canonX = " + canonX + " || canonY = " + canonY);

                if ((x + 1) % 8 == 0) {

                    if (canonX < 0) continue;
                    double canonZ = getSechCurve(
                            absCanX,
                            (absCanX + 1),
                            canonY,
                            1,
                            Math.sqrt(2) / 2);
                    minZ = Math.min(canonZ, minZ);
                    maxZ = Math.max(canonZ, maxZ);
                    System.out.println("canonZ = " + canonZ);
                    int z = (int) ImageUtil.normalizeWithConstraint(canonZ, 0, 1.5, 0, (SIDE * 1.5) - 1);
                    System.out.println(z);
                    if (z < SIDE && z >= 0) {
                        trackballImage.getPixelWriter().setColor(z, SIDE - 1 - y, grey);
                        trackballImage.getPixelWriter().setColor(SIDE - 1 - z, SIDE - 1 - y, grey);

                    }
                } else if ((y + 1) % 8 == 0) {
                    if (canonY < 0) continue;


                }
            }
        }
        System.out.println("minZ = " + minZ + " || maxZ = " + maxZ);

    }

    private void paintImage3() {
        Vector3D light = new Vector3D(0, 0, 75);

        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {

                Vector3D proj = getProjection(x, y, RADIUS);
                Color shadedPoint = Reflections.applyLambertianReflection(light, proj, proj.normalize(), Color.color(0.8, 0.2, 0.3));
                trackballImage.getPixelWriter().setColor(x, y, shadedPoint);
            }
        }

        Color grey = Color.color(0.47, 0.47, 0.5);
        double canonX;
        double canonY;
        double canonZ;
        int z;

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {


                canonX = Gradients.mapToNewRange(x, 0, SIDE - 1, -1, 1, 4);
                // mind the `-` sign!
                canonY = -1 * Gradients.mapToNewRange(y, 0, SIDE - 1, -1, 1, 4);

                if ((x + 1) % 8 == 0) {
                    canonZ = getHyperbolicCurve(
                            canonX,
                            -canonX / 10,
                            canonY,
                            2);
                    z = (int) Gradients.mapToNewRange(-canonZ, -1, 1, 0, SIDE - 1, 2);
                    // vertical bars
                    trackballImage.getPixelWriter().setColor(z, y, grey);
                    // horizontal bars
                    trackballImage.getPixelWriter().setColor(y, z, grey);
                }
            }
        }
        System.out.println(min);
        System.out.println(max);

    }

    private double getTheta(double unitX) {
        return (Math.PI / 4) * (2 * unitX - 1);
    }

    private double getPhi(double unitY) {
        return (Math.PI / 4) * (4 * unitY + 1);
    }

    private Point3D getSphericalCoords(double theta, double phi, double radius) {
        double x = radius * Math.sin(theta) * Math.cos(phi);
        double y = radius * Math.sin(theta) * Math.sin(phi);
        double z = radius * Math.cos(theta);
        return new Point3D(x, y, z);
    }

    /**
     * <code>axisOffset + (Math.pow(TrigUtil.sech(curveCoeffMult * val), pow) * axisOffsetCoeff)</code>
     *
     * @param axisOffset -ve moves centre leftwards / downwards, +ve moves centre rightwards / upwards
     * @param curveCoeffMult 0 == straight
     * @param val
     * @return
     */
    private double getSechCurve(double axisOffset, double curveCoeffMult, double val, double pow, double axisOffsetCoeff) {
        return axisOffset + (Math.pow(TrigUtil.sech(curveCoeffMult * val), pow) * axisOffsetCoeff);
    }


    /**
     * <code>axisOffset + (curvCoeff * Math.pow(val, pow))</code>
     *
     * @param axisOffset -ve moves centre leftwards / downwards, +ve moves centre rightwards / upwards
     * @param curvCoeff  -ve curves leftwards / downwards, +ve curves rightwards / upwards
     * @param val        input value
     * @param pow        the exponent to bring the base <code>val</code> up to
     * @return
     */
    private double getHyperbolicCurve(double axisOffset, double curvCoeff, double val, double pow) {
        return axisOffset + (curvCoeff * Math.pow(val, pow));
    }

    private boolean isSafeImageCoords(int x, int y) {
        return (x >= 0 && x < SIDE) && (y >= 0 && y < SIDE);
    }

    private void putCirclePixelAt(int x, int y) {
        Vector3D projection = getProjection(x, y, RADIUS);
        int projX = (int) projection.getX();
        int projY = (int) projection.getY();
        double[] canonCoords = getCanonical(x, SIDE - 1 - y);
        double canonX = canonCoords[0];
        double canonY = canonCoords[1];
        if (isWithinCircle(projX, projY, RADIUS)) {
            double unitX = ImageUtil.normalizeWithConstraint(x, 0, 255, 0, 1);
            double unitY = ImageUtil.normalizeWithConstraint(y, 0, 255, 0, 1);
            double theta = getTheta(unitX);
            double phi = getPhi(unitY);
            Point3D sphericalCoords = getSphericalCoords(theta, phi, RADIUS);
//                        System.out.println("sphericalCoords = " + sphericalCoords);

            int resX = (int) ImageUtil.normalizeWithConstraint(sphericalCoords.getX(), -1, 1, 0, 255);
            int resY = (int) ImageUtil.normalizeWithConstraint(sphericalCoords.getY(), -1, 1, 0, 255);
//                        System.out.println("resX = " + resX + " || " + "resY = " + resY);
            if ((resX >= 0 && resX < SIDE) && (resY >= 0 && resY < SIDE)) {
                trackballImage.getPixelWriter().setColor(resX, resY, Color.color(0.47, 0.47, 0.5));
            }
        }
    }

    private double getHyperbolicCanonicalCoord(double x, double y, double radius) {
        double z;
        y = y;
        if (isInHemisphere(x, y, radius)) {
            z = sphereDepth(x, y, radius);
        } else {
            z = hyperbolicDepth(x, y, radius);
        }
        return z;
    }
}
