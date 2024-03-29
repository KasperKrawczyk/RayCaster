package mathutil;

import model.Vector3D;

public class Gradients {

    private Gradients() {
        throw new UnsupportedOperationException(ImageUtil.INSTANTIATION_ERR_MSG);
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

        float gradientZ = getLinearGradientNonInterpolated(z, y, x, z, vol.length, 1, 0, 0, vol);
        float gradientY = getLinearGradientNonInterpolated(z, y, x, y, vol[0].length, 0, 1, 0, vol);
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


    private static float getLinearGradient2(int z, int y, int x, int alongAxis, int axisLength,
                                            int zLo, int zHi,
                                            int yLo, int yHi,
                                            int xLo, int xHi,
                                            short[][][] vol, float decimalPart) {
        float x0;
        float x1;

        if (alongAxis < 3) {
            //forward difference
            x0 = lerp(vol[z][y][x], vol[z + zLo][y + yLo][x + xLo], decimalPart);
            x1 = lerp(vol[z + zLo][y + yLo][x + xLo], vol[z + zHi][y + yHi][x + xHi], decimalPart);
        } else if (alongAxis > axisLength - 3) {
            //backward difference
            x0 = lerp(vol[z - zHi][y - yHi][x - xHi], vol[z - zLo][y - yLo][x - xLo], decimalPart);
            x1 = lerp(vol[z - zLo][y - yLo][x - xLo], vol[z][y][x], decimalPart);
        } else {
            //central difference
            x0 = lerp(vol[z - zHi][y - yHi][x - xHi], vol[z - zLo][y - yLo][x - xLo], decimalPart);
            x1 = lerp(vol[z + zLo][y + yLo][x + xLo], vol[z + zHi][y + yHi][x + xHi], decimalPart);
        }
        return x1 - x0;
    }

    /**
     * Non-interpolated gradient
     *
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

    public static short tlerp(double z, double y, double x, short[][][] vol) {
        int depth = vol.length;
        int height = vol[0].length;
        int width = vol[0][0].length;
        z = Math.min(Math.max(0, z), depth - 1);
        y = Math.min(Math.max(0, y), height - 1);
        x = Math.min(Math.max(0, x), width - 1);


        float x0 = (float) Math.max(Math.min(Math.floor(x), width - 1), 0);
        float y0 = (float) Math.max(Math.min(Math.floor(y), height - 1), 0);
        float z0 = (float) Math.max(Math.min(Math.floor(z), depth - 1), 0);
        float x1 = (float) Math.max(Math.min(Math.ceil(x), width - 1), 0);
        float y1 = (float) Math.max(Math.min(Math.ceil(y), height - 1), 0);
        float z1 = (float) Math.max(Math.min(Math.ceil(z), depth - 1), 0);


        float xPrim = (float) (x - x0);
        float yPrim = (float) (y - y0);
        float zPrim = (float) (z - z0);

        int z0Int = (int) z0;
        int y0Int = (int) y0;
        int x0Int = (int) x0;
        int z1Int = (int) z1;
        int y1Int = (int) y1;
        int x1Int = (int) x1;

        // 'corner' colour values
        short upLeftClose = vol[z0Int][y0Int][x0Int];
        short downLeftClose = vol[z0Int][y1Int][x0Int];
        short upRightClose = vol[z0Int][y0Int][x1Int];
        short downRightClose = vol[z0Int][y1Int][x1Int];

        short upLeftFar = vol[z1Int][y0Int][x0Int];
        short downLeftFar = vol[z1Int][y1Int][x0Int];
        short upRightFar = vol[z1Int][y0Int][x1Int];
        short downRightFar = vol[z1Int][y1Int][x1Int];

        float closeInterpolated = blerp(upLeftClose, upRightClose,
                downLeftClose, downRightClose,
                xPrim, yPrim);
        float farInterpolated = blerp(upLeftFar, upRightFar,
                downLeftFar, downRightFar,
                xPrim, yPrim);

        float depthInterpolated = lerp(closeInterpolated, farInterpolated, zPrim);

        return (short) depthInterpolated;

    }

    public static short[][][] rescaleTricubic(short[][][] vol, int newSizeX, int newSizeY, int newSizeZ) {
        int oldSizeX = vol[0][0].length;
        int oldSizeY = vol[0].length;
        int oldSizeZ = vol.length;
        short[][][] rescaled = new short[newSizeZ][newSizeY][newSizeX];

        float zPrim;
        float yPrim;
        float xPrim;
        float x0;
        float y0;
        float z0;

        for (int z = 0; z < newSizeZ; z++) {
            for (int y = 0; y < newSizeY; y++) {
                for (int x = 0; x < newSizeX; x++) {

                    yPrim = ((float) y / (float) (newSizeY - 1)) * (oldSizeY - 1);
                    xPrim = ((float) x / (float) (newSizeX - 1)) * (oldSizeX - 1);
                    zPrim = ((float) z / (float) (newSizeZ - 1)) * (oldSizeZ - 1);

                    x0 = (float) Math.max(Math.min(Math.floor(xPrim), oldSizeX - 1), 0);
                    y0 = (float) Math.max(Math.min(Math.floor(yPrim), oldSizeY - 1), 0);
                    z0 = (float) Math.max(Math.min(Math.floor(zPrim), oldSizeZ - 1), 0);

                    xPrim -= x0;
                    yPrim -= y0;
                    zPrim -= z0;

                    rescaled[z][y][x] = (short) interpolateTricubic(vol, (int) x0, (int) y0, (int) z0,
                            xPrim, yPrim, zPrim);

                }
            }

        }
        return rescaled;

    }

    public static short interpolateTricubic(float x, float y, float z, short[][][] vol) {
        int width = vol[0][0].length;
        int height = vol[0].length;
        int depth = vol.length;

        int x0 = (int) Math.max(Math.min(Math.floor(x), width - 1), 0);
        int y0 = (int) Math.max(Math.min(Math.floor(y), height - 1), 0);
        int z0 = (int) Math.max(Math.min(Math.floor(z), depth - 1), 0);

        float xPrim = x - x0;
        float yPrim = y - y0;
        float zPrim = z - z0;

        return (short) interpolateTricubic(vol, x0, y0, z0, xPrim, yPrim, zPrim);
    }

    public static float interpolateCubic(float[] arr, float xPrim) {
        float p0;
        float p1;
        float p2;
        float p3;
        p0 = arr[0];
        p1 = arr[1];
        p2 = arr[2];
        p3 = arr[3];

        return (float) (p1 + 0.5 * xPrim * (p2 - p0 + xPrim * (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3 + xPrim * (3.0 * (p1 - p2) + p3 - p0))));
    }

    public static float interpolateCubic(short[] arr, int curX, float xPrim) {
        float p0;
        float p1;
        float p2;
        float p3;

        if (curX == 0) {
            p1 = arr[curX];
            p2 = arr[curX + 1];
            p0 = 2 * p1 - p2;
            p3 = arr[curX + 2];
        } else if (curX == arr.length - 1) {
            p0 = arr[curX - 2];
            p1 = arr[curX - 1];
            p2 = arr[curX];
            p3 = 2 * p2 - p1;
        } else {
            p0 = arr[curX - 1];
            p1 = arr[curX];
            p2 = arr[curX + 1];
            if (curX + 2 > arr.length - 1) {
                p3 = 2 * p2 - p1;
            } else {
                p3 = arr[curX + 2];
            }
        }

        return (float) (p1 + 0.5 * xPrim * (p2 - p0 + xPrim * (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3 + xPrim * (3.0 * (p1 - p2) + p3 - p0))));
    }

    private static float interpolateTricubic(short[][][] vol, int curX, int curY, int curZ,
                                             float xPrim, float yPrim, float zPrim) {
        float p0;
        float p1;
        float p2;
        float p3;
        if (curZ == 0) {
            p1 = interpolateBicubic(vol[curZ], curX, curY, yPrim, zPrim);
            p2 = interpolateBicubic(vol[curZ + 1], curX, curY, yPrim, zPrim);
            p0 = 2 * p1 - p2;
            p3 = interpolateBicubic(vol[curZ + 2], curX, curY, yPrim, zPrim);
        } else if (curZ == vol.length - 1) {
            p1 = interpolateBicubic(vol[curZ], curX, curY, yPrim, zPrim);
            p2 = interpolateBicubic(vol[curZ - 1], curX, curY, yPrim, zPrim);
            p0 = 2 * p2 - p1;
            p3 = interpolateBicubic(vol[curZ - 2], curX, curY, yPrim, zPrim);
        } else {
            p0 = interpolateBicubic(vol[curZ - 1], curX, curY, yPrim, zPrim);
            p1 = interpolateBicubic(vol[curZ], curX, curY, yPrim, zPrim);
            p2 = interpolateBicubic(vol[curZ + 1], curX, curY, yPrim, zPrim);
            if (curZ + 2 > vol.length - 1) {
                p3 = 2 * p2 - p1;
            } else {
                p3 = interpolateBicubic(vol[curY + 2], curX, curY, yPrim, zPrim);
            }

        }

        return interpolateCubic(new float[]{p0, p1, p2, p3}, xPrim);
    }

    public static float interpolateBicubic(short[][] mat, int curX, int curY, float xPrim, float yPrim) {

        float p0;
        float p1;
        float p2;
        float p3;

        if (curY == 0) {
            p1 = interpolateCubic(mat[curY], curX, xPrim);
            p2 = interpolateCubic(mat[curY + 1], curX, xPrim);
            p0 = 2 * p1 - p2;
            p3 = interpolateCubic(mat[curY + 2], curX, xPrim);
        } else if (curY == mat.length - 1) {
            p1 = interpolateCubic(mat[curY], curX, xPrim);
            p2 = interpolateCubic(mat[curY - 1], curX, xPrim);
            p0 = 2 * p1 - p2;
            p3 = interpolateCubic(mat[curY - 2], curX, xPrim);
        } else {
            p0 = interpolateCubic(mat[curY - 1], curX, xPrim);
            p1 = interpolateCubic(mat[curY], curX, xPrim);
            p2 = interpolateCubic(mat[curY + 1], curX, xPrim);
            if (curY + 2 > mat.length - 1) {
                p3 = 2 * p2 - p1;
            } else {
                p3 = interpolateCubic(mat[curY + 2], curX, xPrim);
            }
        }

        return interpolateCubic(new float[]{p0, p1, p2, p3}, yPrim);
    }

    public static double mapToNewRange(double input, double minInput, double maxInput, double minOutput, double maxOutput, int deciPrec) {
        double deltaInput = maxInput - minInput;
        double deltaOutput = maxOutput - minOutput;
        double scale = deltaOutput / deltaInput;
        double negA = -1 * minInput;
        double offset = (negA * scale) + minOutput;
        double finalNumber = (input * scale) + offset;
        int calcScale = (int) Math.pow(10, deciPrec);
        return (double) Math.round(finalNumber * calcScale) / calcScale;
    }

}
