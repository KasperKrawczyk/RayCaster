package component.camera;

import model.Point3D;

public class SceneCamera extends AbstractCamera {

    public SceneCamera(Point3D viewPortCentre) {
        super(viewPortCentre);
    }

    public SceneCamera() {
        super(new Point3D(0, 0, 0));
    }
}
