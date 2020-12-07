package elkaproj.config;

/**
 * Represents width and height, specified in arbitrary units, in Cartesian coordinate system.
 */
public class Dimensions {

    private int w, h;

    /**
     * Creates a new dimensions object, which represents width and height in arbitrary units in Cartesian coordinate
     * system.
     *
     * @param width Width of the object this class represents the size of.
     * @param height Height of the object this class represents the size of.
     */
    public Dimensions(int width, int height) {
        this.w = width;
        this.h = height;
    }

    /**
     * Gets the width represented by this dimensions object.
     * @return Width represented by this dimensions object.
     */
    public int getWidth() {
        return this.w;
    }

    /**
     * Gets the height represented by this dimensions object.
     * @return Height represented by this dimensions object.
     */
    public int getHeight() {
        return this.h;
    }
}
