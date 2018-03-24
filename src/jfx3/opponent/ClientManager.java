package jfx3.opponent;

import jfx3.App;
import marblegame.gamemechanics.Match;
import marblegame.solvers.PlayClient;

public class ClientManager extends ConnectionService<PlayClient> {
    private Match match = null;

    public void setOpponent(App.NetworkOpponent opponent) {
        cancel();
        setNextObj(new PlayClient(opponent.getHost()));
        match = null;
        restart();
    }

    @Override
    protected ConnectionService<PlayClient>.Task createTaskImpl() {
        return new ConnectionService<PlayClient>.Task() {

            @Override
            protected Result<PlayClient> callImpl(PlayClient object) throws Exception {
                if (match != null) {
                    // Query the new connection
                    int result = object.solve(match);
                    return sendResult(result);
                } else if (object != null) {
                    // No match, but we do have a current client
                    object.ensureOpenSocket();
                }
                return null;
            }
        };
    }
}
