package config;

import javafx.scene.paint.Color;

import java.util.HashMap;

public interface IConfig {
    HashMap<Short, Color> getHuToColorMap();
    String getDatasetPath();
    int getDatasetSize();
    int getDatasetHeight();
    int getDatasetWidth();
}
