package config;


import javafx.scene.paint.Color;

import java.io.File;
import java.util.HashMap;

public class HeadConfig implements IConfig {

    public static final String CT_HEAD_PATH = String.join(File.separator, "resources", "CThead");
    public static final int CT_HEAD_DATASET_SIZE = 113;
    public static final int CT_HEAD_SIDE = 256;

    private final HashMap<Short, Color> huToColorMap = new HashMap<Short, Color>() {{
        put((short) -99, Color.WHITE);
        put((short) 299, Color.color(1, 0.79, 0.6));
        put((short) 1900, Color.color(0.8902, 0.8549, 0.7882));
        put(Short.MAX_VALUE, Color.WHITE);
    }};

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
