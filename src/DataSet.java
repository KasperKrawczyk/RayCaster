import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.*;

/**
 * This class represents a dataset created from
 * a raw byte CT scan file, encoded as a float[][][] of [0..1] bounded values.
 *
 * @author Kasper Krawczyk
 */
public class DataSet {


    public static double[] opacityLUT;
    public static final String FILE_NOT_FOUND_ERR_MSG = "Error: The %s file is not in the working directory";
    public static final String WORK_DIR_MSG = "Working Directory = " + System.getProperty("user.dir");

    public final int size;
    public final int height;
    public final int width;

    private static float[][][] grey;
    private static short[][][] bytes;
    private static float[][][] gradients;

    private final String path;

    private File file;

    private short minVolValue = Short.MAX_VALUE;
    private short maxVolValue = Short.MIN_VALUE;
    private float minVolumeGradientMagnitude = Float.MAX_VALUE;
    private float maxVolumeGradientMagnitude = Float.MIN_VALUE;


    /**
     * Creates a dataset from the provided raw byte CT scan file
     * @param path
     * @param size
     * @param height
     * @param width
     */
    public DataSet(String path, int size, int height, int width) {
        this.path = path;
        this.file = new File(path);
        this.size = size;
        this.height = height;
        this.width = width;
        bytes = new short[this.getHeight()][this.getSize()][this.getWidth()]; //allocate the memory - note this is fixed for this data set
        grey = new float[this.getHeight()][this.getSize()][this.getWidth()];
        try {
            this.parseBytes();

        }  catch (IOException ioe) {
            System.out.println(String.format(FILE_NOT_FOUND_ERR_MSG, path));
            System.out.println(WORK_DIR_MSG);
            return;
        }
        this.set3DGradients(getBytes());
        this.setOpacityLUT();
        this.parseGrey();
    }

    /**
     * Returns a float[][] from the [0..1] bounded CT data
     * @param sliceNum
     * @return
     */
    public float[][] getGreySlice(int sliceNum){
        return getGrey()[sliceNum];
    }

    /**
     * Normalises short x to the interval between short min and short max
     * @param x short value to be normalised
     * @param min short lower bound
     * @param max short upper bound
     * @return normalised short value x
     */
    private float normalize(short x, short min, short max){
        return ((float) x - (float) min)
                / ((float) max - (float) min);
    }

    /**
     * Returns the Image created from a 2d matrix
     * representing a CT slice from the 3d matrix representing the dataset
     * @param sliceNum the 2d matrix from the 3d dataset to be returned
     * @return
     */
    public Image getSlice(int sliceNum) {
        WritableImage writableImage = new WritableImage(this.getWidth(), this.getHeight());
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();
        float val;

        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                val = this.getGrey()[sliceNum][y][x];
                Color color = Color.color(val, val, val);

                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }

    /**
     * Reads in byte data from the dataset
     * @throws IOException is thrown in case of IO failure
     */
    private void parseBytes() throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        short read; //value read in
        int b1;
        int b2;

        //loop through the data reading it in
        for (int k = 0; k < this.getSize(); k++) {
            for (int j = 0; j < this.getHeight(); j++) {
                for (int i = 0; i < this.getWidth(); i++) {

                    b1 = ((int) in.readByte()) & 0xff; //the 0xff
                    b2 = ((int) in.readByte()) & 0xff;
                    read = (short) ((b2 << 8) | b1); //and swizzle the bytes around
                    //read = Short.reverseBytes((short) in.readUnsignedByte());
                    if (read < this.getMinVolValue()) this.setMinVolValue(read); //update the minimum
                    if (read > this.getMaxVolValue()) this.setMaxVolValue(read); //update the maximum
                    getBytes()[j][k][i] = read;
                }
            }
        }
    }


    /**
     * Sets gradients of the 3D volume data as a 3D float array,
     * along with the min and the max of the gradients and the overall min and max of the volume.
     * @param vol the volume data
     * @return Object[] {3D matrix, min gradient, max gradient, min overall value, max overall value}
     */
    public void set3DGradients(short[][][] vol) {
        int height = vol[0].length;
        int depth = vol.length;
        int width = vol[0][0].length;
        float[][][] mat = new float[depth][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    mat[z][y][x] = (float) Gradients.get3DGradientNonInterpolated(z, y, x, vol).magnitude();
                    this.minVolumeGradientMagnitude = Math.min(mat[z][y][x], this.minVolumeGradientMagnitude);
                    this.maxVolumeGradientMagnitude = Math.max(mat[z][y][x], this.maxVolumeGradientMagnitude);

                    this.minVolValue = (short) Math.min(vol[z][y][x], this.minVolValue);
                    this.maxVolValue = (short) Math.max(vol[z][y][x], this.maxVolValue);

                }

            }
        }
        gradients = mat;
    }

    private void setOpacityLUT() {
        opacityLUT = new double[(int) this.maxVolumeGradientMagnitude + 1];

        for (int i = 0; i < opacityLUT.length; i++) {
            opacityLUT[i] = i / this.maxVolumeGradientMagnitude;
        }
    }

    /**
     * Populates the 3d array of normalised floating-point values representing the grey-scale dataset
     */
    private void parseGrey(){
        for (int k = 0; k < this.getSize(); k++) {
            for (int j = 0; j < this.getHeight(); j++) {
                for (int i = 0; i < this.getWidth(); i++) {
                    getGrey()[j][k][i] = normalize(getBytes()[j][k][i], this.getMinVolValue(), this.getMaxVolValue());
                }
            }
        }
    }

    public static short[][][] getBytes() {
        return bytes;
    }

    public static float[][][] getGrey() {
        return grey;
    }

    public short getMinVolValue() {
        return minVolValue;
    }

    public void setMinVolValue(short minVolValue) {
        this.minVolValue = minVolValue;
    }

    public short getMaxVolValue() {
        return maxVolValue;
    }

    public void setMaxVolValue(short maxVolValue) {
        this.maxVolValue = maxVolValue;
    }

    public int getSize() {
        return size;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public File getFile() {
        return file;
    }

    public static float[][][] getGradients() {
        return gradients;
    }

    public String getPath() {
        return path;
    }

    public float getMinVolumeGradientMagnitude() {
        return minVolumeGradientMagnitude;
    }

    public float getMaxVolumeGradientMagnitude() {
        return maxVolumeGradientMagnitude;
    }

    public static double[] getOpacityLUT() {
        return opacityLUT;
    }
}
