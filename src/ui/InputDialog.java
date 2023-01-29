package ui;

import config.BrainConfig;
import config.CustomConfig;
import config.HeadConfig;
import config.IConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.TabContentWindow;

public class InputDialog extends BorderPane {

    public static final String PATH_LABEL = "Dataset path";
    public static final String SIZE_LABEL = "Dataset size";
    public static final String HEIGHT_LABEL = "Height";
    public static final String WIDTH_LABEL = "Width";
    public static final String CONFIRM_BTN_MSG = "Ok";
    public static final int STD_INSET = 10;

    private final GridPane gridPane = new GridPane();
    private final FlowPane buttonPane = new FlowPane();

    private final TextField pathTextField = new TextField();
    private final TextField sizeTextField = new TextField();
    private final TextField heightTextField = new TextField();
    private final TextField widthTextField = new TextField();

    private final Button confirmButton = new Button(CONFIRM_BTN_MSG);

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

        buttonPane.getChildren().addAll(confirmButton);

        confirmButton.setOnAction(event -> {

            if (pathTextField.getText().trim().isEmpty() ||
                    sizeTextField.getText().trim().isEmpty() ||
                    heightTextField.getText().trim().isEmpty() ||
                    widthTextField.getText().trim().isEmpty()) {
                buildMainTabPane(stage);

            } else {
                IConfig customConfig = new CustomConfig(
                        pathTextField.getText(),
                        Integer.parseInt(sizeTextField.getText()),
                        Integer.parseInt(heightTextField.getText()),
                        Integer.parseInt(widthTextField.getText())
                );
                buildMainTabPane(stage, customConfig);
            }
//            stage.close();
        });

        Scene scene = new Scene(this);
        stage.setScene(scene);
        stage.show();

    }

    private void buildMainTabPane(Stage stage, IConfig customConfig) {
        TabPane tabPane = new TabPane();
        VBox tabVBox = new VBox(tabPane);
        Tab headTab = new Tab("Head", new TabContentWindow(stage, new HeadConfig()));
//        Tab multiHeadTab = new Tab("Multi Head");
        Tab brainTab = new Tab("Brain", new TabContentWindow(stage, new BrainConfig()));
        Tab customTab = new Tab("Custom Dataset", new TabContentWindow(stage, customConfig));

        tabPane.getTabs().addAll(headTab);
        Scene scene = new Scene(tabVBox);
        stage.setScene(scene);
        stage.show();
    }

    private void buildMainTabPane(Stage stage) {
        System.out.println("Non-custom");
        TabPane tabPane = new TabPane();
        VBox tabVBox = new VBox(tabPane);
        Tab headTab = new Tab("Head", new TabContentWindow(stage, new HeadConfig()));
//        Tab multiHeadTab = new Tab("Multi Head");
//        Tab brainTab = new Tab("Brain", new ui.TabContentWindow(stage, new BrainConfig()));

        tabPane.getTabs().addAll(headTab);
        Scene scene = new Scene(tabVBox);
        stage.setScene(scene);
        stage.show();
    }

}
