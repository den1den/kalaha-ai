package marblegame;

import marblegame.players.Player;

import java.util.Arrays;

public class MatchBuilder {
    private BoardState board;
    private int startAmount = 4;
    private int fieldsPerPlayer = 6;
    private int targetAmount = 3;
    private int target2Amount = 2;
    private Player[] players;

    public MatchBuilder(int players) {
        this.players = new Player[players];
    }

    public MatchBuilder(Player... players) {
        this.players = players;
    }

    public MatchBuilder setBoard(BoardState board) {
        this.board = board;
        return this;
    }

    public MatchBuilder setPlayers(int players) {
        this.players = new Player[players];
        return this;
    }

    public MatchBuilder setPlayers(Player... players) {
        this.players = players;
        return this;
    }

    public Match createMatch() {
        if (players == null) {
            throw new IllegalStateException("The players should be set");
        }
        if (board == null)
            board = new BoardState(
                    fill(startAmount, fieldsPerPlayer * players.length),
                    fill(0),
                    0
            );
        int[] startFields = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            startFields[i] = i * fieldsPerPlayer;
        }
        int[] endFields = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            endFields[i] = (i + 1) * fieldsPerPlayer - 1;
        }
        return new Match(board, startFields, endFields, fill(targetAmount), fill(target2Amount));
    }

    private int[] fill(int val) {
        return fill(val, players.length);
    }

    private int[] fill(int val, int size) {
        int[] arr = new int[size];
        Arrays.fill(arr, val);
        return arr;
    }

    public Match createComputerWinMatch() {
        if (players == null) {
            throw new IllegalStateException("The players should be set");
        }
        if (board == null)
            board = new BoardState(
                    new int[]{
                            2, 2, 2, 2, 2, 2,
                            4, 4, 4, 4, 4, 1
                    },
                    fill(0),
                    0
            );
        int[] startFields = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            startFields[i] = i * fieldsPerPlayer;
        }
        int[] endFields = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            endFields[i] = (i + 1) * fieldsPerPlayer - 1;
        }
        return new Match(board, startFields, endFields, fill(targetAmount), fill(target2Amount));
    }

    public Match createHumanWonMatch() {
        Match m = createMatch();
        m.boardState.points[0] = 1000;
        return m;
    }

    public Match createHumanWinMatch() {
        Match m = createMatch();
        m.boardState = new BoardState(
                new int[]{
                        4, 4, 4, 4, 4, 1,
                        2, 2, 2, 2, 2, 2
                },
                fill(0),
                0
        );
        return m;
    }
}