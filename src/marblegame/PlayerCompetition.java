package marblegame;

import marblegame.gamemechanics.Competition;
import marblegame.gamemechanics.Match;
import marblegame.players.Player;

public class PlayerCompetition extends Competition {
    private Player[] players;

    public PlayerCompetition(Match match, Player... players) {
        super(match);
        this.players = players;
    }

    public Player[] getPlayers() {
        return players;
    }

    /**
     * Let the next player do the next moveNow
     *
     * @return
     */
    public int move() {
        int turn = getTurn();
        Player player = players[turn];
        return move(player.getMove());
    }
}
