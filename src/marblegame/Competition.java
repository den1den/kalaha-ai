package marblegame;

import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.PossibleMoveIterator;
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

    public boolean canMove(int moveIndex) {
        return match.canMove(moveIndex);
    }

    public int[] getFields() {
        return match.getFieldsCopy();
    }

    public int[] getPoints() {
        return match.getPoints();
    }

    public boolean canPlay() {
        return PossibleMoveIterator.from(match).hasNext();
    }
}
