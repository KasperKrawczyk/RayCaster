package model;

public class Quaternion {

    public static final Quaternion ZERO = makeExactQuaternionRadians(0, new Vector3D(0, 0, 0));

    private Vector3D vector;
    private double w;

    public Quaternion() {
    }

    public Quaternion(double angle, Vector3D v) {
        double alpha = Math.toRadians(angle);
        this.w = Math.cos(alpha / 2.0);
        this.vector = v.mult(Math.sin(alpha / 2.0));

    }

    /**
     * @param radians in radians
     * @param vector vector to set for the imaginary part of the new quaternion
     * @return a new model.Quaternion object with the exact parameters
     */
    public static Quaternion makeExactQuaternionRadians(double radians, Vector3D vector) {
        Quaternion q = new Quaternion();
        q.setW(radians);
        q.setVector(vector);
        return q;
    }

    /**
     * @param degrees
     * @param vector  vector to set for the imaginary part of the new quaternion
     * @return a new model.Quaternion object with the exact parameters
     */
    public static Quaternion makeExactQuaternionDegrees(double degrees, Vector3D vector) {
        Quaternion q = new Quaternion();
        float radians = (float) (degrees / 180f * Math.PI * 2);
        q.setW(Math.cos(radians) / 2);
        q.setVector(vector.mult(Math.sin(radians)));
        return q;
    }

    public static Quaternion newRotator(double angle, Vector3D vector) {
        return new Quaternion(angle, vector);
    }

    /**
     * Normalises the input vectors and returns a quaternion
     * based on their cross-product
     *
     * @param start the projection of the start of the mouse movement (on mouse down)
     * @param end   the projection of the end of the mouse movement (on mouse up)
     * @return a quaternion to rotate around the cross-product of the input vectors
     */
    public static Quaternion getQuatBetweenVectors(Vector3D start, Vector3D end) {
        Vector3D startNorm = start.normalize();
        Vector3D endNorm = end.normalize();

//        double resDot = startNorm.dotProd(endNorm);
//        if (resDot >= (1 - 1e-16)) {
//            return model.Quaternion.ZERO;
//        }
        Quaternion q = makeExactQuaternionRadians(
                1 + startNorm.dotProd(endNorm),
                startNorm.crossProd(endNorm)
        );
        return q.normalize();

    }

    public static Quaternion getQuatBetweenVectorsNaive(Vector3D startIn, Vector3D endIn) {

        Vector3D start = startIn.normalize();
        Vector3D end = endIn.normalize();
        double dot = start.dotProd(end);
        double wx = start.getY() * end.getZ() - start.getZ() * end.getY();
        double wy = start.getZ() * end.getX() - start.getX() * end.getZ();
        double wz = start.getX() * end.getY() - start.getY() * end.getX();

        return makeExactQuaternionRadians(
                dot + Math.sqrt(dot * dot + wx * wx + wy * wy + wz * wz),
                new Vector3D(wx, wy, wz)
        ).normalize();

    }

    public Quaternion concatenate(double angle, Axis axis) {
        return this.mult(Quaternion.makeExactQuaternionRadians(angle, axis.getVector()));
    }

    @Override
    public String toString() {
        return "model.Quaternion{" +
                "w=" + w +
                ", vector=" + vector +
                '}';
    }

    /**
     * model.Quaternion multiplication by another quaternion combines their angles of rotation
     *
     * @param other the other quaternion to multiply this quaternion by
     * @return a new model.Quaternion, the product of the quaternion-by-quaternion multiplication
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

    public Quaternion mult2(Quaternion q) {
        Vector3D v = q.getVector();
        double nw = w * q.w - vector.x * v.x - vector.y * v.y - vector.z * v.z;
        double nx = w * v.x + vector.x * q.w + vector.y * v.z - vector.z * v.y;
        double ny = w * v.y + vector.y * q.w + vector.z * v.x - vector.x * v.z;
        double nz = w * v.z + vector.z * q.w + vector.x * v.y - vector.y * v.x;
        Quaternion toReturn = new Quaternion();
        q.setW(nw);
        q.setVector(new Vector3D(nx, ny, nz));
        return toReturn;
    }

    /**
     * Rotates the toRotate model.Vector3D around the give localCentre as the origin
     * around this quaternion
     *
     * @param toRotate    the vector to rotate
     * @param localCentre the centre of rotation
     * @param scale       the scalar by which to increse the angle of rotation
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
     * Rotates the toRotate model.Vector3D around the give localCentre as the origin
     * around this quaternion
     *
     * @param in          the vector to rotate
     * @param localCentre the centre of rotation
     * @return the rotated vector
     */
    public Vector3D rotate(Vector3D in, Vector3D localCentre) {
        //.sub(localCentre) called to translate from the local centre to the origin
//        System.out.println("in before sub = " + in);
        Vector3D toRotate = in.sub(localCentre);
//        System.out.println("toRotate = " + toRotate);

        Vector3D thisVectorPart = this.getVector();
        Vector3D crossProd = thisVectorPart.crossProd(toRotate);
        //.add(localCentre) called to detranslate
        toRotate = toRotate
                .add(crossProd
                        .mult(2 * this.getW()))
                .add(thisVectorPart
                        .crossProd(crossProd)
                        .mult(2));
        toRotate = toRotate.add(localCentre);
//        System.out.println("after add(localCentre) = " + toRotate);
//        System.out.println("QUAT AFTER = " + this);
        return toRotate;
    }

    public Vector3D rotate2(Vector3D in, Vector3D localCentre) {
        //.sub(localCentre) called to translate from the local centre to the origin
        System.out.println("in before sub = " + in);
        Vector3D toRotate = in.sub(localCentre);
        System.out.println("toRotate = " + toRotate);

        Vector3D thisVectorPart = this.getVector();
        Vector3D t = thisVectorPart.mult(2).crossProd(toRotate);
        //.add(localCentre) called to detranslate
        toRotate = toRotate.add(t.mult(this.getW())).add(thisVectorPart.crossProd(t));
        toRotate = toRotate.add(localCentre);
        System.out.println("after add(localCentre) = " + toRotate);
        System.out.println("QUAT AFTER = " + this);
        return toRotate;
    }

    public Quaternion add(Quaternion other) {
        return Quaternion.makeExactQuaternionRadians(this.w + other.getW(), this.getVector().add(other.getVector()));
    }

    public double magnitude() {
        return Math.sqrt(
                Math.pow(this.getW(), 2) +
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

    public Quaternion normalize2() {
        double norm = 1 / this.magnitude();

        if (norm < 1e-16) {
            return ZERO;
        }
//        System.out.println("magnitude = " + magnitude);
        double w = this.getW() * norm;
        double x = this.getVector().getX() * norm;
        double y = this.getVector().getY() * norm;
        double z = this.getVector().getZ() * norm;

        return Quaternion.makeExactQuaternionRadians(w, new Vector3D(x, y, z));
    }


}
