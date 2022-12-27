import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class TrackballPane extends StackPane {
    public static final int SIDE = 256;
    public static final float RADIUS = 1;

    private WritableImage trackballImage = new WritableImage(SIDE, SIDE);
    private final ImageView mainView;
    private ImageView trackballView = new ImageView(trackballImage);
    private Quaternion lastQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
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
        });

        this.trackballView.setOnMouseDragged(event -> {
            if (this.start == null) {
                return;
            }
            Vector3D startVector = getProjection(this.start.getX(), this.start.getY());
            Vector3D endVector = getProjection(event.getX(), event.getY());
            this.curQuat = Quaternion.getQuatBetweenVectors(startVector, endVector);

        });

        this.trackballView.setOnMouseReleased(event -> {
            if (this.start == null) {
                return;
            }
            this.lastQuat = curQuat.mult(this.lastQuat).normalize();
            this.curQuat = Quaternion.makeExactQuaternionRadians(1, Vector3D.NULL);
            this.start = null;
            Camera.moveViewPortByRotator(this.lastQuat);

            Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(
                    DataSet.getBytes(),
                    Trackball.NUM_OF_THREADS)
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
        if (isInHemisphere(x, y)) {
            z = sphereDepth(x, y);
        } else {
            z = hyperbolicDepth(x, y);
        }
        return new Vector3D(canonCoords[0], -canonCoords[1], z);
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

}
