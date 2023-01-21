package config;

import javafx.scene.paint.Color;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class BrainConfig implements IConfig {

    public static final String MR_BRAIN_PATH = String.join(File.separator, "resources", "MRbrain");
    public static final int MR_BRAIN_DATASET_SIZE = 109;
    public static final int MR_BRAIN_SIDE = 256;

    private final HashMap<Short, Color> huToColorMap = new HashMap<Short, Color>() {{
        put((short) -99, Color.WHITE);
        put((short) 299, Color.color(1, 0.79, 0.6));
        put((short) 1900, Color.color(0.8902, 0.8549, 0.7882));
        put(Short.MAX_VALUE, Color.WHITE);
    }};

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

    public HashMap<Short, Color> getHuToColorMap() {
        return huToColorMap;
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
