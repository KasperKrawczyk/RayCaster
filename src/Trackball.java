import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Arrays;

public class Trackball {
    public static final double MIN_DIST = 0.001;
    public static final float RADIUS = 1;

    private final ImageView mainView;

    private Quaternion lastQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
    private Quaternion curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
    private Point2D start;

    public Trackball(ImageView mainView) {
        this.mainView = mainView;

        this.mainView.setOnMousePressed(event -> {
            this.start = new Point2D(event.getX(), event.getY());
        });

        this.mainView.setOnMouseDragged(event -> {
            //System.out.println("dragging started");
            if (this.start == null) {
                return;
            }

            Vector3D startVector = getProjection(this.start.getX(), this.start.getY());
//            System.out.println("startVector = " + startVector);
            Vector3D endVector = getProjection(event.getX(), event.getY());
//            System.out.println("endVector = " + endVector);
            this.curQuat = getQuatBetweenVectors(startVector, endVector);

            //if (isMinEuclideanDist(startVector, endVector, MIN_DIST)) {
                this.lastQuat = curQuat.mult(this.lastQuat);
                this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
                this.start = new Point2D(event.getX(), event.getY());
                World.moveViewPortByRotator(this.lastQuat);
                Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(
                        DataSet.getBytes(),
                        80)
                );
                this.mainView.setImage(renderedImage);
            //}

        });

        this.mainView.setOnMouseReleased(event -> {


            if (this.start == null) {
                return;
            }
            this.lastQuat = curQuat.mult(this.lastQuat);
//            System.out.println("lastQuat = " + this.lastQuat);
//            System.out.println("lastQuat.magnitude() = " + this.lastQuat.magnitude());
            this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
//            System.out.println("NEW EXACT CURQUAT");
//            System.out.println("lastQuat = " + lastQuat);
//            System.out.println("lastQuat.magnitude() = " + lastQuat.magnitude());
            this.start = null;
            World.moveViewPortByRotator(this.lastQuat);


            Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(
                    DataSet.getBytes(),
                    80)
            );
            this.mainView.setImage(renderedImage);

        });


    }


    private double hyperbolicDepth(double x, double y) {
        return (Math.pow(RADIUS, 2) / 2) / (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
    }

    private double sphereDepth(double x, double y) {
        return Math.sqrt(Math.pow(RADIUS, 2) - Math.pow(x, 2) + Math.pow(y, 2));
    }

    private double[] getCanonical(double x, double y) {
        double[] canonCoords = new double[2]; //[x, y]

        canonCoords[0] = (2 * x - World.VIEW_PLANE_WIDTH - 1) / (World.VIEW_PLANE_WIDTH - 1);
        canonCoords[1] = ((2 * y - World.VIEW_PLANE_HEIGHT - 1) / (World.VIEW_PLANE_WIDTH - 1));

        return canonCoords;
    }

    private Vector3D getProjection(double x, double y) {
        double[] canonCoords = getCanonical(x, y);
        double z;
        x = canonCoords[0];
        y = canonCoords[1];
//        System.out.println("canon x = " + x + " || canon y = " + y);
        if (isInHemisphere(x, y)) {
            z = sphereDepth(x, y);
        } else {
            z = hyperbolicDepth(x, y);
        }
//        System.out.println("depth z = " + z);
        Vector3D v = new Vector3D(canonCoords[0], canonCoords[1], z);
//        System.out.println("v = " + v);
//        System.out.println(v.getX());
//        System.out.println(v.getY());
//        System.out.println(v.getZ());
        return v;
    }

    /**
     * Normalises the input vectors and returns a quaternion
     * based on their cross-product
     *
     * @param start the projection of the start of the mouse movement (on mouse down)
     * @param end   the projection of the end of the mouse movement (on mouse up)
     * @return a quaternion to rotate around the cross-product of the input vectors
     */
    private Quaternion getQuatBetweenVectors(Vector3D start, Vector3D end) {
//        System.out.println("start = " + start);
//        System.out.println("end = " + end);
        Vector3D startNorm = start.normalize();
        Vector3D endNorm = end.normalize();
//        System.out.println("DEGREES BT start AND end = " + Math.toDegrees(Math.acos(startNorm.dotProd(endNorm))));
        Quaternion q = Quaternion.makeExactQuaternionRadians(
                1 + startNorm.dotProd(endNorm),
                startNorm.crossProd(endNorm)
        );
//        System.out.println("q = " + q);
        q= q.normalize();
//        System.out.println("q norm = " + q);

        return q;

    }

    private boolean isInHemisphere(double x, double y) {
        return (Math.pow(x, 2) + Math.pow(y, 2)) <= (Math.pow(RADIUS, 2) / 2d);
    }

    private boolean isMinEuclideanDist(Vector3D start, Vector3D cur, double minDist) {
        double dist = start.getEuclideanDist(cur);
        System.out.println("dist = " + dist);
        return minDist <= start.getEuclideanDist(cur);
    }

}
