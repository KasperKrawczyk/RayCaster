import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Trackball {
    public static final double MIN_DIST = 0.001;
    public static final float RADIUS = 1;
    public static final int NUM_OF_THREADS = 60;

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
            Vector3D endVector = getProjection(event.getX(), event.getY());
            this.curQuat = Quaternion.getQuatBetweenVectors(startVector, endVector);

            //if (isMinEuclideanDist(startVector, endVector, MIN_DIST)) {
                this.lastQuat = curQuat.mult(this.lastQuat).normalize();
                this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
                this.start = new Point2D(event.getX(), event.getY());
                Camera.moveViewPortByRotator(this.lastQuat);
                Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(
                        DataSet.getBytes(),
                        NUM_OF_THREADS)
                );
                this.mainView.setImage(renderedImage);
            //}

        });

        this.mainView.setOnMouseReleased(event -> {


            if (this.start == null) {
                return;
            }
            this.lastQuat = curQuat.mult(this.lastQuat).normalize();
            this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
            this.start = null;
            Camera.moveViewPortByRotator(this.lastQuat);


            Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(
                    DataSet.getBytes(),
                    NUM_OF_THREADS)
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

        canonCoords[0] = (2 * x - Camera.VIEW_PLANE_WIDTH - 1) / (Camera.VIEW_PLANE_WIDTH - 1);
        canonCoords[1] = ((2 * y - Camera.VIEW_PLANE_HEIGHT - 1) / (Camera.VIEW_PLANE_WIDTH - 1));

        return canonCoords;
    }

    private Vector3D getProjection(double x, double y) {
        double[] canonCoords = getCanonical(x, y);
        double z;
        x = canonCoords[0];
        y = canonCoords[1];
        if (isInHemisphere(x, y)) {
            z = sphereDepth(x, y);
        } else {
            z = hyperbolicDepth(x, y);
        }
        Vector3D v = new Vector3D(canonCoords[0], -canonCoords[1], z);
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
        Vector3D startNorm = start.normalize();
        Vector3D endNorm = end.normalize();
        Quaternion q = Quaternion.makeExactQuaternionRadians(
                1 + startNorm.dotProd(endNorm),
                startNorm.crossProd(endNorm)
        );
        q = q.normalize();
        return q;

    }

    private boolean isInHemisphere(double x, double y) {
        return (Math.pow(x, 2) + Math.pow(y, 2)) <= (Math.pow(RADIUS, 2) / 2d);
    }

    private boolean isMinEuclideanDist(Vector3D start, Vector3D cur, double minDist) {
        double dist = start.getEuclideanDist(cur);
        return minDist <= start.getEuclideanDist(cur);
    }

}
