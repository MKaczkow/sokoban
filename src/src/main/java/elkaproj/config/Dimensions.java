package elkaproj.config;

import java.util.Objects;

/**
 * Represents width and height, specified in arbitrary units, in Cartesian coordinate system.
 */
public class Dimensions {

    private final int w, h;

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

    /**
     * Comprares this coordinates object to another one.
     * @param o Another object to compare to.
     * @return Whether the two objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimensions that = (Dimensions) o;
        return this.w == that.w && h == that.h;
    }

    /**
     * Computes a hash code for this object instance.
     * @return Computed hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.w, this.h);
    }

    /**
     * Represents a coordinate change.
     */
    public static class Delta {

        private final Dimensions from, to;

        /**
         * Creates a new coordinate delta from given coordinates.
         * @param from Source coordinates.
         * @param to Target coordinates.
         */
        public Delta(Dimensions from, Dimensions to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Gets the source component of this delta.
         * @return Source component of this delta.
         */
        public Dimensions getFrom() {
            return this.from;
        }

        /**
         * Gets the target component of this delta.
         * @return Target component of this delta.
         */
        public Dimensions getTo() {
            return this.to;
        }

        /**
         * Gets the X change of this delta.
         * @return X change of this delta.
         */
        public int getXChange() {
            return this.to.w - this.from.w;
        }

        /**
         * Gets the Y change of this delta.
         * @return Y change of this delta.
         */
        public int getYChange() {
            return this.to.h - this.from.h;
        }

        /**
         * Gets the overall magnitude of this change.
         * @return Magnitude of this change.
         */
        public double getMagnitude() {
            return Math.sqrt(Math.pow(this.getXChange(), 2) + Math.pow(this.getYChange(), 2));
        }

        /**
         * Checks whether this delta is a match for given target.
         * @param target Target to check against.
         * @return Whether the end coordinates of this delta match the desired target.
         */
        public boolean isForTarget(Dimensions target) {
            return this.to.equals(target);
        }

        /**
         * Compares this delta to another one.
         * @param o Another object to compare to.
         * @return Whether the objects are equal.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Delta delta = (Delta) o;
            return this.from.equals(delta.from) && this.to.equals(delta.to);
        }

        /**
         * Computes a hash code for this delta.
         * @return Computed hash code.
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.from, this.to);
        }
    }
}
