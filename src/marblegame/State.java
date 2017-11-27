package marblegame;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;

/**
 * Created by dennis on 2-3-17.
 */
public class State {
    int[] fields;
    int[] points;
    int turn;

    State(int[] fields, int[] points) {
        this(fields, points, 0);
    }

    public State(int[] fields, int[] points, int turn) {
        this.fields = fields;
        this.points = points;
        this.turn = turn;
    }

    public State(State boardState) {
        this(
                Arrays.copyOf(boardState.fields, boardState.fields.length),
                Arrays.copyOf(boardState.points, boardState.points.length),
                boardState.turn
        );
    }

    int getMaxOtherPlayerPoints() {
        int max = -1;
        for (int i = 0; i < points.length; i++) {
            if (i != turn) {
                if (points[i] > max) {
                    max = points[i];
                }
            }
        }
        return max;
    }

    int remainingPoints() {
        int sum = 0;
        for (int i = 0; i < fields.length; i++) {
            sum += fields[i];
        }
        return sum;
    }

    String toStringPlayer(int player, int move) {
        StringBuilder r = new StringBuilder();
        int fieldsPerPlayer = fields.length / this.points.length;
        for (int i = player * fieldsPerPlayer; i < (player + 1) * fieldsPerPlayer; i++) {
            if (i == move) {
                r.append(String.format("_%2d_, ", this.fields[i]));
            } else {
                r.append(String.format("<%2d>, ", this.fields[i]));
            }
        }
        r.append("{").append(this.points[player]).append("}");
        return r.toString();
    }

    public String toStringPlayerIndices(int player) {
        StringBuilder r = new StringBuilder();
        int fieldsPerPlayer = fields.length / this.points.length;
        for (int i = player * fieldsPerPlayer; i < (player + 1) * fieldsPerPlayer; i++) {
            r.append(String.format("<%2d>, ", i));
        }
        return r.toString();
    }

    public String toStringPlayer(int player) {
        return toStringPlayer(player, -1);
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder("Board: ").append(toStringPlayer(0));
        for (int i = 1; i < this.points.length; i++) {
            r.append(System.lineSeparator()).append("       ").append(toStringPlayer(i));
        }
        return r.toString();
    }

    public int[] getFields() {
        return fields;
    }

    public int getPoints() {
        return points[turn];
    }

    int getPlayerPoints() {
        return points[turn];
    }

    static class Serializer {
        static JSONObject toJson(State state) {
            JSONObject r = new JSONObject();
            r.put("fields", Util.toArray(state.fields));
            r.put("points", Util.toArray(state.points));
            r.put("turn", state.turn);
            return r;
        }

        static State fromJSONObject(JSONObject object) {
            return new State(
                    Util.toArray((JSONArray) object.get("fields")),
                    Util.toArray((JSONArray) object.get("points")),
                    Math.toIntExact((Long) object.get("turn"))
            );
        }
    }
}
