package model;

import java.util.ArrayList;

public class Scene {
    private final ArrayList<SceneObject> sceneObjects = new ArrayList<>();
    private final Point3D min;
    private final Point3D max;
    private final Point3D centroid = new Point3D(0, 0, 0);

    private Vector3D light;

    public Scene(Vector3D light) {
        this.min = new Point3D(-1000, -1000, -1000);
        this.max = new Point3D(1000, 1000, 1000);
        this.light = light;

    }

    public Scene(Vector3D light, ArrayList<SceneObject> sceneObjects) {
        this(light);
        this.sceneObjects.addAll(sceneObjects);
        this.setCentroid();
    }

    private void setCentroid() {
        int numSceneObjects = this.sceneObjects.size();
        for (SceneObject so : this.sceneObjects) {
            centroid.setX(centroid.getX() + so.getAabb().getCentre().getX());
            centroid.setY(centroid.getY() + so.getAabb().getCentre().getY());
            centroid.setZ(centroid.getZ() + so.getAabb().getCentre().getZ());
        }
        this.centroid.setX(this.centroid.getX() / numSceneObjects);
        this.centroid.setY(this.centroid.getY() / numSceneObjects);
        this.centroid.setZ(this.centroid.getZ() / numSceneObjects);
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

    public Point3D getCentroid() {
        return centroid;
    }
}
