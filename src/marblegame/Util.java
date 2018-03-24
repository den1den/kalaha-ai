package marblegame;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import talf.mechanics.Coordinate;
import talf.mechanics.Move;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class Util {
    public static JSONArray toArray(int[] ints) {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < ints.length; i++) {
            arr.add(ints[i]);
        }
        return arr;
    }

    public static int[] toArray(JSONArray arr) {
        int[] ints = new int[arr.size()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = Math.toIntExact((Long) arr.get(i));
        }
        return ints;
    }

    public static int sum(boolean... bools) {
        int s = 0;
        for (int i = 0; i < bools.length; i++) {
            if (bools[i])
                s += 1;
        }
        return s;
    }

    public static <E> List<E> toList(Iterator<E> iterator) {
        ArrayList<E> l = new ArrayList<>();
        while (iterator.hasNext()) {
            l.add(iterator.next());
        }
        return l;
    }

    public static void deepCopy(byte[][] src, byte[][] dst) {
        assert src.length == dst.length;
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
        }
    }

    public static <K, V> Iterable<Pair<K, V>> flatten(Map<K, ? extends Collection<V>> map) {
        return pairIterable(map);
    }

    public static <K, V> Iterable<Pair<K, V>> pairIterable(Map<K, ? extends Collection<V>> map) {
        return new Iterable<Pair<K, V>>() {
            @Override
            public Iterator<Pair<K, V>> iterator() {
                return new Iterator<Pair<K, V>>() {
                    Iterator<K> ki = map.keySet().iterator();
                    Iterator<V> vi;
                    K k;
                    V v;

                    {
                        findNext();
                    }

                    @Override
                    public boolean hasNext() {
                        return v != null;
                    }

                    @Override
                    public Pair<K, V> next() {
                        Pair<K, V> r = new Pair<>(k, v);
                        findNext();
                        return r;
                    }

                    private void findNext() {
                        while (vi == null || !vi.hasNext()) {
                            if (ki.hasNext()) {
                                k = ki.next();
                                vi = map.get(k).iterator();
                            } else {
                                v = null;
                                return;
                            }
                        }
                        v = vi.next();
                    }
                };
            }
        };
    }

    public static byte[][] deepCopy(byte[][] arr) {
        byte[][] dest = new byte[arr.length][];
        for (int i = 0; i < dest.length; i++) {
            dest[i] = new byte[arr[i].length];
        }
        deepCopy(arr, dest);
        return dest;
    }

    public static Iterable<Move> pairIterableAsMove(Map<Coordinate, List<Coordinate>> moves) {
        return Move.asMove(pairIterable(moves));
    }

    public static <K, V> void putSorted(SortedMap<K, List<V>> map, K key, V value) {
        List<V> vs = map.get(key);
        if (vs == null) {
            vs = new ArrayList<>();
            map.put(key, vs);
        }
        vs.add(value);
    }

    public static <V> Iterator<V> chain(Iterator<V> i1, Iterator<V> i2) {
        return new Iterator<V>() {
            @Override
            public boolean hasNext() {
                if (i1.hasNext()) return true;
                return i2.hasNext();
            }

            @Override
            public V next() {
                if (i1.hasNext()) {
                    return i1.next();
                }
                return i2.next();
            }
        };
    }

    public static int[] createIntArray(int length, int fill) {
        int[] a = new int[length];
        Arrays.fill(a, fill);
        return a;
    }

    public static <T> T[] multipleRefElements(T[] ts, int[] amounts) {
        ArrayList<T> list = new ArrayList<>();
        for (int i = 0; i < ts.length; i++) {
            for (int j = 0; j < amounts[i]; j++) {
                list.add(ts[i]);
            }
        }
        return (T[]) list.toArray();
    }

    public static <T> T[][] deepCopy(T[][] tss) {
        Class<Object[]> type = Object[].class;
        T[][] r = (T[][]) Array.newInstance(type, tss.length);
        for (int i = 0; i < tss.length; i++) {
            r[i] = Arrays.copyOf(tss[i], tss[i].length);
        }
        return r;
    }

    public static boolean[][] deepCopy(boolean[][] a) {
        boolean[][] result = new boolean[a.length][];
        for (int i = 0; i < a.length; i++) {
            result[i] = Arrays.copyOf(a[i], a[i].length);
        }
        return result;
    }

    public static <T> void swap(T[] arr, int i0, int i1) {
        T el = arr[i0];
        arr
    }

    public static Writer getFileWriter(String filename) throws IOException {
        Path path = Paths.get(filename);
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        Files.createFile(path);
        return Files.newBufferedWriter(path);
    }

    public static <X> ChangeListener<X> onNew(Consumer<X> xConsumer) {
        return new ChangeListener<X>() {
            @Override
            public void changed(ObservableValue<? extends X> observable, X oldValue, X newValue) {
                xConsumer.accept(newValue);
            }
        };
    }

    public static class JsonObj extends JSONObject {
        public static JsonObj o() {
            return new JsonObj();
        }

        public JsonObj o(Object key, Object value) {
            this.put(key, value);
            return this;
        }
    }

    public static boolean isLenovo() {
        return "dennis".equals(System.getenv("USER"));
    }

    public static class JsonArr extends JSONArray {
        public static JsonArr a() {
            return new JsonArr();
        }

        public JsonArr a(Object value) {
            this.add(value);
            return this;
        }
    }

    public static class Pairs<V> {
        private final V first, second;

        public Pairs(V first, V second) {
            this.first = first;
            this.second = second;
        }

        public V getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }

        public boolean hasTwo() {
            return first != null && second != null;
        }
    }

    public static class IteratorAllCombinations<T> implements Iterator<T[]> {
        T[] arr;
        int a, b;

        public IteratorAllCombinations(T[] arr) {
            this.arr = arr;
        }

        @Override
        public boolean hasNext() {
            return a <= arr.length;
        }

        @Override
        public T[] next() {
            T[] next = Arrays.copyOf(arr, arr.length);
            b++;
            if (b >= arr.length) {
                a++;
                b = a + 1;
            }
            return null;
        }
    }
}
