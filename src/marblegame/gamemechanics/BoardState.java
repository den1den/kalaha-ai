package marblegame.gamemechanics;

import marblegame.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;

/**
 * Created by dennis on 2-3-17.
 */
public class BoardState {
    final int[] fields;
    final int[] points;
    int turn;

    public BoardState(int[] fields, int[] points) {
        this(fields, points, 0);
    }

    public BoardState(int[] fields, int[] points, int turn) {
        this.fields = fields;
        this.points = points;
        this.turn = turn;
    }

    public BoardState(BoardState boardState) {
        this(
                Arrays.copyOf(boardState.fields, boardState.fields.length),
                Arrays.copyOf(boardState.points, boardState.points.length),
                boardState.turn
        );
    }

    int getMaxOtherPlayerPoints(int player) {
        int max = -1;
        for (int i = 0; i < points.length; i++) {
            if (i != player) {
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

    public int[] getAllPoints() {
        return points;
    }

    public int getPoints() {
        return points[turn];
    }

    int getPlayerPoints(int player) {
        return points[player];
    }

    int getPlayerPoints() {
        return points[turn];
    }

    public int getNFields() {
        return fields.length;
    }

    static class Serializer {
        static JSONObject toJson(BoardState boardState) {
            JSONObject r = new JSONObject();
            r.put("fields", Util.toArray(boardState.fields));
            r.put("points", Util.toArray(boardState.points));
            r.put("turn", boardState.turn);
            return r;
        }

        static BoardState fromJSONObject(JSONObject object) {
            return new BoardState(
                    Util.toArray((JSONArray) object.get("fields")),
                    Util.toArray((JSONArray) object.get("points")),
                    Math.toIntExact((Long) object.get("turn"))
            );
        }
    }
}
