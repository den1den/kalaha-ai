package marblegame.gamemechanics;

import marblegame.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;

/**
 * Defines a board
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

    /**
     * Copy constructor
     *
     * @param boardState original
     */
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

    /**
     * @return the number of points that can still be scored
     */
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
        StringBuilder r = new StringBuilder("BoardView ")
            .append(super.toString())
            .append(System.lineSeparator())
            .append("       ")
            .append(toStringPlayer(0));
        for (int i = 1; i < this.points.length; i++) {
            r.append(System.lineSeparator()).append("       ").append(toStringPlayer(i));
        }
        return r.toString();
    }

    public String toStringWithTurn() {
        StringBuilder r = new StringBuilder("BoardView: ");
        if (turn == 0) {
            r.append("-> ");
        } else {
            r.append("   ");
        }
        r.append(toStringPlayer(0));
        for (int i = 1; i < this.points.length; i++) {
            r.append(System.lineSeparator());
            if (turn == i) {
                r.append("-> ");
            } else {
                r.append("   ");
            }
            r.append("       ").append(toStringPlayer(i));
        }
        return r.toString();
    }

    public int[] getFieldsCopy() {
        return Arrays.copyOf(fields, fields.length);
    }

    public int[] getPointsCopy() {
        return Arrays.copyOf(points, points.length);
    }

    public int getPointsOfPlayer() {
        return points[turn];
    }

    public int getPlayerPoints(int player) {
        return points[player];
    }

    public int getPlayerPoints() {
        return points[turn];
    }

    public int getNFields() {
        return fields.length;
    }

    public int getTurn() {
        return turn;
    }

    public int getPrevTurn() {
        if (turn == 0) {
            return this.points.length - 1;
        } else {
            return turn - 1;
        }
    }

    public int getOverallMaximalPoints() {
        int MAX = 0;
        for (int i = 0; i < points.length; i++) {
            MAX += points[i];
        }
        for (int i = 0; i < fields.length; i++) {
            MAX += fields[i];
        }
        return MAX;
    }

    public int getFields(int index) {
        return this.fields[index];
    }

    static class Serializer {
        static JSONObject toJson(BoardState boardState) {
            JSONObject r = new JSONObject();
            r.put("fields", Util.jsonToArray(boardState.fields));
            r.put("points", Util.jsonToArray(boardState.points));
            r.put("turn", boardState.turn);
            return r;
        }

        static BoardState fromJSONObject(JSONObject object) {
            return new BoardState(
                    Util.jsonToArray((JSONArray) object.get("fields")),
                    Util.jsonToArray((JSONArray) object.get("points")),
                    Math.toIntExact((Long) object.get("turn"))
            );
        }
    }
}
