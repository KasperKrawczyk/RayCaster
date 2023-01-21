package config;

import javafx.scene.paint.Color;

import java.util.HashMap;

public class CustomConfig implements IConfig {

    private final HashMap<Short, Color> huToColorMap = new HashMap<Short, Color>() {{
        put((short) -99, Color.WHITE);
        put((short) 299, Color.color(1, 0.79, 0.6));
        put((short) 1900, Color.color(0.8902, 0.8549, 0.7882));
        put(Short.MAX_VALUE, Color.WHITE);
    }};

    private String datasetPath;
    private int datasetSize;
    private int datasetHeight;
    private int datasetWidth;

    public CustomConfig(String datasetPath, int datasetSize, int datasetHeight, int datasetWidth) {
        this.datasetPath = datasetPath;
        this.datasetSize = datasetSize;
        this.datasetHeight = datasetHeight;
        this.datasetWidth = datasetWidth;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public int getDatasetSize() {
        return datasetSize;
    }

    public int getDatasetHeight() {
        return datasetHeight;
    }

    public int getDatasetWidth() {
        return datasetWidth;
    }

    @Override
    public HashMap<Short, Color> getHuToColorMap() {
        return null;
    }

    public void setDatasetPath(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public void setDatasetSize(int datasetSize) {
        this.datasetSize = datasetSize;
    }

    public void setDatasetHeight(int datasetHeight) {
        this.datasetHeight = datasetHeight;
    }

    public void setDatasetWidth(int datasetWidth) {
        this.datasetWidth = datasetWidth;
    }
}
