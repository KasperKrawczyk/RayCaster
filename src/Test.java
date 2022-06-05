import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Test {
    private static ImageView view;
    private static int width = 512;
    private static int height = 512;
    private static int centreX = 8;
    private static int centreY = 8;
    private static double maxDist = 416.73601547291895;
    private static double minDist = 1;
    private static Vector3D n = new Vector3D(0, 0, 1);

    public static void main(String[] args) {

        buildTestPane(100, 100);

    }


    /**
     * Builds the thumbnail window at the given x and y coordinates
     * @param atX the x coordinate of the upper left corner of the new window
     * @param atY the y coordinate of the upper left corner of the new window
     */
    public static void buildTestPane(double atX, double atY) {
        StackPane stackPane = new StackPane();


        WritableImage image = new WritableImage(width, height);
        ImageView thumbView = new ImageView(image);
        stackPane.getChildren().add(thumbView);
        PixelWriter pixelWriter = image.getPixelWriter();




        Scene testScene = new Scene(stackPane, image.getWidth(), image.getHeight());

        stackPane.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {

            double thetaRad = Math.toRadians(30);
            Vector3D n = new Vector3D(0, 0, 1).normalize().rotateY(-thetaRad).rotateX(-thetaRad / 3);
            AABB aabb = new AABB(
                    new Vector3D(255, 113, 255).add(new Vector3D(0, 0, 0)),
                    new Vector3D(0, 0, 0).add(new Vector3D(0, 0, 0))
            );



            for (int y = -height / 2; y < height / 2; y++) {
                for (int x = -width / 2; x < width / 2; x++) {
                    Vector3D v = new Vector3D(x, y, 0);
                    Point3D o = Point3D.Point3DfromVector(v);
                    Ray ray = new Ray(v, n);
                    Vector3D[] intersectionPoints = aabb.getIntersections(ray, 0, Float.MAX_VALUE);


                    double colorMultiplier = 0;
                    if(intersectionPoints != null) {
                        System.out.println(intersectionPoints[1].sub(intersectionPoints[0]).magnitude());
                        Point3D intersectionPointNear = Point3D.Point3DfromVector(intersectionPoints[0]);
                        double distance = o.distance(intersectionPointNear);
                        maxDist = Math.max(maxDist, distance);
                        minDist = Math.min(minDist, distance);
                        colorMultiplier = (distance - minDist) / (maxDist - minDist);
                        pixelWriter.setColor(x + width / 2 - 30, y + height / 2 - 30,
                                Color.color(Color.BLUE.getRed() * colorMultiplier,
                                        Color.BLUE.getGreen() * colorMultiplier,
                                        Color.BLUE.getBlue() * colorMultiplier
                                ));
                    }

                    if (aabb.getCloserIntersection(ray, 0, Float.MAX_VALUE) != null) {

                    }

                }

            }
            event.consume();
        });


        //Build and display the new window
        Stage newWindow = new Stage();
        newWindow.setTitle("Test");
        newWindow.setScene(testScene);

        // Set position of second window, related to primary window.
        newWindow.setX(atX);
        newWindow.setY(atY);

        newWindow.show();
    }

    /**
     * Builds the thumbnail window at the given x and y coordinates
     * @param atX the x coordinate of the upper left corner of the new window
     * @param atY the y coordinate of the upper left corner of the new window
     */
    public static void buildTestPaneRayCast(double atX, double atY) {
        BorderPane borderPane = new BorderPane();
        Slider angleSlider = new Slider(0.0, 360.0, 0.0);
        view = new ImageView();


        WritableImage image = new WritableImage(width, height);
        borderPane.setTop(angleSlider);
        borderPane.setCenter(view);

        angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number >
                                        observable, Number oldValue, Number newValue) {
                World.setViewPlaneAngle(-newValue.doubleValue());
//                Image newImage = (VolumeRenderer.volumeRayCast(DataSet.getBytes()));
                //Image newImage = (VolumeRenderer.volumeRayCastParallelized(DataSet.getBytes(), 80));


                //borderPane.setCenter(new ImageView(Util.rescaleBilinearColour(512, 512, newImage)));
                //borderPane.setCenter(new ImageView(newImage));
            }
        });



        Scene testScene = new Scene(borderPane, image.getWidth(), image.getHeight());



        //Build and display the new window
        Stage newWindow = new Stage();
        newWindow.setTitle("Ray Caster Test");
        newWindow.setScene(testScene);

        // Set position of second window, related to primary window.
        newWindow.setX(atX);
        newWindow.setY(atY);

        newWindow.show();
    }
}
