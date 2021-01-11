package elkaproj;

import java.util.Iterator;

/**
 * Casts a sequence because apparently Java can't.
 *
 * @param <T> Cast type.
 * @param <U> Original type.
 */
public class CastingIterator<T, U extends T> implements Iterator<T> {

    private final Iterator<U> originalIterator;

    /**
     * Creates a new iterator from an interable.
     *
     * @param iterable Iterable to iterate over.
     */
    public CastingIterator(Iterable<U> iterable) {
        this.originalIterator = iterable.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.originalIterator.hasNext();
    }

    @Override
    public T next() {
        return this.originalIterator.next();
    }
}
