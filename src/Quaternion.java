public class Quaternion {

//    public static final Quaternion X = new Quaternion(0, new Vector3D(1, 0, 0));
//    public static final Quaternion Y = new Quaternion(0, new Vector3D(0, 1, 0));
//    public static final Quaternion Z = new Quaternion(0, new Vector3D(0, 0, 1));

    private Vector3D vector;
    private double w;

    public Quaternion() {}

    public Quaternion(double angle, Vector3D v) {
        double alpha = Math.toRadians(angle);
        this.w = Math.cos(alpha / 2.0);
        this.vector = v.mult(Math.sin(alpha / 2.0));

    }

    /**
     *
     * @param radians in radians
     * @param vector vector to set for the imaginary part of the new quaternion
     * @return a new Quaternion object with the exact parameters
     */
    public static Quaternion makeExactQuaternionRadians(double radians, Vector3D vector) {
        Quaternion q = new Quaternion();
        q.setW(radians);
        q.setVector(vector);
        return q;
    }

    /**
     *
     * @param degrees
     * @param vector vector to set for the imaginary part of the new quaternion
     * @return a new Quaternion object with the exact parameters
     */
    public static Quaternion makeExactQuaternionDegrees(double degrees, Vector3D vector) {
        Quaternion q = new Quaternion();
        q.setW(Math.toRadians(degrees));
        q.setVector(vector);
        return q;
    }

    public static Quaternion newRotator(double angle, Vector3D vector) {
        return new Quaternion(angle, vector);
    }

    public Quaternion concatenate(double angle, Axis axis) {
        return this.mult(Quaternion.makeExactQuaternionRadians(angle, axis.getVector()));
    }

    @Override
    public String toString() {
        return "Quaternion{" +
                "w=" + w +
                ", vector=" + vector +
                '}';
    }

    /**
     * Quaternion multiplication by another quaternion combines their angles of rotation
     * @param other the other quaternion to multiply this quaternion by
     * @return a new Quaternion, the product of the quaternion-by-quaternion multiplication
     */
    public Quaternion mult(Quaternion other) {
        Vector3D thisVector = this.getVector();
        Vector3D otherVector = other.getVector();

        // Components of this quaternion.
        double thisW = this.getW();
        double thisX = thisVector.getX();
        double thisY = thisVector.getY();
        double thisZ = thisVector.getZ();

        // Components of the other quaternion.
        double otherW = other.getW();
        double otherX = otherVector.getX();
        double otherY = otherVector.getY();
        double otherZ = otherVector.getZ();

        // Components of the product.
        double w = thisW * otherW - thisX * otherX - thisY * otherY - thisZ * otherZ;
        double x = thisW * otherX + thisX * otherW + thisY * otherZ - thisZ * otherY;
        double y = thisW * otherY - thisX * otherZ + thisY * otherW + thisZ * otherX;
        double z = thisW * otherZ + thisX * otherY - thisY * otherX + thisZ * otherW;

        Quaternion q = new Quaternion();
        q.setW(w);
        q.setVector(new Vector3D(x, y, z));
//        System.out.println("MULT = " + q);

        return q;
    }

    /**
     * Rotates the toRotate Vector3D around the give localCentre as the origin
     * around this quaternion
     * @param toRotate the vector to rotate
     * @param localCentre the centre of rotation
     * @param scale the scalar by which to increse the angle of rotation
     * @return the rotated vector
     */
    public Vector3D rotateWithScale(Vector3D toRotate, Vector3D localCentre, double scale) {
        //.sub(localCentre) called to translate from the local centre to the origin
        toRotate = toRotate.sub(localCentre);
        Vector3D thisVectorPartScaled = this.getVector().mult(scale);
        double wScaled = this.getW() * scale;
        Vector3D crossProd = thisVectorPartScaled.crossProd(toRotate);
        //.add(localCentre) called to detranslate
        return toRotate
                .add(crossProd
                        .mult(2 * wScaled))
                .add(thisVectorPartScaled
                        .crossProd(crossProd)
                        .mult(2))
                .add(localCentre);
    }

    /**
     * Rotates the toRotate Vector3D around the give localCentre as the origin
     * around this quaternion
     * @param toRotate the vector to rotate
     * @param localCentre the centre of rotation
     * @return the rotated vector
     */
    public Vector3D rotate(Vector3D toRotate, Vector3D localCentre) {
        //.sub(localCentre) called to translate from the local centre to the origin
        toRotate = toRotate.sub(localCentre);
        Vector3D thisVectorPart = this.getVector();
        Vector3D crossProd = thisVectorPart.crossProd(toRotate);
        //.add(localCentre) called to detranslate
        return toRotate
                .add(crossProd
                        .mult(2 * this.getW()))
                .add(thisVectorPart
                        .crossProd(crossProd)
                        .mult(2))
                .add(localCentre);
    }

    public Quaternion add(Quaternion other) {
        return Quaternion.makeExactQuaternionRadians(this.w + other.getW(), this.getVector().add(other.getVector()));
    }

    public double magnitude() {
        return Math.sqrt(Math.pow(this.w, 2) +
                Math.pow(this.getVector().getX(), 2) +
                Math.pow(this.getVector().getY(), 2) +
                Math.pow(this.getVector().getZ(), 2));
    }

    public Quaternion normalize() {
        double magnitude = this.magnitude();
//        System.out.println("magnitude = " + magnitude);
        Vector3D normVector = this.getVector().div(magnitude);
        return Quaternion.makeExactQuaternionRadians(this.w / magnitude, normVector);
    }

    public Quaternion mult(double scalar) {
        return new Quaternion(this.w * scalar, this.getVector().mult(scalar));
    }

    public Quaternion getConjugate() {
        return new Quaternion(this.w, this.getVector().flip());
    }

    public Vector3D getVector() {
        return this.vector;
    }

    public void setVector(Vector3D vector) {
        this.vector = vector;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }


}
