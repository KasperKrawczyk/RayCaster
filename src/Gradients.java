public class Gradients {

    private Gradients() {
        throw new UnsupportedOperationException(Util.INSTANTIATION_ERR_MSG);
    }

    /**
     * Calculates the bi-linearly interpolated value at (x, y),
     * given the height and width of the starting image
     *
     * @param height the height of the old image
     * @param width  the width of the old image
     * @param y      the floating-point y coordinate to be found the interpolated value for
     * @param x      the floating-point x coordinate to be found the interpolated value for
     * @param grey   the floating-point encoding of the grey-scale sample
     * @return the interpolated value for (x, y)
     */
    static float blerpWeighted(int height, int width, float y, float x, float[][] grey) {
        // 'corner' colour values
        float upLeft;
        float downLeft;
        float upRight;
        float downRight;
        // cast as ints - index values for 'corners'
        float x0;
        float y0;
        float x1;
        float y1;
        // used to determine weights
        float weightX;
        float weightY;

        // four index values needed to determine the 'corner' colour values
        x0 = (float) Math.max(Math.min(Math.floor(x), width - 1), 0);
        y0 = (float) Math.max(Math.min(Math.floor(y), height - 1), 0);
        x1 = (float) Math.max(Math.min(Math.ceil(x), width - 1), 0);
        y1 = (float) Math.max(Math.min(Math.ceil(y), height - 1), 0);

        // 'corner' colour values
        upLeft = grey[(int) y0][(int) x0];
        downLeft = grey[(int) y1][(int) x0];
        upRight = grey[(int) y0][(int) x1];
        downRight = grey[(int) y1][(int) x1];

        weightY = y - y0;
        weightX = x - x0;

        return (upLeft * (1 - weightX) * (1 - weightY)
                + downLeft * weightY * (1 - weightX)
                + upRight * weightX * (1 - weightY)
                + downRight * weightX * weightY);
    }

    /**
     * Calculates the linearly interpolated value between the floating-point
     * values grey1 and grey2
     *
     * @param val1     the smaller value
     * @param val2     the larger value
     * @param quotient the quotient of the linear interpolation equation
     * @return
     */
    public static float lerp(float val1, float val2, float quotient) {
        return val1 + ((val2 - val1) * quotient);
    }

    public static double lerp(double val1, double val2, float quotient) {
        return val1 + ((val2 - val1) * quotient);
    }

    public static Vector3D lerp(Vector3D vec1, Vector3D vec2, float quotient) {
        return new Vector3D(
                lerp(vec1.getX(), vec2.getX(), quotient),
                lerp(vec1.getY(), vec2.getY(), quotient),
                lerp(vec1.getZ(), vec2.getZ(), quotient)
        );
    }

    public static Vector3D blerp(Vector3D vec1, Vector3D vec2, Vector3D vec3, Vector3D vec4,
                                 float quotient12, float quotient34, float quotient56) {
        Vector3D vec5 = lerp(vec1, vec2, quotient12);
        Vector3D vec6 = lerp(vec3, vec4, quotient34);
        return lerp(vec5, vec6, quotient56);
    }

    public static float blerp(float upLeft, float upRight, float downLeft, float downRight, float xQuotient, float yQuotient) {
        float interGreyUp = lerp(upLeft, upRight, xQuotient);
        float interGreyDown = lerp(downLeft, downRight, xQuotient);
        return lerp(interGreyUp, interGreyDown, yQuotient);
    }

    public static double blerp(double upLeft, double upRight, double downLeft, double downRight, float xQuotient, float yQuotient) {
        double interGreyUp = lerp(upLeft, upRight, xQuotient);
        double interGreyDown = lerp(downLeft, downRight, xQuotient);
        return lerp(interGreyUp, interGreyDown, yQuotient);
    }

    private static float tlerp(float upLeft0, float upRight0,
                               float downLeft0, float downRight0,
                               float upLeft1, float upRight1,
                               float downLeft1, float downRight1,
                               float xQuotient, float yQuotient, float zQuotient) {
        float interGreyUp0 = lerp(upLeft0, upRight0, xQuotient);
        float interGreyDown0 = lerp(downLeft0, downRight0, xQuotient);
        float interGreyUp1 = lerp(upLeft1, upRight1, xQuotient);
        float interGreyDown1 = lerp(downLeft1, downRight1, xQuotient);
        float blerp0 = lerp(interGreyUp0, interGreyDown0, yQuotient);
        float blerp1 = lerp(interGreyUp1, interGreyDown1, yQuotient);
        return lerp(blerp0, blerp1, zQuotient);

    }

    public static float interpolatedMaterial(int y, int z, int x, short[][][] vol, float val) {
        float materialLocationOffsetX = Math.min(1, (val - (float) vol[y][z][x - 1]) / val);
        float materialLocationOffsetY = Math.min(1, (val - (float) vol[y - 1][z][x]) / val);
        float materialLocationOffsetZ = Math.min(1, (val - (float) vol[y][z - 1][x]) / val);

        float interpolatedX = lerp(Math.max(x - 1, 0), 1, materialLocationOffsetX);
        float interpolatedY = lerp(Math.max(y - 1, 0), 1, materialLocationOffsetY);
        float interpolatedZ = lerp(Math.max(z - 1, 0), 1, materialLocationOffsetZ);


        return (interpolatedX + interpolatedY + interpolatedZ) / 3;
    }

    public static Vector3D get3DGradientInterpolated3D(int z, int y, int x, short[][][] vol, float depthInterpolated) {
        float decimalPart = depthInterpolated - (int) depthInterpolated;

        float gradientZ = getLinearGradient(z, y, x, z, vol.length, 1, 2, 0, 0, 0, 0, vol, decimalPart);
        float gradientY = getLinearGradient(z, y, x, y, vol[0].length, 0, 0, 1, 2, 0, 0, vol, decimalPart);
        float gradientX = getLinearGradient(z, y, x, x, vol[0][0].length, 0, 0, 0, 0, 1, 2, vol, decimalPart);


        return new Vector3D(gradientX, gradientY, gradientZ);
    }

    public static Vector3D get3DGradientInterpolated3D(double depthInterpolated, double heightInterpolated, double widthInterpolated,
                                                       short[][][] vol) {
        int z = (int) depthInterpolated;
        int y = (int) heightInterpolated;
        int x = (int) widthInterpolated;
        float decimalPartDepth = (float) (depthInterpolated - z);
        float decimalPartHeight = (float) (heightInterpolated - y);
        float decimalPartWidth = (float) (widthInterpolated - x);

        float gradientZ = getLinearGradient(z, y, x, z, vol.length, 1, 2, 0, 0, 0, 0, vol, decimalPartDepth);
        float gradientY = getLinearGradient(z, y, x, y, vol[0].length, 0, 0, 1, 2, 0, 0, vol, decimalPartHeight);
        float gradientX = getLinearGradient(z, y, x, x, vol[0][0].length, 0, 0, 0, 0, 1, 2, vol, decimalPartWidth);


        return new Vector3D(gradientX, gradientY, gradientZ);
    }

    public static Vector3D get3DGradientNonInterpolated(int z, int y, int x, short[][][] vol) {

        float gradientZ = getLinearGradientNonInterpolated(z, y, x, z, vol.length, 1, 0,0, vol);
        float gradientY = getLinearGradientNonInterpolated(z, y, x, y, vol[0].length, 0, 1 ,0, vol);
        float gradientX = getLinearGradientNonInterpolated(z, y, x, x, vol[0][0].length, 0, 0, 1, vol);


        return new Vector3D(gradientX, gradientY, gradientZ);
    }

    private static float getLinearGradient(int z, int y, int x, int alongAxis, int axisLength,
                                           int zLo, int zHi,
                                           int yLo, int yHi,
                                           int xLo, int xHi,
                                           short[][][] vol, float decimalPart) {
        float x0;
        float x1;

        if (alongAxis < 2) {
            //forward difference
            x0 = vol[z][y][x] + (vol[z][y][x] * decimalPart);
            x1 = vol[z][y][x] + ((vol[z + zLo][y + yLo][x + xLo] - vol[z][y][x]) * decimalPart);
        } else if (alongAxis == axisLength - 1) {
            //backward difference
            x0 = vol[z - zHi][y - yHi][x - xHi] + ((vol[z - zLo][y - yLo][x - xLo] - vol[z - zHi][y - yHi][x - xHi]) * decimalPart);
            x1 = vol[z][y][x] + (vol[z][y][x] * decimalPart);
        } else {
            //central difference
            x0 = vol[z - zHi][y - yHi][x - xHi] + ((vol[z - zLo][y - yLo][x - xLo] - vol[z - zHi][y - yHi][x - xHi]) * decimalPart);
            x1 = vol[z][y][x] + ((vol[z + zLo][y + yLo][x + xLo] - vol[z][y][x]) * decimalPart);
        }
        return x1 - x0;
    }

    /**
     * Non-interpolated gradient
     * @param z
     * @param y
     * @param x
     * @param alongAxis
     * @param axisLength
     * @param zLo
     * @param yLo
     * @param xLo
     * @param vol
     * @return
     */
    private static float getLinearGradientNonInterpolated(int z, int y, int x, int alongAxis, int axisLength,
                                           int zLo, int yLo, int xLo,
                                           short[][][] vol) {
        float x0;
        float x1;

        if (alongAxis < 2) {
            //forward difference
            x0 = vol[z][y][x];
            x1 = vol[z + zLo][y + yLo][x + xLo];
        } else if (alongAxis == axisLength - 1) {
            //backward difference
            x0 = vol[z - zLo][y - yLo][x - xLo];
            x1 = vol[z][y][x];
        } else {
            //central difference
            x0 = vol[z - zLo][y - yLo][x - xLo];
            x1 = vol[z + zLo][y + yLo][x + xLo];
        }
        return x1 - x0;
    }

    public static short tlerp(double x, double y, double z, short[][][] vol) {
        int depth = vol.length;
        int height = vol[0].length;
        int width = vol[0][0].length;
        x = Math.min(Math.max(0, x), width - 1);
        y = Math.min(Math.max(0, y), height - 1);
        z = Math.min(Math.max(0, z), depth - 1);


        float x0 = (float) Math.max(Math.min(Math.floor(x), width - 1), 0);
        float y0 = (float) Math.max(Math.min(Math.floor(y), height - 1), 0);
        float z0 = (float) Math.max(Math.min(Math.floor(z), depth - 1), 0);
        float x1 = (float) Math.max(Math.min(Math.ceil(x), width - 1), 0);
        float y1 = (float) Math.max(Math.min(Math.ceil(y), height - 1), 0);
        float z1 = (float) Math.max(Math.min(Math.ceil(z), depth - 1), 0);


        float xPrim = (float) (x - x0);
        float yPrim = (float) (y - y0);
        float zPrim = (float) (z - z0);

        // 'corner' colour values
        short upLeftClose = vol[(int) x0][(int) y0][(int) z0];
        short downLeftClose = vol[(int) x1][(int) y0][(int) z0];
        short upRightClose = vol[(int) x0][(int) y1][(int) z0];
        short downRightClose = vol[(int) x1][(int) y1][(int) z0];

        short upLeftFar = vol[(int) x0][(int) y0][(int) z1];
        short downLeftFar = vol[(int) x1][(int) y0][(int) z1];
        short upRightFar = vol[(int) x0][(int) y1][(int) z1];
        short downRightFar = vol[(int) x1][(int) y1][(int) z1];

        float closeInterpolated = blerp(upLeftClose, upRightClose,
                downLeftClose, downRightClose,
                xPrim, yPrim);
        float farInterpolated = blerp(upLeftFar, upRightFar,
                downLeftFar, downRightFar,
                xPrim, yPrim);

        float depthInterpolated = lerp(closeInterpolated, farInterpolated, zPrim);

        return (short) depthInterpolated;

    }
}
