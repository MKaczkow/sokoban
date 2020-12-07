package elkaproj.kvcreader;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KVCReader<T> implements Closeable {

    private final InputStream inputStream;
    private final Class<T> type;

    public KVCReader(InputStream inputStream, Class<T> type) {
        this.inputStream = inputStream;
        this.type = type;
    }

    public T readObject() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Constructor<T> ctor = (Constructor<T>)Arrays.stream(this.type.getDeclaredConstructors())
                .filter(x -> x.getParameterCount() == 0)
                .findFirst()
                .orElse(null);

        if (ctor == null)
            throw new NoSuchMethodException("Specified type has no usable constructor.");

        ctor.setAccessible(true);
        T t = ctor.newInstance();

        Map<String, Field> fields = new HashMap<String, Field>();
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

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    private static class StringPair {

        public String first, second;
    }

    private static class KVCReaderStateMachine implements Iterable<StringPair> {

        private final InputStream inputStream;

        public KVCReaderStateMachine(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public Iterator<StringPair> iterator() {
            return new KVCIterator(this.inputStream);
        }

        private static class KVCIterator implements Iterator<StringPair> {

            private final BufferedReader reader;
            private String next;

            public KVCIterator(InputStream inputStream) {
                this.reader = new BufferedReader(new InputStreamReader(inputStream));
            }

            @Override
            public boolean hasNext() {
                try {
                    this.next = this.reader.readLine();
                    return next != null && next.length() != 0 && !next.startsWith(".") && next.contains("=");
                } catch (Exception ex) {
                    return false;
                }
            }

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
