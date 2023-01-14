package config;

import java.io.File;


public class BrainConfig implements IConfig {

    public static final String MR_BRAIN_PATH = String.join(File.separator, "resources", "MRbrain");
    public static final int MR_BRAIN_DATASET_SIZE = 109;
    public static final int MR_BRAIN_SIDE = 256;

    private String datasetPath = MR_BRAIN_PATH;
    private int datasetSize = MR_BRAIN_DATASET_SIZE;
    private int datasetHeight = MR_BRAIN_SIDE;
    private int datasetWidth = MR_BRAIN_SIDE;

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
