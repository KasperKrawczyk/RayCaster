package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import java.text.DecimalFormat;

public class ColorMappingListItem {
    public static final DecimalFormat CMLI_DECIMAL_FMT_2 = new DecimalFormat("#.00");
    public static final DecimalFormat CMLI_DECIMAL_FMT_4 = new DecimalFormat("#.0000");
    private SimpleStringProperty shortDescription;
    private Color color;
    private short ceiling;

    public ColorMappingListItem(Color color, short floor) {
        this.color = color;
        this.ceiling = floor;
        this.shortDescription = new SimpleStringProperty("rgba("
                + CMLI_DECIMAL_FMT_2.format(color.getRed()) + " "
                + CMLI_DECIMAL_FMT_2.format(color.getGreen()) + " "
                + CMLI_DECIMAL_FMT_2.format(color.getBlue()) + " "
                + CMLI_DECIMAL_FMT_2.format(color.getOpacity())
                + ") below " + floor + " HU");
    }

    @Override
    public String toString() {
        return shortDescription.get();
    }

    public String getShortDescription() {
        return shortDescription.get();
    }

    public SimpleStringProperty shortDescriptionProperty() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription.set(shortDescription);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public short getCeiling() {
        return ceiling;
    }

    public void setCeiling(short ceiling) {
        this.ceiling = ceiling;
    }
}
