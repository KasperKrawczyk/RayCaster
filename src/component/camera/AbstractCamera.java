package component.camera;

import model.Axis;
import model.Point3D;
import model.Quaternion;
import model.Vector3D;

public abstract class AbstractCamera {

    public static final Vector3D ORIGIN = new Vector3D(0, 0, 0);
    public static final int VIEW_PLANE_HEIGHT = 600;
    public static final int VIEW_PLANE_WIDTH = 600;
    public static final int VIEW_PORT_DATASET_CENTRE_DISTANCE = 0;
    public static final int VIEW_PORT_EYE_DISTANCE_MULT = (int) (VIEW_PLANE_WIDTH / 1.38);
    public static final float WIDTH_HEIGHT_RESOLUTION = (float) VIEW_PLANE_WIDTH / (float) VIEW_PLANE_HEIGHT;
    public static final int DEFAULT_VIEWPORT_LOOK_AT_CENTRE_DISTANCE_MULTIPLIER = 100;

    protected Vector3D lookAtCentre = ORIGIN;

    public Vector3D viewPortCentre;
    public Vector3D viewPortCorner0; //start of the matrix, first row
    public Vector3D viewPortCorner1; //end of the matrix, first row
    public Vector3D viewPortCorner2; //start of the matrix, last row
    public Vector3D viewPortCorner3; //end of the matrix, last row
    public Vector3D eye; // behind viewPortCentre along the DATASET_CENTRE <> viewPortCentre axis
    public Vector3D viewPortNormal;
    public Vector3D light;
    public double circleRadius;
    public double viewPortAngle;

    public int viewPortDatasetCentreDistanceMultiplier = DEFAULT_VIEWPORT_LOOK_AT_CENTRE_DISTANCE_MULTIPLIER;


    public AbstractCamera(Point3D viewPortCentre) {
        initCamera(viewPortCentre);
    }

    protected AbstractCamera() {
    }

    public void initCamera(Point3D viewPortCentre) {
        initViewPortCentre(viewPortCentre);
        initViewPortNormal();
        updateViewPortCorners();
        initLight();
        initCircleRadius();
        initEye();

    }

    private void initViewPortCentre(Point3D viewPortCentre) {
        this.viewPortCentre = new Vector3D(viewPortCentre);
    }


    private void initEye() {
        eye = viewPortCentre
                .add(lookAtCentre
                        .sub(viewPortCentre)
                        .normalize()
                        .flip()
                        .mult(VIEW_PORT_EYE_DISTANCE_MULT)
                );
    }

    private void initLight() {
        light = new Vector3D(65.5, 40, -200);
    }


    private void initCircleRadius() {
        circleRadius = lookAtCentre.sub(viewPortCentre).magnitude();
    }

    private void initViewPortNormal() {
        viewPortNormal = lookAtCentre.sub(viewPortCentre).normalize();
    }

    public void moveLightByVector(Vector3D moveBy) {
        light.add(moveBy);
    }

    public  void moveLightTo(Point3D newLightLocation) {
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
     * @param rotator the model.Quaternion to rotate <code>viewPortCentreFloor</code> by
     */
    public void moveViewPortByRotator(Quaternion rotator) {
        //first, the centre point of the view plane is calculated
        viewPortCentre = rotator.rotate(viewPortNormal, lookAtCentre);
        viewPortNormal = lookAtCentre.sub(viewPortCentre).normalize();
        viewPortCentre = lookAtCentre
                .newMovedByVector(viewPortNormal
                        .flip()
                        .mult(DEFAULT_VIEWPORT_LOOK_AT_CENTRE_DISTANCE_MULTIPLIER)
                );
        viewPortCentre.moveThisByVector(viewPortNormal.mult(viewPortDatasetCentreDistanceMultiplier));
        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        updateViewPortCorners();
        //lastly, we can move the camera lense to position it along its dataset centre <> viewport centre axis,
        //behind the viewport
        updateEye();
    }

    public void updateViewPort(int newDistanceMultiplier) {
        setViewPortDatasetCentreDistanceMultiplier(newDistanceMultiplier);
        //first, the new centre point of the view plane is calculated
        viewPortCentre = lookAtCentre.newMovedByVector(viewPortNormal.flip().mult(DEFAULT_VIEWPORT_LOOK_AT_CENTRE_DISTANCE_MULTIPLIER));
        viewPortCentre.moveThisByVector(viewPortNormal.mult(viewPortDatasetCentreDistanceMultiplier));

        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        updateViewPortCorners();
        //lastly, we can move the camera lense to position it along its dataset centre <> viewport centre axis,
        //behind the viewport
        updateEye();
    }

    public void moveViewPortByAngleDegrees(double degrees) {
        //first, the centre floor point of the view plane is calculated
        viewPortCentre = Quaternion.makeExactQuaternionDegrees(degrees, Axis.Y.getVector())
                .rotate(viewPortCentre, lookAtCentre);
        viewPortNormal = lookAtCentre.sub(viewPortCentre).normalize();

        //then, the VP corners 0, 1, 2, and 3 can be derived therefrom
        updateViewPortCorners();
        //lastly, we can move the camera lense to position it along its dataset centre <> viewport centre axis,
        //behind the viewport
        updateEye();

    }



    /**
     * Returns the locations of the two floor-level corners of the view plane,
     * Abstractly, the two points are ends of the segment tangential to the
     * circle at the point <code>viewPortCentreFloor</code>.
     */
    private void updateViewPortCorners() {

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

    private void printCameraUpdate() {
        System.out.println("----------WORLD UPDATE----------");
        System.out.println("viewPortCentre = " + viewPortCentre);
        System.out.println("viewPortNormal = " + viewPortNormal);
        System.out.println("viewPortCorner0 = " + viewPortCorner0);
        System.out.println("viewPortCorner1 = " + viewPortCorner1);
        System.out.println("viewPortCorner2 = " + viewPortCorner2);
        System.out.println("viewPortCorner3 = " + viewPortCorner3);
    }

    private void updateEye() {
        Vector3D v = viewPortNormal.flip().mult(VIEW_PORT_EYE_DISTANCE_MULT);
        eye = new Vector3D(viewPortCentre);
        eye.moveThisByVector(v);
    }

    /**
     * Returns a vector by which to move the origin of ray
     * when iterating over the view port matrix along a row
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next on the same row
     */
    public Vector3D getStepX() {
        return viewPortCorner1.sub(viewPortCorner0).normalize();
    }

    /**
     * Returns a vector by which to move the origin of ray when changing rows
     * while iterating over the view port matrix
     * @return a vector indicating the direction and magnitude of transition
     * from a pixel to the next on the next row
     */
    public Vector3D getStepY() {
        return viewPortCorner2.sub(viewPortCorner0).normalize();
    }

    public Vector3D getLight() {
        return light;
    }

    public Vector3D getViewPortNormal() {
        return viewPortNormal;
    }

    public  void setViewPortAngle(double viewPortAngle) {
        this.viewPortAngle = viewPortAngle;
    }

    public Vector3D getViewPortCorner0() {
        return viewPortCorner0;
    }

    public Vector3D getViewPortCorner1() {
        return viewPortCorner1;
    }

    public Vector3D getViewPortCorner2() {
        return viewPortCorner2;
    }

    public Vector3D getViewPortCorner3() {
        return viewPortCorner3;
    }

    public Vector3D getEye() {
        return eye;
    }

    public int getViewPortDatasetCentreDistanceMultiplier() {
        return viewPortDatasetCentreDistanceMultiplier;
    }

    public void setViewPortDatasetCentreDistanceMultiplier(int viewPortDatasetCentreDistanceMultiplier) {
        this.viewPortDatasetCentreDistanceMultiplier = viewPortDatasetCentreDistanceMultiplier;
    }
}
