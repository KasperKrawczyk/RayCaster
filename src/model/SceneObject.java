package model;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SceneObject {
    private final AABB aabb;
    private final short[][][] vol;
    private final NavigableMap<Short, Color> huToColorMap = new TreeMap<>();

    public SceneObject(AABB aabb, short[][][] vol, HashMap<Short, Color> huToColorMap) {
        this.aabb = aabb;
        this.vol = vol;
        this.huToColorMap.putAll(huToColorMap);
    }

    public AABB getAabb() {
        return aabb;
    }

    public short[][][] getVol() {
        return vol;
    }

    public NavigableMap<Short, Color> getHuToColorMap() {
        return huToColorMap;
    }
}
