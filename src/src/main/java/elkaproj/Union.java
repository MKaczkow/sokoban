package elkaproj;

/**
 * A tagged union of 2 types.
 *
 * @param <T1> First type of the union.
 * @param <T2> Second type of the union.
 */
public class Union<T1, T2> {

    private final Object value;
    private final Class<?> actualType;
    private final Class<T1> type1;
    private final Class<T2> type2;

    /**
     * Creates a new union of specified types and with specified value.
     *
     * @param type1 First type of the union.
     * @param type2 Second type of the union.
     * @param value Value to contain in the union. Must be of a type assignable to either T1 or T2, but not both.
     */
    public Union(Class<T1> type1, Class<T2> type2, Object value) {
        if (value == null)
            throw new IllegalArgumentException("Value cannot be null.");

        this.type1 = type1;
        this.type2 = type2;

        boolean a1 = type1.isAssignableFrom(value.getClass());
        boolean a2 = type2.isAssignableFrom(value.getClass());

        if (!(a1 || a2))
            throw new IllegalArgumentException("Value is of neither of the union's types.");

        if (a1 && a2)
            throw new IllegalArgumentException("Value is of type ambiguous between both T1 and T2.");

        this.actualType = a1 ? this.type1 : this.type2;
        this.value = value;
    }

    /**
     * Gets the first type of the union.
     *
     * @return First type of the union.
     */
    public Class<T1> getType1() {
        return this.type1;
    }

    /**
     * Gets the second type of the union.
     *
     * @return Second type of the union.
     */
    public Class<T2> getType2() {
        return this.type2;
    }

    /**
     * Gets whether contained type is of type T1.
     *
     * @return Whether contained type is of type T1.
     */
    public boolean isType1() {
        return this.actualType == this.type1;
    }

    /**
     * Gets whether contained type is of type T2.
     *
     * @return Whether contained type is of type T2.
     */
    public boolean isType2() {
        return this.actualType == this.type2;
    }

    /**
     * Unwraps the contained value as value of type T1, if possible.
     *
     * @return Unwrapped value.
     * @throws IllegalAccessException Value was not a value of type T1.
     */
    public T1 unwrapType1() throws IllegalAccessException {
        if (this.actualType != this.type1)
            throw new IllegalAccessException("Contained value is not of type T1.");

        return (T1) this.value;
    }

    /**
     * Unwraps the contained value as value of type T2, if possible.
     *
     * @return Unwrapped value.
     * @throws IllegalAccessException Value was not a value of type T2.
     */
    public T2 unwrapType2() throws IllegalAccessException {
        if (this.actualType != this.type2)
            throw new IllegalAccessException("Contained value is not of type T2.");

        return (T2) this.value;
    }
}
