import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class TrackballPane extends StackPane {
    public static final int SIDE = 256;
    public static final float SCALE = (1f / SIDE);
    public static final float RADIUS = 1;

    private WritableImage trackballImage = new WritableImage(SIDE, SIDE);
    private final ImageView mainView;
    private ImageView trackballView = new ImageView(trackballImage);
    private Quaternion lastQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.Y);
    private Quaternion curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
    private Point2D start;

    public TrackballPane(ImageView view) {
        super();
        paintImage();
        this.mainView = view;
        this.getChildren().add(trackballView);
        this.trackballView.setImage(trackballImage);

        this.trackballView.setOnMousePressed(event -> {
            this.start = new Point2D(event.getX(), event.getY());
            System.out.println("start = " + start);

        });

        this.trackballView.setOnMouseDragged(event -> {
            //System.out.println("dragging started");
            if (this.start == null) {
                return;
            }

            Vector3D startVector = getProjection(this.start.getX(), this.start.getY());
//            System.out.println("startVector = " + startVector);
            Vector3D endVector = getProjection(event.getX(), event.getY());
            System.out.println("endVector = " + endVector);
            this.curQuat = getQuatBetweenVectors(startVector, endVector);
//            System.out.println("curQuat.getVector() = " + curQuat.getVector());
//            System.out.println("curQuat.magnitude() = " + curQuat.magnitude());

        });

        this.trackballView.setOnMouseReleased(event -> {


            if (this.start == null) {
                return;
            }
            this.lastQuat = curQuat.mult(this.lastQuat);
//            System.out.println("lastQuat = " + this.lastQuat);
//            System.out.println("lastQuat.magnitude() = " + this.lastQuat.magnitude());
            this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
//            System.out.println("NEW EXACT CURQUAT");
//            System.out.println("curQuat = " + curQuat);
//            System.out.println("curQuat.magnitude() = " + curQuat.magnitude());
            this.start = null;
            World2.moveViewPortByRotator(this.lastQuat);
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

        canonCoords[0] = (2 * x - SIDE - 1) / (SIDE - 1);
        canonCoords[1] = ((2 * y - SIDE - 1) / (SIDE - 1));

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
        Vector3D v = new Vector3D(canonCoords[0], -canonCoords[1], z);
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
        start = start.normalize();
        end = end.normalize();

        return new Quaternion(
                -(1 + start.dotProd(end)),
                start.crossProd(end)
        );

    }

    private boolean isInHemisphere(double x, double y) {
        return (Math.pow(x, 2) + Math.pow(y, 2)) <= (Math.pow(RADIUS, 2) / 2d);
    }

    private void paintImage() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                trackballImage.getPixelWriter().setColor(x, y, Color.color(0.8, 0.2, 0.3));
            }
        }
    }




    public Quaternion getLastQuat() {
        return lastQuat;
    }
}
