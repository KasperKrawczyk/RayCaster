import javafx.scene.paint.Color;

public class Reflections {

    private Reflections() {
        throw new UnsupportedOperationException(Util.INSTANTIATION_ERR_MSG);
    }

    public static Color applyReflectionModel(Vector3D light, Vector3D eye, Vector3D intersectionPoint,
                                             float shininessCoeff, float reflectionConstant,
                                             Vector3D gradient, Color intersectionPointColor) {
        Vector3D n = gradient.normalize();
        Vector3D toLight = light.sub(intersectionPoint);
        Vector3D fromLight = intersectionPoint.sub(light);
        Vector3D l = toLight.normalize();
        Vector3D toEye = eye.sub(intersectionPoint);
        Vector3D e = toEye.normalize();
        Vector3D r = fromLight.sub((n.mult(2 * fromLight.dotProd(n))));
        double cosTheta = gradient.cos(l);
        double cosFi = r.cos(e);
        float phongMultiplier = (float) (Math.pow(cosFi, shininessCoeff) * reflectionConstant);
        double red;
        double green;
        double blue;
        double opacity;

        red = Math.min(1, (intersectionPointColor.getRed() * cosTheta) +
                        (intersectionPointColor.getRed() * phongMultiplier));
        green = Math.min(1, (intersectionPointColor.getGreen() * cosTheta) +
                        (intersectionPointColor.getGreen() * phongMultiplier));
        blue = Math.min(1, (intersectionPointColor.getBlue() * cosTheta) +
                        (intersectionPointColor.getBlue() * phongMultiplier));

//        red = intersectionPointColor.getRed() * cosTheta;
//        green = intersectionPointColor.getGreen() * cosTheta;
//        blue = intersectionPointColor.getBlue() * cosTheta;
        opacity = DataSet.getOpacityLUT()[(int) gradient.magnitude()];

        return new Color(red, green, blue, opacity);

    }

    public static Color applyLambertianReflection(Vector3D light, Vector3D intersectionPoint,
                                                  Vector3D gradient, Color intersectionPointColor) {
        Vector3D toLight = light.sub(intersectionPoint);
        double cosTheta = gradient.cos(toLight);
        double red;
        double green;
        double blue;
        double opacity;

        red = Math.min(1, intersectionPointColor.getRed() * cosTheta);
        green = Math.min(1, intersectionPointColor.getGreen() * cosTheta);
        blue = Math.min(1, intersectionPointColor.getBlue() * cosTheta);

//        red = intersectionPointColor.getRed() * cosTheta;
//        green = intersectionPointColor.getGreen() * cosTheta;
//        blue = intersectionPointColor.getBlue() * cosTheta;
        //opacity = DataSet.getOpacityLUT()[(int) gradient.magnitude()];

        return new Color(red, green, blue, intersectionPointColor.getOpacity());

    }
}
