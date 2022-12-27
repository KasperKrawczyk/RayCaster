import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.HashMap;

/**
 * This utility class collects static methods necessary for resizing and gamma-correcting images
 *
 * @author Kasper Krawczyk
 */

public class Util {

    public static final String INSTANTIATION_ERR_MSG = "Utility classes should not be instantiated";

    private Util() {
        throw new UnsupportedOperationException(INSTANTIATION_ERR_MSG);
    }


    public static Image rescaleBilinearColour(int newSizeX, int newSizeY, Image image) {
        int oldSizeY = (int) image.getHeight();
        int oldSizeX = (int) image.getWidth();

        WritableImage rescaledImage = new WritableImage(newSizeX, newSizeY);
        PixelWriter pixelWriter = rescaledImage.getPixelWriter();

        Color upLeft;
        Color downLeft;
        Color upRight;
        Color downRight;
        double newRed;
        double newGreen;
        double newBlue;
        double newAlpha;
        float x0;
        float y0;
        float x1;
        float y1;
        float xPrim;
        float yPrim;

        for (int y = 0; y < newSizeY; y++) {
            for (int x = 0; x < newSizeX; x++) {


                yPrim = ((float) y / (float) (newSizeY - 1)) * (oldSizeY - 1);
                xPrim = ((float) x / (float) (newSizeX - 1)) * (oldSizeX - 1);

                x0 = (float) Math.max(Math.min(Math.floor(xPrim), oldSizeX - 1), 0);
                y0 = (float) Math.max(Math.min(Math.floor(yPrim), oldSizeY - 1), 0);
                x1 = (float) Math.max(Math.min(Math.ceil(xPrim), oldSizeX - 1), 0);
                y1 = (float) Math.max(Math.min(Math.ceil(yPrim), oldSizeY - 1), 0);

                xPrim -= x0;
                yPrim -= y0;


                upLeft = image.getPixelReader().getColor((int) x0, (int) y0);
                upRight = image.getPixelReader().getColor((int) x1, (int) y0);
                downLeft = image.getPixelReader().getColor((int) x0, (int) y1);
                downRight = image.getPixelReader().getColor((int) x1, (int) y1);

                newRed = Gradients.blerp(upLeft.getRed(), upRight.getRed(),
                        downLeft.getRed(), downRight.getRed(),
                        xPrim, yPrim);
                newGreen = Gradients.blerp(upLeft.getRed(), upRight.getRed(),
                        downLeft.getRed(), downRight.getRed(),
                        xPrim, yPrim);
                newBlue = Gradients.blerp(upLeft.getBlue(), upRight.getBlue(),
                        downLeft.getBlue(), downRight.getBlue(),
                        xPrim, yPrim);

                newAlpha = Gradients.blerp(upLeft.getOpacity(), upRight.getOpacity(),
                        downLeft.getOpacity(), downRight.getOpacity(),
                        xPrim, yPrim);

                pixelWriter.setColor(x, y, new Color(newRed, newGreen, newBlue, newAlpha));

            }
        }
        return rescaledImage;
    }


    /**
     * Returns the gamma look-up table for the specified gamma value
     *
     * @param gamma the gamma value to produce the look-up table for
     * @return the look-up table for the given gamma as int[]
     */
    private static float[] getGammaLUT(float gamma) {
        float invertedGamma = 1 / gamma;
        float[] table = new float[256];

        for (int i = 0; i < table.length; i++) {
            // i^invertedGamma, where i is in [0, 1]
            table[i] = (float) (255f * (Math.pow(i / 255f, invertedGamma)));
        }

        return table;
    }

    private static float[][] equalize(float[][] mat) {
        float[] minMax = getMinMax(mat);
        float minFloat = minMax[0];
        float maxFloat = minMax[1];
        int height = mat.length;
        int width = mat[0].length;
        float[][] newMat = new float[height][width];
        int numOfPix = height * width;


        int minInt = normalize(minFloat, 0, 255);
        int maxInt = normalize(maxFloat, 0, 255);

        int histSize = maxInt - minInt + 1;
        int[] hist = new int[histSize];

        for (int y = 0; y < mat.length; y++) {
            for (int x = 0; x < mat[0].length; x++) {
                float grey = mat[y][x];
                int bin = (int) (255f * (grey - minFloat));
                hist[bin]++;
            }
        }

        int[] cumulativeDistroTable = getCumulativeDistroLUT(hist);
        int[] eqHist = getEqualizedHist(maxInt - minInt, numOfPix, cumulativeDistroTable);


        for (int y = 0; y < mat.length; y++) {
            for (int x = 0; x < mat[0].length; x++) {
                float grey = mat[y][x];
                int bin = (int) (255f * (grey - minFloat));
                int eqGreyInt = eqHist[bin];
                float eqGreyFloat = eqGreyInt / 255f;
                newMat[y][x] = eqGreyFloat;
            }
        }
        return newMat;

    }

    private static int[] getCumulativeDistroLUT(int[] hist) {
        int[] distroTable = new int[hist.length];
        int curSum = 0;
        for (int i = 0; i < hist.length; i++) {
            curSum += hist[i];
            distroTable[i] = curSum;
        }
        return distroTable;
    }

    private static int[] getEqualizedHist(int numOfLevels, int numOfPixels, int[] cumulativeDistroTable) {
        int[] eqHisto = new int[cumulativeDistroTable.length];
        for (int i = 0; i < cumulativeDistroTable.length; i++) {
            eqHisto[i] = Math.max(0, Math.round(((float) numOfLevels * cumulativeDistroTable[i]) / (numOfPixels)) - 1);
        }
        return eqHisto;
    }


    private static float[][] applyFilter(float[][] mat, int[][] filter) {
        int height = mat.length;
        int width = mat[0].length;
        int marginY = filter.length / 2;
        int marginX = filter[0].length / 2;
        float[][] newMat = new float[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y + marginY < height && y - marginY >= 0 &&
                        x + marginX < width && x - marginX >= 0) {
                    applyFilterToPix(mat, newMat, filter, y, x, marginY, marginX);
                }
            }
        }
        return newMat;
    }

    private static void applyFilterToPix(float[][] mat, float[][] newMat, int[][] filter,
                                         int y, int x, int marginY, int marginX) {
        float sum = 0;
        for (int i = y - marginY; i <= y + marginY; i++) {
            for (int j = x - marginX; j <= x + marginX; j++) {
                sum += mat[i][j] * filter[i - (y - marginY)][j - (x - marginX)];
            }
        }
        newMat[y][x] = sum;
    }


    private static float[] getMinMax(float[][] mat) {
        float minFloat = Float.MAX_VALUE;
        float maxFloat = Float.MIN_VALUE;
        for (int y = 0; y < mat.length; y++) {
            for (int x = 0; x < mat[0].length; x++) {
                float grey = mat[y][x];
                minFloat = Math.min(minFloat, grey);
                maxFloat = Math.max(maxFloat, grey);
            }
        }
        return new float[]{minFloat, maxFloat};
    }

    private static void normalizeMat(float[][] mat, float min, float max) {
        for (int y = 0; y < mat.length; y++) {
            for (int x = 0; x < mat[0].length; x++) {
                mat[y][x] = (mat[y][x] - min) / (max - min);
            }
        }
    }

    public static int normalize(float val, int min, int max) {
        return (int) ((max - min) * val);
    }

    private static Image volumeRender(short[][][] vol, Vector3D light, Vector3D eye) {
        int height = vol[0].length;
        int depth = vol.length;
        int width = vol[0][0].length;
        WritableImage renderedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = renderedImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    short val = vol[z][y][x];
                    if (val >= 500 && val <= 1900 && z > 1) {
                        Color materialColor = new Color(1.0, 0.79, 0.6, 1);
                        float materialLocationOffset = Math.min(1, (val - (float) vol[z - 1][y][x]) / (val - vol[z - 2][y][x]));
                        float materialInterpolated = Gradients.lerp(z - 1, 1, materialLocationOffset);
                        Vector3D gradient = Gradients.get3DGradientInterpolated3D(z, y, x, vol, materialInterpolated);
                        Vector3D intersectionPoint = new Vector3D(materialInterpolated, y, x);

                        Color colorAppliedReflectionModel = Reflections.applyReflectionModel(light, eye, intersectionPoint,
                                0.4f, 0.65f,
                                gradient, materialColor);
                        pixelWriter.setColor(x, y, colorAppliedReflectionModel);
                        break;
                    } else if (z == depth - 1) {
                        pixelWriter.setColor(x, y, new Color(0, 0, 0, 0));

                    }

                }

            }
        }
        return renderedImage;
    }

    public static Image updateRendering(int newY, int newX, Algo resizeAlgo, Vector3D light, Vector3D eye) {
        Image updatedImage;


        updatedImage = volumeRender(DataSet.getBytes(), light, eye);
        if (resizeAlgo == Algo.BILINEAR) {
            updatedImage = rescaleBilinearColour(newX, newY, updatedImage);
        }


        return updatedImage;
    }

    public static void writeHistogram(short[][][] vol) {
        File histoFile = new File("resources/histoData.txt");
        HashMap<Short, Integer> map = new HashMap<>();
        int height = vol[0].length;
        int depth = vol.length;
        int width = vol[0][0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    short val = vol[z][y][x];
                    if (map.containsKey(val)) {
                        map.put(val, map.get(val) + 1);
                    } else {
                        map.put(val, 1);
                    }


                }
            }
        }

        short min = Short.MAX_VALUE;
        short max = Short.MIN_VALUE;
        for (short val : map.keySet()) {
            max = (short) Math.max(max, val);
            min = (short) Math.min(min, val);
        }
        System.out.println("max = " + max);
        System.out.println("min = " + min);
        int histoLength = max + Math.abs(min) + 1;
        System.out.println("histoLength = " + histoLength);
        int[] histo = new int[histoLength];
        for (short val : map.keySet()) {
            histo[val + Math.abs(min)] += map.get(val);
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(histoFile), "utf-8"))) {
            for (int i = 0; i < histo.length; i++) {

                writer.append(String.valueOf(i + min)).append(",").append(String.valueOf(histo[i])).append("\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        for (int i = 0; i < histoLength; i++) {
//            System.out.println(i + " " + histo[i]);
//        }


    }

}
