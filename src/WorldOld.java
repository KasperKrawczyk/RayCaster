public class WorldOld {

    public static final Vector3D ORIGIN = new Vector3D(0, 0, 0);
    public static final Vector3D DATASET_CENTRE = new Vector3D(
            256 / 2.0,
            113 / 2.0,
            256 / 2.0);
    public static final int VIEW_PLANE_HEIGHT = 300;
    public static final int VIEW_PLANE_WIDTH = 400;
    public static final int viewPlaneEyeDistance = 50;
    public static Vector3D circleCentre;
    public static Vector3D viewPlaneCorner0; //start of the matrix, floor level
    public static Vector3D viewPlaneCornerFloor1; //end of the matrix, floor level
    public static Vector3D viewPlaneCornerUp0; //start of the matrix
    public static Vector3D viewPlaneCornerUp1; //end of the matrix
    public static Vector3D viewPlaneCentre;
    public static Vector3D eye; // immediately before viewPlaneCentre along the circleCentre <> viewPlaneCentre axis
    public static Vector3D viewPlaneCentreFloor;
    public static Vector3D viewPlaneNormal;
    public static Vector3D light;
    public static double circleRadius;
    public static double viewPlaneAngle;

    public static void initWorld() {
        initCircleCentre();
        initViewPlaneCentre();
        initViewPlaneCentreFloor();
        initViewPlaneCorner();

        initLight();
        initViewPlaneNormal();
        initCircleRadius();

    }

    private static void initCircleCentre() {
        double x = Main.getDatasetWidth() / 2.0;
        double z = Main.getDatasetSize() / 2.0; //or dataset width
        double y = Main.getDatasetHeight() / 2.0;
        circleCentre = new Vector3D(x, 0, z);
    }

    private static void initViewPlaneCorner() {
        viewPlaneCorner0 = new Vector3D(viewPlaneCentreFloor);
        viewPlaneCorner0.setX(VIEW_PLANE_WIDTH / 2.0);
    }

    private static void initViewPlaneCentre() {
        //along the x axis, the view plane will be centred on the centre of the dataset
        double x = Main.getDatasetWidth() / 2.0;
        double y = VIEW_PLANE_HEIGHT / 2.0;
        viewPlaneCentre = new Vector3D(x, y, 0);
    }

    private static void initEye() {
        eye = new Vector3D(viewPlaneCentre);
        eye.setZ(eye.getZ() - viewPlaneEyeDistance);
    }

    private static void initLight() {
        light = new Vector3D(65.5, 40, -200);
    }

    private static void initViewPlaneCentreFloor() {
        viewPlaneCentreFloor = new Vector3D(viewPlaneCentre);
        viewPlaneCentreFloor.setY(0);
    }

    private static void initCircleRadius() {
        circleRadius = circleCentre.sub(viewPlaneCentreFloor).magnitude();
    }

    private static void initViewPlaneNormal() {
        viewPlaneNormal = circleCentre.sub(viewPlaneCentreFloor).normalize();
    }

    public static void moveLightByVector(Vector3D moveBy) {
        light.add(moveBy);
    }

    /**
     * Moves the view plane counter-clockwise by the specified angle in degrees.
     * This includes the <code>viewPlaneCorner</code>,
     * the <code>viewPlaneCentre</code>,
     * the <code>viewPlaneCentreFloor</code>,
     * and the <code>viewPlaneNormal</code>.
     * @param degrees the specified angle in degrees
     */
    public static void moveViewPlaneByAngleDegrees(double degrees) {
        //first, the centre floor point of the view plane is calculated
        viewPlaneCentreFloor = Quaternion.newRotator(degrees, Axis.Y.getVector()).rotate(viewPlaneCentreFloor, circleCentre);

        System.out.println("circleCentre = " + circleCentre);

        //then, the VP corners 0 and 1, and the VP centre can be derived therefrom
        viewPlaneNormal = circleCentre.sub(viewPlaneCentreFloor).normalize();

        updateViewPlaneCorners();

        viewPlaneCentre = viewPlaneCentreFloor.add(new Vector3D(0, VIEW_PLANE_HEIGHT / 2.0, 0));
        updateEye();

//        System.out.println("circleCentre " + circleCentre);
//        System.out.println("viewPlaneCentreFloor " + viewPlaneCentreFloor);
//        System.out.println("viewPlaneCorner0 " + viewPlaneCorner0);
//        System.out.println("viewPlaneCorner1 " + viewPlaneCorner1);
//        System.out.println("viewPlaneCentre " + viewPlaneCentre);
//        System.out.println("viewPlaneNormal " + viewPlaneNormal);

    }


    /**
     * Moves the view plane counter-clockwise by the specified angle in degrees.
     * This includes the <code>viewPlaneCorner</code>,
     * the <code>viewPlaneCentre</code>,
     * the <code>viewPlaneCentreFloor</code>,
     * and the <code>viewPlaneNormal</code>.
     * @param rotator the Quaternion to rotate <code>viewPlaneCentreFloor</code> by
     */
    public static void moveViewPlaneByRotator(Quaternion rotator) {
        //first, the centre floor point of the view plane is calculated
        viewPlaneCentreFloor = rotator.rotate(WorldOld.getViewPlaneNormal(), circleCentre);

        //then, the VP corners 0 and 1, and the VP centre can be derived therefrom
        viewPlaneNormal = circleCentre.sub(viewPlaneCentreFloor).normalize();

        updateViewPlaneCorners(); // FIXME: 05/06/2022 we need the upper corners as well

        viewPlaneCentre = viewPlaneCentreFloor.add(new Vector3D(0, VIEW_PLANE_HEIGHT / 2.0, 0));
        updateEye();

//        System.out.println("circleCentre " + circleCentre);
//        System.out.println("viewPlaneCentreFloor " + viewPlaneCentreFloor);
//        System.out.println("viewPlaneCorner0 " + viewPlaneCorner0);
//        System.out.println("viewPlaneCorner1 " + viewPlaneCorner1);
//        System.out.println("viewPlaneCentre " + viewPlaneCentre);
//        System.out.println("viewPlaneNormal " + viewPlaneNormal);

    }

    /**
     * Returns a vector by which to move the origin of ray
     * for iteration over the view plane matrix.
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next.
     */
    public static Vector3D getStep() {
        return new Vector3D(
                (viewPlaneCornerFloor1.getX() - viewPlaneCorner0.getX()) / VIEW_PLANE_WIDTH,
                0,
                (viewPlaneCornerFloor1.getZ() - viewPlaneCorner0.getZ()) / VIEW_PLANE_WIDTH
        );
    }



    /**
     * Returns the locations of the two floor-level corners of the view plane,
     * Abstractly, the two points are ends of the segment tangential to the
     * circle at the point <code>viewPlaneCentreFloor</code>.
     */
    private static void updateViewPlaneCorners() {
        double slopeRadius = (WorldOld.circleCentre.getX() - WorldOld.viewPlaneCentreFloor.getX()) /
                (WorldOld.circleCentre.getZ() - WorldOld.viewPlaneCentreFloor.getZ());

        double slopeHalfside = -1 / slopeRadius;

        Vector3D b = new Vector3D(
                slopeHalfside / (Math.sqrt(1 + Math.pow(slopeHalfside, 2))),
                0,
                1 / (Math.sqrt(1 + Math.pow(slopeHalfside, 2)))
        );

        viewPlaneCorner0 = viewPlaneCentreFloor.sub(b.mult(VIEW_PLANE_WIDTH / 2.0));
        viewPlaneCornerFloor1 = viewPlaneCentreFloor.add(b.mult(VIEW_PLANE_WIDTH / 2.0));
    }


    private static void updateEye() {
        Vector3D v = circleCentre.sub(viewPlaneCentreFloor).flip().normalize();
        v = v.mult(viewPlaneEyeDistance);
        eye = new Vector3D(viewPlaneCentre).add(v);
    }

    public static Vector3D getCircleCentre() {
        return circleCentre;
    }

    public static Vector3D getViewPlaneCorner0() {
        return viewPlaneCorner0;
    }

    public static Vector3D getViewPlaneCentre() {
        return viewPlaneCentre;
    }

    public static Vector3D getViewPlaneCentreFloor() {
        return viewPlaneCentreFloor;
    }

    public static Vector3D getViewPlaneNormal() {
        return viewPlaneNormal;
    }

    public static Vector3D getLight() {
        return light;
    }

    public static double getCircleRadius() {
        return circleRadius;
    }

    public static double getViewPlaneAngle() {
        return viewPlaneAngle;
    }

    public static void setViewPlaneAngle(double viewPlaneAngle) {
        WorldOld.viewPlaneAngle = viewPlaneAngle;
    }

    public static Vector3D getViewPlaneCornerFloor1() {
        return viewPlaneCornerFloor1;
    }

    public static Vector3D getEye() {
        return eye;
    }
}
