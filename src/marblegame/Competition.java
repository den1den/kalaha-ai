package marblegame;

import marblegame.players.Player;

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
        int move = player.getMove();
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
}
