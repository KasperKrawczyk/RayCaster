public class Camera {

    public static final Vector3D ORIGIN = new Vector3D(0, 0, 0);
    public static final Vector3D DATASET_CENTRE = new Vector3D(256 * 0.5, 113 * 0.5, 256 * 0.5);
    public static final int VIEW_PLANE_HEIGHT = 600;
    public static final int VIEW_PLANE_WIDTH = 600;
    public static final int VIEW_PORT_DATASET_CENTRE_DISTANCE = 0;
    public static final int VIEW_PORT_EYE_DISTANCE_MULT = (int) (VIEW_PLANE_WIDTH / 1.38);
    public static final float WIDTH_HEIGHT_RESOLUTION = (float) VIEW_PLANE_WIDTH / (float) VIEW_PLANE_HEIGHT;
    public static Vector3D viewPortCentre;
    public static Vector3D viewPortCorner0; //start of the matrix, first row
    public static Vector3D viewPortCorner1; //end of the matrix, first row
    public static Vector3D viewPortCorner2; //start of the matrix, last row
    public static Vector3D viewPortCorner3; //end of the matrix, last row
    public static Vector3D eye; // behind viewPortCentre along the DATASET_CENTRE <> viewPortCentre axis
    public static Vector3D viewPortNormal;
    public static Vector3D light;
    public static double circleRadius;
    public static double viewPortAngle;

    public static void initCamera() {
        initViewPortCentre();
        initViewPortNormal();
        updateViewPortCorners();
        initLight();
        initCircleRadius();
        initEye();

    }

    private static void initViewPortCentre() {
        //along the x-axis, the view plane will be centred on the centre of the dataset
        double x = Main.getDatasetWidth() / 2.0;
        double y = Main.getDatasetSize() / 2.0;
        viewPortCentre = new Vector3D(x, y, VIEW_PORT_DATASET_CENTRE_DISTANCE);
    }


    private static void initEye() {
        eye = viewPortCentre
                .add(DATASET_CENTRE
                        .sub(viewPortCentre)
                        .normalize()
                        .flip()
                        .mult(VIEW_PORT_DATASET_CENTRE_DISTANCE)
                );
    }

    private static void initLight() {
        light = new Vector3D(65.5, 40, -200);
    }


    private static void initCircleRadius() {
        circleRadius = DATASET_CENTRE.sub(viewPortCentre).magnitude();
    }

    private static void initViewPortNormal() {
        viewPortNormal = DATASET_CENTRE.sub(viewPortCentre).normalize();
    }

    public static void moveLightByVector(Vector3D moveBy) {
        light.add(moveBy);
    }

    public static void moveLightTo(Point3D newLightLocation) {
        light = new Vector3D(
                newLightLocation.getX(),
                newLightLocation.getY(),
                newLightLocation.getZ()
        );
    }

    /**
     * Moves the view plane counter-clockwise by the specified angle in degrees.
     * This includes the <code>viewPortCorner</code>,
     * the <code>viewPortCentre</code>,
     * the <code>viewPortCentreFloor</code>,
     * and the <code>viewPortNormal</code>.
     * @param rotator the Quaternion to rotate <code>viewPortCentreFloor</code> by
     */
    public static void moveViewPortByRotator(Quaternion rotator) {
        //first, the centre point of the view plane is calculated
        viewPortCentre = rotator.rotate(viewPortNormal, DATASET_CENTRE);
        viewPortNormal = DATASET_CENTRE.sub(viewPortCentre).normalize();
//        System.out.println("viewPortCentre = " + viewPortCentre);
//        System.out.println("viewPortNormal = " + viewPortNormal);

        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        updateViewPortCorners();

        updateEye();


//        System.out.println("VPCentre to DTSCentre");
//        System.out.println(viewPortCentre.sub(DATASET_CENTRE).magnitude());


    }

    public static void moveViewPortByAngleDegrees(double degrees) {
        //first, the centre floor point of the view plane is calculated
        viewPortCentre = Quaternion.makeExactQuaternionDegrees(degrees, Axis.Y.getVector())
                .rotate(viewPortCentre, DATASET_CENTRE);

        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        viewPortNormal = DATASET_CENTRE.sub(viewPortCentre).normalize();

        updateViewPortCorners();

        updateEye();

    }



    /**
     * Returns the locations of the two floor-level corners of the view plane,
     * Abstractly, the two points are ends of the segment tangential to the
     * circle at the point <code>viewPortCentreFloor</code>.
     */
    private static void updateViewPortCorners() {

        //view plane equation
        //normal.x, normal.y, normal.z,
        // - normal.x*viewPortCentre.x - normal.y*viewPortCentre.y - normal.z*viewPortCentre.z
        Vector3D horizontal = Axis.Y.getVector().crossProd(viewPortNormal).normalize(); //horizontal axis of the viewport
        Vector3D vertical = viewPortNormal.crossProd(horizontal).normalize(); //vertical axis of the viewport
        float halfHeight = (float) (VIEW_PLANE_HEIGHT * 0.5);
        float halfWidth = (float) (VIEW_PLANE_WIDTH * 0.5);
        Vector3D halfVertical = vertical.mult(halfHeight);
        Vector3D halfHorizontal = horizontal.mult(halfWidth);
        //
        //0           1
        //
        //2           3
        //
        viewPortCorner0 = viewPortCentre.add(halfVertical).sub(halfHorizontal);
        viewPortCorner1 = viewPortCentre.add(halfVertical).add(halfHorizontal);
        viewPortCorner2 = viewPortCentre.sub(halfVertical).sub(halfHorizontal);
        viewPortCorner3 = viewPortCentre.sub(halfVertical).add(halfHorizontal);


    }

    private static void printCameraUpdate() {
        System.out.println("----------WORLD UPDATE----------");
        System.out.println("viewPortCentre = " + viewPortCentre);
        System.out.println("viewPortNormal = " + viewPortNormal);
        System.out.println("viewPortCorner0 = " + viewPortCorner0);
        System.out.println("viewPortCorner1 = " + viewPortCorner1);
        System.out.println("viewPortCorner2 = " + viewPortCorner2);
        System.out.println("viewPortCorner3 = " + viewPortCorner3);
    }

    private static void updateEye() {
        Vector3D v = viewPortNormal.flip().mult(VIEW_PORT_EYE_DISTANCE_MULT);
        eye = new Vector3D(viewPortCentre);
        eye.moveByVector(v);
    }

    /**
     * Returns a vector by which to move the origin of ray
     * when iterating over the view port matrix along a row
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next on the same row
     */
    public static Vector3D getStepX() {
        return viewPortCorner1.sub(viewPortCorner0).normalize();
    }

    /**
     * Returns a vector by which to move the origin of ray when changing rows
     * while iterating over the view port matrix
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next on the next row
     */
    public static Vector3D getStepY() {
        return viewPortCorner2.sub(viewPortCorner0).normalize();
    }

    public static Vector3D getLight() {
        return light;
    }

    public static Vector3D getViewPortNormal() {
        return viewPortNormal;
    }

    public static void setViewPortAngle(double viewPortAngle) {
        Camera.viewPortAngle = viewPortAngle;
    }

    public static Vector3D getViewPortCorner0() {
        return viewPortCorner0;
    }

    public static Vector3D getViewPortCorner1() {
        return viewPortCorner1;
    }

    public static Vector3D getViewPortCorner2() {
        return viewPortCorner2;
    }

    public static Vector3D getViewPortCorner3() {
        return viewPortCorner3;
    }

    public static Vector3D getEye() {
        return eye;
    }
}
