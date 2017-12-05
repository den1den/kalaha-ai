package marblegame.players;

import marblegame.gamemechanics.Match;
import net.PlayClient;

public class NetworkPlayer extends AutomaticPlayer {

    private PlayClient client;

    public NetworkPlayer(String host, Match match) {
        super(host, match);
        client = new PlayClient(host);
    }

    @Override
    protected int calcMove() {
        return client.getResponse(match);
    }

    public boolean isConnected() {
        return client.isConnected();
    }
}
