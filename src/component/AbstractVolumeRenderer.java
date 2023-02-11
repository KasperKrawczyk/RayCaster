package component;

import component.camera.AbstractCamera;
import component.camera.SingleObjectCamera;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

public abstract class AbstractVolumeRenderer {


    protected final NavigableMap<Short, Color> huToColorMap = new TreeMap<>();
    protected final AbstractCamera camera;


    public AbstractVolumeRenderer(AbstractCamera camera) {
        this.camera = camera;
        populateMapDefault();
    }

    public AbstractVolumeRenderer(AbstractCamera camera, HashMap<Short, Color> huToColorMap) {
        this.camera = camera;
        this.huToColorMap.putAll(huToColorMap);
    }

    public static final String NUM_OF_THREADS_ERR_MSG = "The number of threads should divide the image width with no remainder";

    public abstract ArrayList<? extends AbstractVoxel> collectSamples(Vector3D intersection0, Vector3D intersection1, short[][][] vol);

    public abstract Color compositeSamples(ArrayList<? extends AbstractVoxel> list);


    public abstract Color sampleCompositeShade(Vector3D intersectionVector0, Vector3D intersectionVector1,
                                      short[][][] vol);

    public abstract Image volumeRayCastParallelized(short[][][] vol, int numOfThreads);


    protected void matToImg(Color[][] colorMat, PixelWriter pixelWriter) {
        for (int y = 0; y < AbstractCamera.VIEW_PLANE_HEIGHT; y++) {
            for (int x = 0; x < AbstractCamera.VIEW_PLANE_WIDTH; x++) {
                pixelWriter.setColor(x, y, colorMat[y][x]);
            }
        }
    }

    protected int[][] getBoundingIndices(int sectionWidth) {
        int numOfSections = AbstractCamera.VIEW_PLANE_WIDTH / sectionWidth;
        //[sectionNum][0] for start, [sectionNum][1] for end
        int[][] indices = new int[numOfSections][2];
        int curStart;
        int curEnd;
        for (int sectionNum = 0; sectionNum < numOfSections; sectionNum++) {
            curStart = sectionWidth * sectionNum;
            curEnd = curStart + sectionWidth;
            indices[sectionNum][0] = curStart;
            indices[sectionNum][1] = curEnd;
        }
        return indices;
    }

    private static boolean isCorrectNumThreads(int numOfThreads) {
        return SingleObjectCamera.VIEW_PLANE_WIDTH % numOfThreads == 0;
    }


    private void populateMapDefault() {
        huToColorMap.put((short) -99, Color.WHITE);
        huToColorMap.put((short) 299, Color.color(1, 0.79, 0.6));
        huToColorMap.put((short) 1900, Color.color(0.8902, 0.8549, 0.7882));
        huToColorMap.put(Short.MAX_VALUE, Color.WHITE);
    }

    public boolean addHuToColorMapping(short gteqCeilVal, Color color) {
        Color c = this.huToColorMap.put(gteqCeilVal, color);
        return c == null;
    }

    public boolean removeHuToColorMapping(short gteqCeilVal) {
        Color c = this.huToColorMap.remove(gteqCeilVal);
        return c == null;
    }

}
