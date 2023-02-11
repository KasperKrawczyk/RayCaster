package component.camera;

import config.IConfig;
import model.Point3D;
import model.Vector3D;

public class SceneCamera extends AbstractCamera {

    public SceneCamera(IConfig config) {
        super(config);
    }
    public SceneCamera(IConfig config, Point3D centroid) {
        super(config);
        this.lookAtCentre = new Vector3D(centroid);
    }


}
