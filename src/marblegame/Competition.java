package marblegame;

import marblegame.players.Player;

import java.util.Arrays;

public class Competition {
    private Match match;
    private Player[] players;
    private int moves = 0;
    private int lastMove = -1;

    public Competition(Match match, Player... players) {
        this.match = match;
        this.players = players;
    }

    public Match getMatch() {
        return match;
    }

    public Player[] getPlayers() {
        return players;
    }

    int getMoves() {
        return moves;
    }

    int isFinished() {
        return match.isFinished();
    }

    public int move() {
        int turn = getTurn();
        Player player = players[turn];
        return move(player.getMove());
    }

    public int move(int move) {
        int gain = match.move(move);
        this.lastMove = move;
        this.moves++;
        return gain;
    }

    public int getLastMove() {
        return lastMove;
    }

    public int getTurn() {
        return match.getTurn();
    }

    public boolean isInRange(int moveIndex) {
        return match.isInRange(moveIndex);
    }

    public int[] getFields() {
        return Arrays.copyOf(match.getBoardState().fields, match.getBoardState().fields.length);
    }

    public int calcWinner() {
        return match.calcWinner();
    }

    public int[] getPoints() {
        return match.boardState.points;
    }

    public boolean canPlay() {
        return Match.AvailableMoveIterator.from(match).hasNext();
    }
}
