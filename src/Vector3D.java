public class Vector3D extends Point3D {

    public static final Vector3D IDENTITY = new Vector3D(1, 1, 1);
    public static final Vector3D NULL = new Vector3D(0, 0, 0);
    public static final Vector3D X = new Vector3D(1, 0, 0);
    public static final Vector3D Y = new Vector3D(0, 1, 0);
    public static final Vector3D Z = new Vector3D(0, 0, 1);

    public Vector3D(double x, double y, double z) {
        super(x, y, z);
    }

    public Vector3D(Vector3D toCopy) {
        super(toCopy.getX(), toCopy.getY(), toCopy.getZ());
    }


    @Override
    public String toString() {
        return "Vector3D = " +
                '<' + this.x + ", " +
                this.y + ", " +
                this.z + '>';
    }

    public String toIndexString() {
        return "Vector3D = " +
                '[' + (int) this.x + ']' +
                '[' + (int) this.y + ']' +
                '[' + (int) this.z + ']';
    }

    public Vector3D normalize() {
        double mag = this.magnitude();
        return this.div(mag);
    }

    public Vector3D flip() {
        return new Vector3D(-this.getX(), -this.getY(), -this.getZ());
    }

    public double magnitude() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
    }

    public Vector3D sub(Vector3D other) {
        return new Vector3D(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ());
    }

    public Vector3D div(double scalar) {
        return new Vector3D(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public Vector3D mult(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double dotProd(Vector3D other) {
        return (this.x * other.getX() + this.y * other.getY() + this.z * other.getZ());
    }

    public Vector3D crossProd(Vector3D other) {
        return new Vector3D(
                this.y * other.getZ() - this.z * other.getY(),
                this.z * other.getX() - this.x * other.getZ(),
                this.x * other.getY() - this.y * other.getX()
        );
    }

    public double cos(Vector3D other) {
        Vector3D thisNorm = this.normalize();
        Vector3D otherNorm = other.normalize();
        double dotProd = thisNorm.dotProd(otherNorm);
        return dotProd > 0 ? dotProd : 0;

    }

    /**
     * Returns new vector rotated counter-clockwise around X axis by theta degrees
     * @param theta
     * @return
     */
    public Vector3D rotateX(double theta) {
        double newX;
        double newY;
        double newZ;

        newX = this.x;
        newY = ((this.y * Math.cos(theta)) - (this.z * Math.sin(theta)));
        newZ = ((this.y * Math.sin(theta)) + (this.z * Math.cos(theta)));

        return new Vector3D(newX, newY, newZ);

    }

    /**
     * Returns new vector rotated counter-clockwise around Y axis by theta degrees
     * @param theta
     * @return
     */
    public Vector3D rotateY(double theta) {
        double newX;
        double newY;
        double newZ;

        newX = ((this.x * Math.cos(theta)) + (this.z * Math.sin(theta)));
        newY = this.y;
        newZ = (-(this.x * Math.sin(theta)) + (this.z * Math.cos(theta)));

        return new Vector3D(newX, newY, newZ);

    }

    /**
     * Returns new vector rotated counter-clockwise around Y axis by theta radians
     * along a circle of a specified radius and centre.
     * @param theta
     * @return
     */
    public Vector3D rotateY(double theta, Vector3D centre) {
        double newX;
        double newY;
        double newZ;

        Vector3D v = this.sub(centre); //translate

        newX = ((v.x * Math.cos(theta)) + (v.z * Math.sin(theta)));
        newY = this.y;
        newZ = (-(v.x * Math.sin(theta)) + (v.z * Math.cos(theta)));

        return new Vector3D(newX, newY, newZ).add(centre); //detranslate

    }

    /**
     * Returns new vector rotate counter-clockwise around Z axis by theta degrees
     * @param theta
     * @return
     */
    public Vector3D rotateZ(double theta) {
        double newX;
        double newY;
        double newZ;

        newX = ((this.x * Math.cos(theta)) - (this.y * Math.sin(theta)));
        newY = ((this.x * Math.sin(theta)) + (this.y * Math.cos(theta)));
        newZ = this.z;

        return new Vector3D(newX, newY, newZ);

    }

    /**
     * Returns a vector perpendicular to this vector on the XZ plane
     * @return
     */
    public Vector3D getPerpendicularUnitY(double degrees) {
        //first, this vector is translated to the origin
        Vector3D thisTranslated = new Vector3D(
                WorldOld.ORIGIN.getX() - this.x,
                this.y,
                WorldOld.ORIGIN.getZ() - this.z);
        //then, the translated vector can be rotated
        Vector3D translatedRotated = thisTranslated.rotateY(Math.toRadians(degrees));
        //finally, the translated and rotated vector can be moved back (de-translated)
        return translatedRotated.add(this);
    }


}
