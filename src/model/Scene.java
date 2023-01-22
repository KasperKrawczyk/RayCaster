package model;

import java.util.ArrayList;

public class Scene {
    private final ArrayList<SceneObject> sceneObjects = new ArrayList<>();
    private final Point3D min;
    private final Point3D max;

    private Vector3D light;

    public Scene(Vector3D light) {
        this.min = new Point3D(-1000, -1000, -1000);
        this.max = new Point3D(1000, 1000, 1000);
        this.light = light;
    }

    public ArrayList<SceneObject> getSceneObjects() {
        return sceneObjects;
    }

    public Point3D getMin() {
        return min;
    }

    public Point3D getMax() {
        return max;
    }

    public Vector3D getLight() {
        return light;
    }

    public void setLight(Vector3D light) {
        this.light = light;
    }
}
