
/*
CS-255 Getting started code for the assignment
I do not give you permission to post this code online
Do not post your solution online
Do not copy code
Do not use JavaFX functions or other libraries to do the main parts of the assignment:
	1. Creating a resized image (you must implement nearest neighbour and bilinear interpolation yourself
	2. Gamma correcting the image
	3. Creating the image which has all the thumbnails and event handling to change the larger image
All of those functions must be written by yourself
You may use libraries / IDE to achieve a better GUI
*/

import java.io.File;


public class Main  {

    public static final String CT_HEAD_PATH = String.join(File.separator, "resources", "CThead");
    public static final String MR_BRAIN_PATH = String.join(File.separator, "resources", "MRbrain");

    public static final int CT_HEAD_DATASET_SIZE = 113;
    public static final int CT_HEAD_SIDE = 256;

    public static final int MR_BRAIN_DATASET_SIZE = 109;
    public static final int MR_BRAIN_SIDE = 256;

    private static String datasetPath;
    private static int datasetSize;
    private static int datasetHeight;
    private static int datasetWidth;

    public static String getDatasetPath() {
        return datasetPath;
    }

    public static int getDatasetSize() {
        return datasetSize;
    }

    public static int getDatasetHeight() {
        return datasetHeight;
    }

    public static int getDatasetWidth() {
        return datasetWidth;
    }

    public static void setDatasetPath(String datasetPath) {
        Main.datasetPath = datasetPath;
    }

    public static void setDatasetSize(int datasetSize) {
        Main.datasetSize = datasetSize;
    }

    public static void setDatasetHeight(int datasetHeight) {
        Main.datasetHeight = datasetHeight;
    }

    public static void setDatasetWidth(int datasetWidth) {
        Main.datasetWidth = datasetWidth;
    }

}
