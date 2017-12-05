package jfx2;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import marblegame.Competition;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;
import marblegame.players.Player;
import marblegame.players.SimplePlayer;
import net.PlayClient;
import net.PlayServer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HumanAgainstNetworkMatch extends AnimatedTwoPlayerVis {
    private static final ExecutorService networkWorkerExecutor = Executors.newSingleThreadExecutor();
    public Button retryButton;
    public ChoiceBox<String> opponentSelector;
    private SimplePlayer humanPlayer;
    private PlayClient networkPlayer;
    private Competition competition;
    private ConnectService connectService = new ConnectService();
    private RequestService requestService = new RequestService();

    public HumanAgainstNetworkMatch() {
        connectService.setExecutor(networkWorkerExecutor);
        requestService.setExecutor(networkWorkerExecutor);
        requestService.setBackoffStrategy(ScheduledService.EXPONENTIAL_BACKOFF_STRATEGY);

        connectService.setOnFailed((event) -> {
            System.err.println("connectService should not be able to fail, only manually retried on success");
        });
        requestService.setOnFailed((event -> {
            retryButton.setVisible(true);
            opponentText.setText("Connection FAIL, trying again...");
        }));
    }

    private void setCompetition() {
        Match match;
        final Player[] players;
        players = new Player[2];
        players[0] = getRightPlayer();
        match = new MatchBuilder(players.length).createMatch();
        match = new MatchBuilder(players.length).createComputerWinMatch();
        match = new MatchBuilder(players.length).createHumanWinMatch();
        // match = new MatchBuilder(players.length).createHumanWonMatch();
        competition = new Competition(match, players);
        // Set labels
        setFields(competition.getFields(), competition.getPoints());
    }

    @Override
    protected void initialize() {
        setRightPlayer(new SimplePlayer("Human"));
        super.initialize();

        setCompetition();

        // setup opponent selector
        opponentSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Cancel previous setup
            connectService.cancel();
            connectService.reset();

            // Do new setup
            final String host = newValue;
            connectService.setHost(host);
            connectService.setOnSucceeded(event -> {
                PlayClient result = (PlayClient) event.getSource().getValue();
                setLeftPlayer(result);
                if (result.isConnected()) {
                    opponentText.setText("Connected to " + host);
                } else {
                    opponentText.setText("Could not connect to " + host);
                }
            });
            connectService.start();
        });
        opponentSelector.getItems().addAll(
                "vandenbrand.eu",
                "localhost"
        );
        opponentSelector.getSelectionModel().selectFirst();
    }

    @Override
    void clickedField(int index) {
        if (competition.canMove(index)) { // competition.canSet
            clickMove(index);
        } else {
            unClickMove();
        }
    }

    private void unClickMove() {
        selectedMove.set(-1);
    }

    private void clickMove(int index) {
        selectedMove.set(index);
    }

    public void doMove(ActionEvent actionEvent) {
        getRightPlayer().setMove(selectedMove.get());
        executeMove();
    }

    protected void executeMove() {
        if (!executing.compareAndSet(false, true)) {
            System.err.println("Still executing");
            return;
        }
        if (getRightPlayer().getMove() == -1) {
            System.err.println("No move set");
            executing.set(false);
            return;
        }
        if (getLeftPlayer() == null) {
            System.err.println("No opponent set");
            executing.set(false);
            return;
        }

        // Do human move
        final int[] preHumanBoardState = competition.getFields();
        int gain = competition.move();
        int humanMove = competition.getLastMove();
        // See if human has won
        final String winMsg;
        if (gain == Match.MOVE_RESULT_WIN) {
            winMsg = "Human has won the game !!! :D";
        } else if (!competition.canPlay()) {
            winMsg = "Human has won the game by pad";
        } else {
            winMsg = null;
        }

        // Start human animation
        animatorService.cancel();
        animatorService.reset();
        animatorService.setTask(new MoveAnimationTask(selectedMove, humanMove, false, preHumanBoardState,
                competition.getFields(), competition.getPoints()
        ));
        if (winMsg != null) {
            animatorService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    statusTextLeft.setText(winMsg);
                    executing.set(false);
                }
            });
            animatorService.start();
            return;
        } else {
            animatorService.start();
        }

        // Start network request in the background
        final int[] preNetworkBoardState = competition.getFields();
        requestService.cancel();
        requestService.reset();
        requestService.setOnSucceeded(event -> {
            System.out.println("requestService.onSucceeded");
            opponentText.setText("Connection OK");
            retryButton.setVisible(false);

            final int networkMove = (int) event.getSource().getValue();

            // After the human animation follows the network animation
            EventHandler<WorkerStateEvent> onHumanAnimationCompleted = event1 -> {
                try {
                    competition.move(networkMove);
                    animatorService.setTask(new MoveAnimationTask(
                            opponentSelectedMove, networkMove, true, preNetworkBoardState,
                            competition.getFields(), competition.getPoints()));
                    animatorService.reset();
                    animatorService.setOnSucceeded(e -> {
                        executing.set(false);
                    });
                    animatorService.start();
                } catch (Error e) {
                    e.printStackTrace();
                    executing.set(false);
                }
            };
            // Check if task should be scheduled or executed now
            if (animatorService.isRunning()) {
                animatorService.setOnSucceeded(onHumanAnimationCompleted);
            } else {
                onHumanAnimationCompleted.handle(null);
            }
        });
        requestService.start();
    }

    SimplePlayer getRightPlayer() {
        return humanPlayer;
    }

    void setRightPlayer(SimplePlayer humanPlayer) {
        this.humanPlayer = humanPlayer;
    }

    PlayClient getLeftPlayer() {
        return networkPlayer;
    }

    void setLeftPlayer(PlayClient networkPlayer) {
        this.networkPlayer = networkPlayer;
    }

    public void startLocalServerPressed(ActionEvent actionEvent) {
        new Thread(PlayServer.runnable(), "Local server").start();
    }

    public void restartClicked(ActionEvent actionEvent) {
        if (executing.get()) {
            System.err.println("Not possible while executing");
            return;
        }
        setCompetition();
    }

    public void clickedRetry(ActionEvent actionEvent) {
        requestService.restart();
        //System.out.println("executing = " + executing);
        //requestService.cancel();
        //requestService.reset();
        //requestService.getOnSucceeded();
        //requestService.start();
    }

    @Override
    public void close() {
        networkWorkerExecutor.shutdown();
    }

    private class ConnectService extends Service<PlayClient> {
        String host;

        private void setHost(String host) {
            this.host = host;
        }

        @Override
        protected Task<PlayClient> createTask() {
            final String host = this.host;
            return new Task<PlayClient>() {
                @Override
                protected PlayClient call() throws Exception {
                    PlayClient client = new PlayClient(host);
                    try {
                        client.connectImpl();
                    } catch (Exception e) {
                        System.err.println("ConnectService.task failed to host " + host + ": " + e);
                    }
                    return client;
                }
            };
        }
    }

    private class RequestService extends ScheduledService<Integer> {
        @Override
        protected Task<Integer> createTask() {
            final PlayClient player = getLeftPlayer();
            final Match match = competition.getMatch();
            return new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    try {
                        int move = player.getResponseImpl(match);
                        if (move == -1) {
                            throw new Exception("Could not get move of " + player);
                        }
                        return move;
                    } catch (IOException e) {
                        System.err.println("RequestService.task failed: " + e);
                        //e.printStackTrace();
                        throw e;
                    }
                }
            };
        }

        @Override
        protected void succeeded() {
            this.cancel();
            // this.reset();
            super.succeeded();
        }
    }
}
