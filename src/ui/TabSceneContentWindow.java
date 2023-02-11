package ui;

import component.AbstractVolumeRenderer;
import component.SceneVolumeRenderer;
import component.camera.SceneCamera;
import component.camera.SingleObjectCamera;
import config.IConfig;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import mathutil.ImageUtil;
import model.*;

import java.util.concurrent.atomic.AtomicInteger;

public class TabSceneContentWindow extends BorderPane {


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
    private final VBox leftVBox;
    private final Slider sizeSlider;
    private final Slider angleSlider;
    private final ToggleButton renderButton;

    private Algo currentAlgo;
    private DataSet dataSet;
    private AbstractVolumeRenderer volumeRenderer;
    private Image mainImage;
    private ListView<ColorMappingListItem> colorMappingList;
    private ImageView mainView;
    private TrackballPane trackballPane;
    private Trackball trackball;
    private SceneCamera sceneCamera;
    private model.Scene scene;
    private int currentSize;


    /**
     * Creates the main window of the application
     *
     * @param stage the stage on which to build the window
     */
    public TabSceneContentWindow(Stage stage, IConfig config) {
        stage.setTitle(MAIN_TITLE);
        this.setMinHeight(MAIN_SIDE);
        this.setMinWidth(MAIN_SIDE);

        this.dataSet = new DataSet(
                config.getDatasetPath(),
                config.getDatasetSize(),
                config.getDatasetHeight(),
                config.getDatasetWidth()
        );

        this.scene = TestSceneBuilder.buildScene(dataSet.getBytes());
        this.sceneCamera = new SceneCamera(config, this.scene.getCentroid());

        this.currentAlgo = Algo.BILINEAR;
        this.currentSize = CT_HEAD_SIDE;
        this.volumeRenderer = new SceneVolumeRenderer(sceneCamera, scene);

        this.mainView = new ImageView(mainImage);
        this.sizeSlider = new Slider(MIN_SIZE_SLIDER_VAL, MAX_SIZE_SLIDER_VAL, CT_HEAD_SIDE);
        this.angleSlider = new Slider(0.0, 90.0, 0.0);
        this.renderButton = new RadioButton(RENDER_BTN_MSG);
        this.trackballPane = new TrackballPane(mainView, volumeRenderer, sceneCamera, dataSet);
        this.topHBox = new HBox();
        this.topHBox.getChildren().addAll(sizeSlider, angleSlider, renderButton);
        this.rightVBox = new VBox();
        this.leftVBox = new VBox();
        this.rightVBox.getChildren().addAll(
                buildLightTouchpane(),
                trackballPane,
                buildLightInputsGrid(),
                buildCameraInputs());
        this.leftVBox.getChildren().addAll(
                buildColorMappingVBox());
        this.trackball = new Trackball(mainView, volumeRenderer, sceneCamera, dataSet);

        ImageUtil.writeHistogram(dataSet.getBytes());

//        Image initRender = (component.VolumeRenderer.volumeRayCastParallelized(model.DataSet.getBytes(),
//                80));
//        mainView.setImage(initRender);


        this.renderButton.setOnAction(event -> {
            Image renderedImage = (volumeRenderer.volumeRayCastParallelized(dataSet.getBytes(),
                    Trackball.NUM_OF_THREADS));
            mainView.setImage(renderedImage);
        });

        this.sizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number>
                                        observable, Number oldValue, Number newValue) {

                currentSize = newValue.intValue();
                mainView.setImage(null);
                Image rescaledImage;

                //rescaledImage =


            }
        });

        angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number>
                                        observable, Number oldValue, Number newValue) {
                sceneCamera.initCamera();

                System.out.println(newValue.doubleValue());
                sceneCamera.moveViewPortByAngleDegrees(newValue.doubleValue());

                Image renderedImage = (volumeRenderer.volumeRayCastParallelized(dataSet.getBytes(),
                        Trackball.NUM_OF_THREADS));
                mainView.setImage(renderedImage);
            }
        });


        this.setTop(topHBox);
        this.setRight(rightVBox);
        this.setCenter(mainView);
        this.setLeft(leftVBox);

        Scene scene = new Scene(this, MAIN_SIDE, MAIN_SIDE);
        stage.setScene(scene);
        stage.show();

    }


    /**
     * Builds the thumbnail window at the given x and y coordinates
     *
     * @return StackPane object
     */
    public StackPane buildLightTouchpane() {
        StackPane touchStackPane = new StackPane();
        AtomicInteger yDelta = new AtomicInteger();


        WritableImage headImage = new WritableImage(TOUCH_PANE_SIDE, TOUCH_PANE_SIDE);
        ImageView touchView = new ImageView(headImage);
        touchStackPane.getChildren().add(touchView);

        PixelWriter pixelWriter = headImage.getPixelWriter();
        for (int y = 0; y < TOUCH_PANE_SIDE; y++) {
            for (int x = 0; x < TOUCH_PANE_SIDE; x++) {
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
//            System.out.println(yDelta);
            Image updatedImage = ImageUtil.updateRendering(
                    dataSet,
                    currentSize,
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

            Image updatedImage = ImageUtil.updateRendering(
                    dataSet,
                    currentSize,
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

    public GridPane buildLightInputsGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);
        Label labelX = new Label("Light x:");
        TextField textFieldX = new TextField();
        Label labelY = new Label("Light y:");
        TextField textFieldY = new TextField();
        Label labelZ = new Label("Light z:");
        TextField textFieldZ = new TextField();
        Button submitBtn = new Button("Submit");
        submitBtn.setOnAction(event -> {
            String xString = textFieldX.getText();
            String yString = textFieldY.getText();
            String zString = textFieldZ.getText();
            if (StringUtil.isNotEmpty(xString)
                    && StringUtil.isNotEmpty(yString)
                    && StringUtil.isNotEmpty(zString)) {
                double newX;
                double newY;
                double newZ;
                try {
                    newX = Double.parseDouble(xString);
                    newY = -Double.parseDouble(yString);
                    newZ = Double.parseDouble(zString);
                } catch (NumberFormatException nfe) {
                    return;
                }
                sceneCamera.moveLightTo(new Point3D(newX, newY, newZ));
                Image renderedImage = (volumeRenderer.volumeRayCastParallelized(dataSet.getBytes(),
                        Trackball.NUM_OF_THREADS));
                mainView.setImage(renderedImage);
            }
        });
        GridPane.setConstraints(labelX, 0, 0);
        GridPane.setConstraints(textFieldX, 1, 0);
        GridPane.setConstraints(labelY, 0, 1);
        GridPane.setConstraints(textFieldY, 1, 1);
        GridPane.setConstraints(labelZ, 0, 2);
        GridPane.setConstraints(textFieldZ, 1, 2);
        GridPane.setConstraints(submitBtn, 0, 3);
        grid.getChildren().addAll(labelX, textFieldX, labelY, textFieldY, labelZ, textFieldZ, submitBtn);
        return grid;
    }

    public VBox buildCameraInputs() {
        Slider slider = new Slider(-800, 400, 0);
        Label sliderLabel = new Label("component.camera.Camera distance");

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number>
                                        observable, Number oldValue, Number newValue) {
                sceneCamera.updateViewPort(newValue.intValue());
                System.out.println(newValue);
                Image renderedImage = (volumeRenderer.volumeRayCastParallelized(dataSet.getBytes(),
                        Trackball.NUM_OF_THREADS));
                mainView.setImage(renderedImage);
            }
        });

        VBox vBox = new VBox(sliderLabel, slider);
        return vBox;
    }

    private VBox buildColorMappingVBox() {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        ListView<ColorMappingListItem> listView = new ListView<>();
        CMLIGrid grid = new CMLIGrid(listView, volumeRenderer);
        vBox.getChildren().addAll(listView, grid);
        listView.setEditable(true);
        listView.setMaxWidth(330);
        listView.setItems(FXCollections.observableArrayList(
                new ColorMappingListItem(Color.WHITE, (short) -99),
                new ColorMappingListItem(Color.color(1, 0.79, 0.6), (short) 299),
                new ColorMappingListItem(Color.color(0.8902, 0.8549, 0.7882), (short) 1900),
                new ColorMappingListItem(Color.WHITE, Short.MAX_VALUE)
        ));
        listView.setCellFactory(lv -> new ListCell<ColorMappingListItem>() {
            private final TextField textField = new TextField();

            {
                textField.setOnAction(e -> {
                    commitEdit(getItem());
                });
                textField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
            }

            @Override
            protected void updateItem(ColorMappingListItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getShortDescription());
                    setGraphic(new Rectangle(20, 20, item.getColor()));
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                setText(null);
                setGraphic(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem().getShortDescription());
                setGraphic(new Rectangle(20, 20, getItem().getColor()));
            }

            @Override
            public void commitEdit(ColorMappingListItem item) {
                super.commitEdit(item);
                setText(item.getShortDescription());
                setGraphic(new Rectangle(20, 20, item.getColor()));
            }
        });

        // for debugging:
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                listView.getItems().forEach(p -> System.out.println(p.getShortDescription()));
            }
        });

        return vBox;
    }


}
