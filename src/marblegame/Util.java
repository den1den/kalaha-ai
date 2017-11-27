package marblegame;

import org.json.simple.JSONArray;

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

    public static boolean isLenovo() {
        return "dennis".equals(System.getenv("USER"));
    }
}
