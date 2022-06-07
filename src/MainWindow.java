import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the main window of the application
 * along with the tools necessary to build it and the thumbnail window
 *
 * @author Kasper Krawczyk
 */
public class MainWindow extends BorderPane {

    public static final String MAIN_TITLE = "CThead viewer";
    public static final int MAIN_SIDE = 1024;
    public static final String BILINEAR_BTN_MSG = "Bilinear";
    public static final String RENDER_BTN_MSG = "Volume Rendering";
    public static final String CT_HEAD_PATH = "CThead";
    public static final int CT_HEAD_SIDE = 256;



    public static final int MIN_SIZE_SLIDER_VAL = 32;
    public static final int MAX_SIZE_SLIDER_VAL = 512;
    public static final int TOUCH_PANE_SIDE = 256;


    private final HBox topHBox;
    private final VBox rightVBox;
    private final Slider sizeSlider;
    private final Slider angleSlider;
    private final ToggleButton renderButton;

    private int currentSize;
    private Algo currentAlgo;
    private DataSet dataSet;
    private Image mainImage;
    private ImageView mainView;
    private TrackballPane trackballPane;
    private Trackball trackball;
    private String datasetPath;
    private int datasetSize;
    private int datasetHeight;
    private int datasetWidth;



    /**
     * Creates the main window of the application
     * @param stage the stage on which to build the window
     */
    public MainWindow(Stage stage){
        stage.setTitle(MAIN_TITLE);
        this.setMinHeight(MAIN_SIDE);
        this.setMinWidth(MAIN_SIDE);

        datasetPath = Main.getDatasetPath();
        datasetSize = Main.getDatasetSize();
        datasetHeight = Main.getDatasetHeight();
        datasetWidth = Main.getDatasetWidth();

        this.dataSet = new DataSet(datasetPath,
                datasetSize,
                datasetHeight,
                datasetWidth);
        this.currentAlgo = Algo.BILINEAR;
        this.currentSize = CT_HEAD_SIDE;


        this.mainView = new ImageView(mainImage);
        this.sizeSlider = new Slider(MIN_SIZE_SLIDER_VAL, MAX_SIZE_SLIDER_VAL, CT_HEAD_SIDE);
        this.angleSlider = new Slider(0.0, 360.0, 0.0);
        this.renderButton = new RadioButton(RENDER_BTN_MSG);
        this.trackballPane = new TrackballPane(this.mainView);
        this.topHBox = new HBox();
        this.topHBox.getChildren().addAll(sizeSlider, angleSlider, renderButton);
        this.rightVBox = new VBox();
        this.rightVBox.getChildren().addAll(buildLightTouchpane(), this.trackballPane);
        World.initWorld();
        this.trackball = new Trackball(mainView);

//        Image initRender = (VolumeRenderer.volumeRayCastParallelized(DataSet.getBytes(),
//                80));
//        mainView.setImage(initRender);




        this.renderButton.setOnAction(event -> {
            Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(DataSet.getBytes(),
                    80));
            mainView.setImage(renderedImage);
        });

        this.sizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number >
                                        observable, Number oldValue, Number newValue) {

                currentSize = newValue.intValue();
                mainView.setImage(null);
                Image rescaledImage;

                //rescaledImage =


            }
        });

        angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number >
                                        observable, Number oldValue, Number newValue) {
                World.initWorld();
                World.moveViewPortByAngleDegrees(-newValue.doubleValue());

                Image renderedImage = (VolumeRenderer.volumeRayCastParallelized(DataSet.getBytes(),
                        80));
                mainView.setImage(renderedImage);
            }
        });




        this.setTop(topHBox);
        this.setRight(rightVBox);
        this.setCenter(mainView);

        Scene scene = new Scene(this, MAX_SIZE_SLIDER_VAL, MAX_SIZE_SLIDER_VAL +  topHBox.getMaxHeight());
        stage.setScene(scene);
        stage.show();

    }


    /**
     * Builds the thumbnail window at the given x and y coordinates
     * @return StackPane object
     */
    public StackPane buildLightTouchpane() {
        StackPane touchStackPane = new StackPane();
        AtomicInteger yDelta = new AtomicInteger();


        WritableImage headImage = new WritableImage(TOUCH_PANE_SIDE, TOUCH_PANE_SIDE);
        ImageView touchView = new ImageView(headImage);
        touchStackPane.getChildren().add(touchView);

        PixelWriter pixelWriter = headImage.getPixelWriter();
        for(int y = 0; y < TOUCH_PANE_SIDE; y++){
            for(int x = 0; x < TOUCH_PANE_SIDE; x++){
                pixelWriter.setColor(x, y, Color.color(0, 0.2, 1));
            }
        }

        touchView.setOnScroll(event -> {
            if (event.getDeltaY() > 0 && yDelta.get() < 500) {
                yDelta.set(yDelta.intValue() + (int) event.getMultiplierX() / 4);
            } else if (yDelta.get() > -500) {
                yDelta.set(yDelta.intValue() - (int) event.getMultiplierX() / 4);
            }

            Vector3D light = new Vector3D(127 - (int) event.getX(), 127 - (int) event.getY(), yDelta.get());
            Vector3D eye = new Vector3D(127, 127, -200);
            System.out.println(yDelta);
            Image updatedImage = Util.updateRendering(currentSize,
                    currentSize,
                    currentAlgo,
                    light,
                    eye);
            mainView.setImage(null);
            mainView.setImage(updatedImage);


            event.consume();
        });

        touchView.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_MOVED, event -> {
            Vector3D light = new Vector3D(127 - (int) event.getX(), 127 - (int) event.getY(), 200);
            Vector3D eye = new Vector3D(127, 127, -200);

            Image updatedImage = Util.updateRendering(currentSize,
                    currentSize,
                    currentAlgo,
                    light,
                    eye);
            mainView.setImage(null);
            mainView.setImage(updatedImage);


            event.consume();
        });

        return touchStackPane;
    }

    public ImageView getMainView() {
        return mainView;
    }
}
