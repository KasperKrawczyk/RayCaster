package model;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;

public class TestSceneBuilder {

    public static final HashMap<Short, Color> HU_TO_COLOR_MAP = new HashMap<Short, Color>() {{
        put((short) -99, Color.WHITE);
        put((short) 299, Color.color(1, 0.79, 0.6));
        put((short) 1900, Color.color(0.8902, 0.8549, 0.7882));
        put(Short.MAX_VALUE, Color.WHITE);
    }};

    public static Scene buildScene(short[][][] vol) {
        AABB aabb1 = new AABB(new Vector3D(0, 0, 0), new Vector3D(255, 225, 255));
        AABB aabb2 = new AABB(new Vector3D(400, 400, 400), new Vector3D(655, 625, 655));

        SceneObject so1 = new SceneObject(aabb1, vol, HU_TO_COLOR_MAP);
        SceneObject so2 = new SceneObject(aabb2, vol, HU_TO_COLOR_MAP);

        ArrayList<SceneObject> sceneObjects = new ArrayList<>();

        Scene scene = new Scene(new Vector3D(0, 1000, 0), sceneObjects);

        return scene;
    }

}
