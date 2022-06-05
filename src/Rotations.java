public class Rotations {

    private Rotations() {
        throw new UnsupportedOperationException(Util.INSTANTIATION_ERR_MSG);
    }


    public static void main(String[] args) {
        Vector3D toRotate = new Vector3D(1, 0, 0);
        System.out.println("toRotate = " + toRotate);
        Quaternion q1 = new Quaternion(90, new Vector3D(0, 1, 0));
        Quaternion q2 = new Quaternion(45, new Vector3D(1, 0, 0));
        Quaternion q3 = q2.mult(q1);
        System.out.println("Quaterion1 = " + q1);
        System.out.println("Quaterion2 = " + q2);
        System.out.println("Quaterion3 = " + q3);
        Vector3D rotatedNaive = q3.rotate(toRotate, World.ORIGIN);
        System.out.println("rotated with q3 = " + q3.rotate(toRotate, World.ORIGIN));
        System.out.println("rotated with q1 = " + q1.rotate(toRotate, World.ORIGIN));

        Vector3D rotated = Quaternion.newRotator(45, Axis.X).concatenate(90, Axis.Y).rotate(toRotate, World.ORIGIN);
        System.out.println("test = " + rotated);
        System.out.println(rotated.equals(rotatedNaive));


    }
}
