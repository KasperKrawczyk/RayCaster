package config;

import java.io.File;


public class HeadConfig implements IConfig {

    public static final String CT_HEAD_PATH = String.join(File.separator, "resources", "CThead");
    public static final int CT_HEAD_DATASET_SIZE = 113;
    public static final int CT_HEAD_SIDE = 256;

    private String datasetPath = CT_HEAD_PATH;
    private int datasetSize = CT_HEAD_DATASET_SIZE;
    private int datasetHeight = CT_HEAD_SIDE;
    private int datasetWidth = CT_HEAD_SIDE;

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
