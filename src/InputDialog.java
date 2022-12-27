import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class InputDialog extends BorderPane {

    public static final String PATH_LABEL = "Dataset path";
    public static final String SIZE_LABEL = "Dataset size";
    public static final String HEIGHT_LABEL = "Height";
    public static final String WIDTH_LABEL = "Width";
    public static final String CONFIRM_BTN_MSG = "Ok";
    public static final String HEAD_DATASET_BTN_MSG = "Use head dataset";
    public static final String BRAIN_DATASET_BTN_MSG = "Use brain dataset";
    public static final int STD_INSET = 10;

    private final GridPane gridPane = new GridPane();
    private final FlowPane buttonPane = new FlowPane();

    private final TextField pathTextField = new TextField();
    private final TextField sizeTextField = new TextField();
    private final TextField heightTextField = new TextField();
    private final TextField widthTextField = new TextField();

    private final Button confirmButton = new Button(CONFIRM_BTN_MSG);
    private final Button headButton = new Button(HEAD_DATASET_BTN_MSG);
    private final Button brainButton = new Button(BRAIN_DATASET_BTN_MSG);

    public InputDialog(Stage stage) {

        this.setCenter(this.gridPane);
        this.setBottom(buttonPane);

        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(STD_INSET, STD_INSET, STD_INSET, STD_INSET));

        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setPadding(new Insets(0, STD_INSET, STD_INSET, STD_INSET));
        buttonPane.setAlignment(Pos.BASELINE_RIGHT);

        pathTextField.setPromptText(PATH_LABEL);
        sizeTextField.setPromptText(SIZE_LABEL);
        heightTextField.setPromptText(HEIGHT_LABEL);
        widthTextField.setPromptText(WIDTH_LABEL);

        gridPane.add(new Label(PATH_LABEL), 0, 0);
        gridPane.add(pathTextField, 1, 0);
        gridPane.add(new Label(SIZE_LABEL), 0, 1);
        gridPane.add(sizeTextField, 1, 1);
        gridPane.add(new Label(HEIGHT_LABEL), 0, 2);
        gridPane.add(heightTextField, 1, 2);
        gridPane.add(new Label(WIDTH_LABEL), 0, 3);
        gridPane.add(widthTextField, 1, 3);

        buttonPane.getChildren().addAll(headButton, brainButton, confirmButton);

        confirmButton.setOnAction(event -> {
            Main.setDatasetPath(pathTextField.getText());
            Main.setDatasetSize(Integer.parseInt(sizeTextField.getText()));
            Main.setDatasetHeight(Integer.parseInt(heightTextField.getText()));
            Main.setDatasetWidth(Integer.parseInt(widthTextField.getText()));

            stage.close();

            MainWindow mainWindow = new MainWindow(stage);

        });

        headButton.setOnAction(event -> {
            Main.setDatasetPath(Main.CT_HEAD_PATH);
            Main.setDatasetSize(Main.CT_HEAD_DATASET_SIZE);
            Main.setDatasetHeight(Main.CT_HEAD_SIDE);
            Main.setDatasetWidth(Main.CT_HEAD_SIDE);

            stage.close();

            MainWindow mainWindow = new MainWindow(stage);
            //Test.buildTestPaneRayCast(100, 100);

        });

        brainButton.setOnAction(event -> {
            Main.setDatasetPath(Main.MR_BRAIN_PATH);
            Main.setDatasetSize(Main.MR_BRAIN_DATASET_SIZE);
            Main.setDatasetHeight(Main.MR_BRAIN_SIDE);
            Main.setDatasetWidth(Main.MR_BRAIN_SIDE);

            stage.close();

            MainWindow mainWindow = new MainWindow(stage);
            //Test.buildTestPaneRayCast(100, 100);

        });

        Scene scene = new Scene(this);
        stage.setScene(scene);
        stage.show();

    }

}
