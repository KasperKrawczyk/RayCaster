public class World2 {

    public static final Vector3D ORIGIN = new Vector3D(0, 0, 0);
    public static final Vector3D DATASET_CENTRE = new Vector3D(
            256 / 2.0,
            113 / 2.0,
            256 / 2.0);
    public static final int VIEW_PLANE_HEIGHT = 300;
    public static final int VIEW_PLANE_WIDTH = 400;
    public static final int viewPortEyeDistance = 50;
    public static Vector3D circleCentre;
    public static Vector3D viewPortCorner0; //start of the matrix, last row
    public static Vector3D viewPortCorner1; //end of the matrix, last row
    public static Vector3D viewPortCorner2; //start of the matrix, first row
    public static Vector3D viewPortCorner3; //end of the matrix, first row
    public static Vector3D viewPortCentre;
    public static Vector3D eye; // immediately before viewPortCentre along the circleCentre <> viewPortCentre axis
    public static Vector3D viewPortCentreFloor;
    public static Vector3D viewPortCentreCeil;
    public static Vector3D viewPortNormal;
    public static Vector3D light;
    public static double circleRadius;
    public static double viewPortAngle;

    public static void initWorld2() {
        initCircleCentre();
        initViewPortCentre();
        initViewPortCentreCeil();
        initViewPortCentreFloor();
        initViewPortNormal();
        initViewPortCorners();

        initLight();
        System.out.println("viewPortNormal = " + viewPortNormal);
        initCircleRadius();


        System.out.println("----------WORLD INIT----------");
        System.out.println("viewPortCentre = " + viewPortCentre);
        System.out.println("viewPortNormal = " + viewPortNormal);
        System.out.println("viewPortCorner0 = " + viewPortCorner0);
        System.out.println("viewPortCorner1 = " + viewPortCorner1);
        System.out.println("viewPortCorner2 = " + viewPortCorner2);
        System.out.println("viewPortCorner3 = " + viewPortCorner3);

    }

    private static void initCircleCentre() {
        double x = Main.getDatasetWidth() / 2.0;
        double z = Main.getDatasetSize() / 2.0; //or dataset width
        double y = Main.getDatasetHeight() / 2.0;
        circleCentre = new Vector3D(x, y, z);
    }

    private static void initViewPortCentre() {
        //along the x axis, the view plane will be centred on the centre of the dataset
        double x = Main.getDatasetWidth() / 2.0;
        double y = Main.getDatasetHeight() / 2.0;
        viewPortCentre = new Vector3D(x, y, 0);
    }

    private static void initViewPortCorners() {
//        viewPortCorner0 = new Vector3D(viewPortCentreCeil);
//        viewPortCorner1 = new Vector3D(viewPortCentreCeil);
//        viewPortCorner2 = new Vector3D(viewPortCentreFloor);
//        viewPortCorner3 = new Vector3D(viewPortCentreFloor);
//        viewPortCorner0.setX(viewPortCentreFloor.getX() - (VIEW_PLANE_WIDTH / 2f));
//        viewPortCorner1.setX(viewPortCentreFloor.getX() + (VIEW_PLANE_WIDTH / 2f));
//        viewPortCorner2.setX(viewPortCentreFloor.getX() - (VIEW_PLANE_WIDTH / 2f));
//        viewPortCorner3.setX(viewPortCentreFloor.getX() + (VIEW_PLANE_WIDTH / 2f));

        Vector3D normal = viewPortNormal;
        //view plane equation
        //normal.x, normal.y, normal.z,
        // - normal.x*viewPortCentre.x - normal.y*viewPortCentre.y - normal.z*viewPortCentre.z
        Vector3D horizontal = Axis.Y.getVector().crossProd(normal).normalize(); //horizontal axis of the viewport
        System.out.println("horizontal = " + horizontal);
        Vector3D vertical = normal.crossProd(horizontal).normalize(); //vertical axis of the viewport
        System.out.println("vertical = " + vertical);
        //
        //0           1
        //
        //2           3
        //
        viewPortCorner0 = viewPortCentre.add(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).sub(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPortCorner1 = viewPortCentre.add(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).add(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPortCorner2 = viewPortCentre.sub(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).sub(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPortCorner3 = viewPortCentre.sub(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).add(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));

    }



    private static void initEye() {
        eye = new Vector3D(viewPortCentre);
        eye.setZ(eye.getZ() - viewPortEyeDistance);
    }

    private static void initLight() {
        light = new Vector3D(65.5, 40, -200);
    }

    private static void initViewPortCentreFloor() {
        viewPortCentreFloor = new Vector3D(viewPortCentre);
        viewPortCentreFloor.setY(0);
    }

    private static void initViewPortCentreCeil() {
        viewPortCentreCeil = new Vector3D(viewPortCentre);
        viewPortCentreCeil.setY(VIEW_PLANE_HEIGHT);
    }

    private static void initCircleRadius() {
        circleRadius = DATASET_CENTRE.sub(viewPortCentre).magnitude();
    }

    private static void initViewPortNormal() {
        viewPortNormal = circleCentre.sub(viewPortCentre).normalize();
    }

    public static void moveLightByVector(Vector3D moveBy) {
        light.add(moveBy);
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
        viewPortCentre = rotator.rotate(World2.getViewPortNormal(), DATASET_CENTRE);
        viewPortNormal = DATASET_CENTRE.sub(viewPortCentre).normalize();


        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        viewPortNormal = DATASET_CENTRE.sub(viewPortCentre).normalize();

        updateViewPortCorners();

        updateEye();

//        System.out.println("----------WORLD UPDATE----------");
//        System.out.println("viewPortCentre = " + viewPortCentre);
//        System.out.println("viewPortNormal = " + viewPortNormal);
//        System.out.println("viewPortCorner0 = " + viewPortCorner0);
//        System.out.println("viewPortCorner1 = " + viewPortCorner1);
//        System.out.println("viewPortCorner2 = " + viewPortCorner2);
//        System.out.println("viewPortCorner3 = " + viewPortCorner3);

    }

    public static void moveViewPlaneByAngleDegrees(double degrees) {
        //first, the centre floor point of the view plane is calculated
        viewPortCentre = Quaternion.makeExactQuaternionDegrees(degrees, Axis.Y.getVector()).rotate(viewPortCentre, circleCentre);

        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        viewPortNormal = DATASET_CENTRE.sub(viewPortCentre).normalize();

        updateViewPortCorners();

        updateEye();

//        System.out.println("----------WORLD UPDATE----------");
//        System.out.println("viewPortCentre = " + viewPortCentre);
//        System.out.println("viewPortNormal = " + viewPortNormal);
//        System.out.println("viewPortCorner0 = " + viewPortCorner0);
//        System.out.println("viewPortCorner1 = " + viewPortCorner1);
//        System.out.println("viewPortCorner2 = " + viewPortCorner2);
//        System.out.println("viewPortCorner3 = " + viewPortCorner3);

    }

    /**
     * Returns a vector by which to move the origin of ray
     * when iterating over the view port matrix along a row
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next on the same row
     */
    public static Vector3D getStepX() {
        return viewPortCorner3.sub(viewPortCorner2).div(VIEW_PLANE_WIDTH);
    }

    /**
     * Returns a vector by which to move the origin of ray when changing rows
     * while iterating over the view port matrix
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next on the next row
     */
    public static Vector3D getStepY() {
        return viewPortCorner2.sub(viewPortCorner0).div(VIEW_PLANE_HEIGHT);
    }

    /**
     * Returns the locations of the two floor-level corners of the view plane,
     * Abstractly, the two points are ends of the segment tangential to the
     * circle at the point <code>viewPortCentreFloor</code>.
     */
    private static void updateViewPortCorners() {

        Vector3D normal = viewPortNormal;
        //view plane equation
        //normal.x, normal.y, normal.z,
        // - normal.x*viewPortCentre.x - normal.y*viewPortCentre.y - normal.z*viewPortCentre.z
        Vector3D horizontal = Axis.Y.getVector().crossProd(normal).normalize(); //horizontal axis of the viewport
//        System.out.println("horizontal = " + horizontal);
        Vector3D vertical = normal.crossProd(horizontal).normalize(); //vertical axis of the viewport
//        System.out.println("vertical = " + vertical);
        //
        //0           1
        //
        //2           3
        //
        viewPortCorner0 = viewPortCentre.add(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).sub(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPortCorner1 = viewPortCentre.add(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).add(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPortCorner2 = viewPortCentre.sub(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).sub(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPortCorner3 = viewPortCentre.sub(vertical.mult(VIEW_PLANE_HEIGHT / 2.0)).add(horizontal.mult(VIEW_PLANE_WIDTH / 2.0));
    }


    private static void updateEye() {
        Vector3D v = circleCentre.sub(viewPortCentreFloor).flip().normalize();
        v = v.mult(viewPortEyeDistance);
        eye = new Vector3D(viewPortCentre).add(v);
    }

    public static Vector3D getLight() {
        return light;
    }

    public static Vector3D getViewPortNormal() {
        return viewPortNormal;
    }

    public static void setViewPortAngle(double viewPortAngle) {
        World2.viewPortAngle = viewPortAngle;
    }

    public static Vector3D getViewPortCorner2() {
        return viewPortCorner2;
    }

    public static Vector3D getViewPortCorner0() {
        return viewPortCorner0;
    }

    public static Vector3D getEye() {
        return eye;
    }
}
