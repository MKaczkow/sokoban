package elkaproj.kvcreader;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Reads data from a Key-Value Configuration stream to objects of specified type.
 * @param <T> Type of object to read into.
 */
public class KVCReader<T> implements Closeable {

    private final InputStream inputStream;
    private final Class<T> type;

    /**
     * Creates a new Key-Value Configuration reader for specified types.
     * @param inputStream Stream to read from.
     * @param type Object type to read into.
     */
    public KVCReader(InputStream inputStream, Class<T> type) {
        this.inputStream = inputStream;
        this.type = type;
    }

    /**
     * Reads a single object from the stream.
     * @return Read object.
     * @throws NoSuchMethodException Specified type has no usable constructor. A constructor with no arguments is required.
     * @throws IllegalAccessException Couldn't access one or more fields for the purposes of writing values.
     * @throws InvocationTargetException Couldn't instantiate the object because the constructor threw an exception.
     * @throws InstantiationException Failed to instantiate the object. For more details consult the contents of the exception.
     * @throws NoSuchFieldException Specified serialized field has no POJO equivalent on the object.
     */
    public T readObject() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Constructor<T> ctor = (Constructor<T>)Arrays.stream(this.type.getDeclaredConstructors())
                .filter(x -> x.getParameterCount() == 0)
                .findFirst()
                .orElse(null);

        if (ctor == null)
            throw new NoSuchMethodException("Specified type has no usable constructor.");

        ctor.setAccessible(true);
        T t = ctor.newInstance();

        Map<String, Field> fields = new HashMap<>();
        Arrays.stream(this.type.getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(KVCName.class))
                .forEach(x -> fields.put(x.getAnnotation(KVCName.class).name(), x));

        for (StringPair sp : new KVCReaderStateMachine(this.inputStream)) {
            Field f = fields.getOrDefault(sp.first, null);
            if (f == null)
                throw new NoSuchFieldException("Field with specified name ('" + sp.first + "') does not exist.");

            f.setAccessible(true);
            f.set(t, KVCValueConverter.convert(sp.second, f.getType()));
        }

        return t;
    }

    /**
     * Closes this reader and the underlying stream.
     * @throws IOException Exception occurred during closing the underlying stream.
     */
    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    /**
     * A string-string value pair.
     */
    private static class StringPair {

        /**
         * First of the values.
         */
        public String first;

        /**
         * Second of the values.
         */
        public String second;
    }

    /**
     * Iterator over the stream contents. Returns a collection of string-string pairs until an end marker (.) or end-of-stream is encountered.
     */
    private static class KVCReaderStateMachine implements Iterable<StringPair> {

        private final InputStream inputStream;

        /**
         * Creates a new state machine to iterate over stream contents.
         * @param inputStream Stream to read from.
         */
        public KVCReaderStateMachine(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        /**
         * Creates an iterator over stream contents.
         * @return Iterator over stream contents.
         */
        @Override
        public Iterator<StringPair> iterator() {
            return new KVCIterator(this.inputStream);
        }

        /**
         * Iterates over stream contents, returning a stream of string-string pairs for consumption by the Key-Value Configuration reader.
         */
        private static class KVCIterator implements Iterator<StringPair> {

            private final BufferedReader reader;
            private String next;

            /**
             * Creates a new iterator for given stream.
             * @param inputStream Stream to iterate over.
             */
            public KVCIterator(InputStream inputStream) {
                this.reader = new BufferedReader(new InputStreamReader(inputStream));
            }

            /**
             * Checks whether a string-string pair is available in the stream. If it is, fetches it, and buffers its raw form.
             * @return Whether a string-string pair is available.
             */
            @Override
            public boolean hasNext() {
                try {
                    this.next = this.reader.readLine();
                    return next != null && next.length() != 0 && !next.startsWith(".") && next.contains("=");
                } catch (Exception ex) {
                    return false;
                }
            }

            /**
             * Parses the next available string-string pair and returns it, if one is available.
             * @return Parsed string-string pair.
             */
            @Override
            public StringPair next() {
                String f, s;
                String[] kv = this.next.split("=", 2);
                f = kv[0].trim();
                s = kv[1].trim();
                StringPair sp = new StringPair();
                sp.first = f;
                sp.second = s;
                return sp;
            }
        }
    }
}
