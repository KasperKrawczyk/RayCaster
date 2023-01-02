package model;

import component.VolumeRenderer;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.Comparator;

public class CMLIGrid extends GridPane {
    private final VolumeRenderer volumeRenderer;
    private final Slider rSlider = new Slider(0.0, 255.0, 0.0);
    private final Slider gSlider = new Slider(0.0, 255.0, 0.0);
    private final Slider bSlider = new Slider(0.0, 255.0, 0.0);
    private final Slider ceilingSlider = new Slider(-200, 2500, 0.0);
    private final Label rLabel = new Label("Red: ");
    private final Label gLabel = new Label("Green: ");
    private final Label bLabel = new Label("Blue: ");
    private final Label floorLabel = new Label("Floor HU: ");
    private final TextField rTextField = new TextField("Red");
    private final TextField gTextField = new TextField("Green");
    private final TextField bTextField = new TextField("Blue");
    private final TextField ceilingTextField = new TextField("HU ceil val (incl)");
    private final Button removeButton = new Button("Remove mapping");

    public CMLIGrid(ListView<ColorMappingListItem> listView, VolumeRenderer volumeRenderer) {
        this.volumeRenderer = volumeRenderer;
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setVgap(5);
        this.setHgap(5);
        this.getChildren().addAll(
                rSlider, gSlider, bSlider, ceilingSlider,
                rLabel, gLabel, bLabel, floorLabel,
                rTextField, gTextField, bTextField, ceilingTextField,
                removeButton
        );
        this.rTextField.setStyle("-fx-control-inner-background: rgba(190,27,77,0.5); -fx-text-fill: rgba(125,125,128,0.83);");
        this.gTextField.setStyle("-fx-control-inner-background: rgba(94,204,58,0.5); -fx-text-fill: rgba(125,125,128,0.83);");
        this.bTextField.setStyle("-fx-control-inner-background: rgba(24,149,226,0.5); -fx-text-fill: rgba(125,125,128,0.83);");
        this.ceilingTextField.setStyle("-fx-control-inner-background: rgba(125,125,128,0.5); -fx-text-fill: rgba(125,125,128,0.83);");
        GridPane.setConstraints(rTextField, 0, 0);
        GridPane.setConstraints(gTextField, 1, 0);
        GridPane.setConstraints(bTextField, 0, 1);
        GridPane.setConstraints(ceilingTextField, 1, 1);
        GridPane.setConstraints(rLabel,0, 2);
        GridPane.setConstraints(rSlider,1, 2, 2, 1);

        GridPane.setConstraints(gLabel,0, 3);
        GridPane.setConstraints(gSlider,1, 3, 2, 1);

        GridPane.setConstraints(bLabel,0, 4);
        GridPane.setConstraints(bSlider,1, 4, 2, 1);

        GridPane.setConstraints(floorLabel,0, 5);
        GridPane.setConstraints(ceilingSlider,1, 5, 2, 1);

        GridPane.setConstraints(removeButton,0, 6, 2, 1);

        rSlider.setOnMouseDragged(event -> rTextField.setText(String.valueOf(rSlider.getValue())));
        gSlider.setOnMouseDragged(event -> gTextField.setText(String.valueOf(gSlider.getValue())));
        bSlider.setOnMouseDragged(event -> bTextField.setText(String.valueOf(bSlider.getValue())));
        ceilingSlider.setOnMouseDragged(event -> ceilingTextField.setText(String.valueOf(ceilingSlider.getValue())));

        rSlider.setOnMousePressed(event -> rTextField.setText(String.valueOf(rSlider.getValue())));
        gSlider.setOnMousePressed(event -> gTextField.setText(String.valueOf(gSlider.getValue())));
        bSlider.setOnMousePressed(event -> bTextField.setText(String.valueOf(bSlider.getValue())));
        ceilingSlider.setOnMousePressed(event -> ceilingTextField.setText(String.valueOf(ceilingSlider.getValue())));

        this.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                rTextField.clear();
                rSlider.setValue(0);
                gTextField.clear();
                gSlider.setValue(0);
                bTextField.clear();
                bSlider.setValue(0);
                ceilingTextField.clear();
                ceilingSlider.setValue(0);
            }
        });

        this.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (rTextField.getText() == null
                        || gTextField.getText() == null
                        || bTextField.getText() == null
                        || ceilingTextField.getText() == null) {
                    e.consume();
                    return;
                }
                int r = (int) Double.parseDouble(rTextField.getText());
                int g = (int) Double.parseDouble(gTextField.getText());
                int b = (int) Double.parseDouble(bTextField.getText());
                short ceiling = (short) (Double.parseDouble(ceilingTextField.getText()));
                String webColor = "rgb(" + r + "," + g + "," + b + ")";
                Color c = Color.web(webColor, 1);
                listView.getItems().add(new ColorMappingListItem(c, ceiling));
                volumeRenderer.addHuToColorMapping(ceiling, c);
                listView.getItems().sort(Comparator.comparingInt(ColorMappingListItem::getCeiling));
            }
        });

        this.removeButton.setOnAction(event -> {
            if (listView.getFocusModel().getFocusedItem() == null) {
                event.consume();
                return;
            }
            int idxToRemove = listView.getFocusModel().getFocusedIndex();
            ColorMappingListItem cmli = listView.getItems().remove(idxToRemove);
            volumeRenderer.removeHuToColorMapping(cmli.getCeiling());
            listView.getItems().sort(Comparator.comparingInt(ColorMappingListItem::getCeiling));

        });

    }

//    public String getShortDescription() {
//        double r = Double.parseDouble(rTextField.getText());
//        double g = Double.parseDouble(gTextField.getText());
//        double b = Double.parseDouble(bTextField.getText());
//        short floor = Short.parseShort(floorTextField.getText());
//        return
//    }

    public Slider getRSlider() {
        return rSlider;
    }

    public Slider getgSlider() {
        return gSlider;
    }

    public Slider getbSlider() {
        return bSlider;
    }

    public Label getrLabel() {
        return rLabel;
    }

    public Label getgLabel() {
        return gLabel;
    }

    public Label getbLabel() {
        return bLabel;
    }

    public TextField getrTextField() {
        return rTextField;
    }

    public TextField getgTextField() {
        return gTextField;
    }

    public TextField getbTextField() {
        return bTextField;
    }

    public Slider getrSlider() {
        return rSlider;
    }

    public Slider getCeilingSlider() {
        return ceilingSlider;
    }

    public Label getFloorLabel() {
        return floorLabel;
    }

    public TextField getCeilingTextField() {
        return ceilingTextField;
    }
}
