package config;

import java.io.File;

public class CustomConfig implements IConfig {

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
