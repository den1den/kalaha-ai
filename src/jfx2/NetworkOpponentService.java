package jfx2;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import marblegame.gamemechanics.Match;
import marblegame.solvers.PlayClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

class NetworkOpponentService extends ScheduledService<NetworkOpponentService.OpponentResult> {
    private final NetworkMatch gui;
    private OpponentResult lastResult = null;
    private String host;
    private Match match;

    private ExecutorService networkService = Executors.newFixedThreadPool(1);

    NetworkOpponentService(NetworkMatch gui) {
        this.gui = gui;
        setExecutor(networkService);
    }

    /**
     * The next start will only connect
     */
    void setConnectToNewHost(String host) {
        cancel();
        this.host = host;
        this.lastResult = null;
    }

    /**
     * The next start will either request a new move
     */
    void setRequestMove(Match match, Consumer<Integer> onSuccess) {
        cancel();
        this.match = match;
        setOnSucceeded(event -> {
            lastResult = getValue();
            NetworkOpponentService.this.match = null;
            onSuccess.accept(lastResult.move);
        });
    }

    @Override
    protected Task<OpponentResult> createTask() {
        PlayClient client;
        if (lastResult == null) {
            client = new PlayClient(host);
        } else {
            client = lastResult.playClient;
        }
        if (match == null) {
            // Only connect
            gui.onConnecting();
            return new ConnectTask(client);
        } else {
            gui.onWaitingForNetwork();
            return new ExecuteMoveTask(client, match);
        }
    }

    @Override
    protected void succeeded() {
        lastResult = getValue();
        if (lastResult.playClient.isConnected()) {
            gui.onConnectedTo(host);
        } else {
            gui.onCannotConnectTo(host, getException());
        }
        setOnSucceeded(null);
        cancel();
    }

    @Override
    protected void failed() {
        gui.onCannotConnectTo(host, getException());
    }

    void close() {
        cancel();
        networkService.shutdown();
    }

    /**
     * Task to only connect to some server
     */
    private static class ConnectTask extends Task<OpponentResult> {
        final PlayClient client;

        ConnectTask(PlayClient client) {
            this.client = client;
        }

        @Override
        protected OpponentResult call() throws Exception {
            client.connectImpl();
            return new OpponentResult(client, -1);
        }
    }

    /**
     * Task to ask for a move over the network
     */
    private static class ExecuteMoveTask extends Task<OpponentResult> {
        final PlayClient client;
        final Match match;

        ExecuteMoveTask(PlayClient client, Match match) {
            this.client = client;
            this.match = match;
        }

        @Override
        protected OpponentResult call() throws Exception {
            int move = client.getResponseImpl(match);
            System.out.println("ExecuteMoveTask finished: move = " + move);
            return new OpponentResult(client, move);
        }
    }

    static class OpponentResult {
        final PlayClient playClient;
        final int move;

        public OpponentResult(PlayClient playClient, int move) {
            this.playClient = playClient;
            this.move = move;
        }

        @Override
        public String toString() {
            return playClient.toString() + ", " + this.move;
        }
    }
}
