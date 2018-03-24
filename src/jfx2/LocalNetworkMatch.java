package jfx2;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import marblegame.gamemechanics.Competition;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;

import java.util.LinkedList;

public class LocalNetworkMatch extends AnimatedTwoPlayerVis implements NetworkMatch {

    // Status texts
    private static final String ANIMATING_PLAYER_TURN_MSG = "Executing your turn";
    private static final String CONNECTED_TO_MSG = "Move received";
    private static final String PLAYER_TURN_MSG = "Your turn";
    private static final String EXECUTING_OPPONENT_TURN_MSG = "Executing opponent turn";
    private static final String WAITING_FOR_OPPONENT_TURN_MSG = "Waiting for opponent turn";
    private final ObservableList<String> statusMessages = FXCollections.observableList(new LinkedList<String>());
    private StatusMessageUpdater statusMessageUpdater;

    @Override
    public void setFields(Match m) {
        System.out.println("m.getBoardState().getTurn() = " + m.getBoardState().getTurn());
        if (statusMessages.isEmpty() || !PLAYER_TURN_MSG.equals(statusMessages.get(0))) {
            statusMessages.add(PLAYER_TURN_MSG);
        }
        super.setFields(m);
    }

    // Buttons
    public Button retryButton;
    public ChoiceBox<OpponentChoice> opponentSelector;
    public Text opponentText;
    public TextField playerNameTextField;
    public ProgressIndicator connectingProgressIndicator;
    private Competition competition;
    /**
     * Service which is used to send network requests
     */
    private NetworkOpponentService networkOpponentService;

    private void setCompetition() {
        Match match;
        match = new MatchBuilder(2).createMatch();
        //match = new MatchBuilder(players.length).createComputerWinMatch();
        //match = new MatchBuilder(players.length).createHumanWinMatch();
        // match = new MatchBuilder(players.length).createHumanWonMatch();
        competition = new Competition(match);
        // Set labels
        setFields(competition.getMatch());
    }

    @Override
    protected void initialize() {
        long t0 = System.currentTimeMillis();
        super.initialize();

        statusMessageUpdater = new StatusMessageUpdater();
        statusMessages.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                c.next();
                if (c.wasAdded()) {
                    System.out.println("statusMessages added = " + c);
                    if (!statusMessageUpdater.isRunning()) {
                        statusMessageUpdater.restart();
                    }
                }
            }
        });

        networkOpponentService = new NetworkOpponentService(this);
        networkOpponentService.setBackoffStrategy(ScheduledService.EXPONENTIAL_BACKOFF_STRATEGY);

        setCompetition();
        // setup opponent selector
        opponentSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Do new setup
            try {
                switch (newValue.type) {
                    case OpponentChoice.CLIENT:
                        networkOpponentService.setConnectToNewHost(newValue.host);
                        networkOpponentService.restart();
                        break;
                    default:
                        statusMessages.add("Opponent not implemented");
                        throw new UnsupportedOperationException("Opponent not implemented");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusMessages.add("Could not change opponent");
            }
        });
        opponentSelector.getItems().addAll(
            new OpponentChoice("localhost", "Localhost", OpponentChoice.CLIENT),
            new OpponentChoice("vandenbrand.eu", "Strom NL", OpponentChoice.CLIENT),
                new OpponentChoice("18yo959058.51mypc.cn", "Dennis China", OpponentChoice.CLIENT),
                new OpponentChoice(null, "Host a game", OpponentChoice.HOST)
        );
        opponentSelector.getSelectionModel().selectFirst();
        long t1 = System.currentTimeMillis();
        System.out.println("Initialized in " + (t1 - t0) + " ms");
    }

    @Override
    public void onConnectedTo(String host) {
        connectingProgressIndicator.setVisible(false);
        opponentText.setText("Connected to:");
        System.out.println("Connected to " + host);
    }

    @Override
    public void onCannotConnectTo(String host, Throwable e) {
        connectingProgressIndicator.setVisible(false);
        opponentText.setText("Cannot connect to:");
        System.out.println("Could not connect to " + host);
        if (e != null) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnecting() {
        connectingProgressIndicator.setVisible(true);
        opponentText.setText("Connecting to:");
    }

    @Override
    public void onWaitingForNetwork() {
        connectingProgressIndicator.setVisible(true);
    }

    public void doMove(ActionEvent actionEvent) {
        executeMove();
    }

    private void executeMove() {
        if (!executing.compareAndSet(false, true)) {
            System.err.println("Still executing");
            return;
        }
        if (selectedMove.get() == -1) {
            System.err.println("No moveNow set");
            executing.set(false);
            return;
        }
//        if (networkOpponentService.getLastValue() == null) {
//            System.err.println("No opponent set");
//            executing.set(false);
//            return;
//        }

        // Do human moveNow
        final int[] preHumanBoardState = competition.getFields();
        int gain = competition.move(selectedMove.get());
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
        animatorService.resetTo(
                selectedMove, humanMove, false, preHumanBoardState,
                competition.getMatch()
        );
        if (winMsg != null) {
            animatorService.setOnSucceeded(event -> {
                statusMessages.add(winMsg);
                executing.set(false);
            });
        }
        animatorService.start();
        statusMessages.add(ANIMATING_PLAYER_TURN_MSG);
        if (winMsg != null) {
            return;
        }

        // Start network request in the background
        final int[] preNetworkBoardState = competition.getFields();
        networkOpponentService.setRequestMove(competition.getMatch(), (networkMove) -> {
            opponentText.setText(CONNECTED_TO_MSG);
            //TODO: retryButton.setVisible(false);

            // After the human animation follows the network animation
            EventHandler<WorkerStateEvent> onHumanAnimationCompleted = event1 -> {
                try {
                    competition.move(networkMove);
                    animatorService.resetTo(
                            opponentSelectedMove, networkMove, true, preNetworkBoardState,
                            competition.getMatch());
                    animatorService.setOnSucceeded(e -> {
                        executing.set(false);
                        statusMessages.add(PLAYER_TURN_MSG);
                    });
                    animatorService.start();
                    statusMessages.add(EXECUTING_OPPONENT_TURN_MSG);
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
        animatorService.setOnSucceeded(event -> statusMessages.add(WAITING_FOR_OPPONENT_TURN_MSG));
        networkOpponentService.restart();
    }

    @Override
    void clickedField(int index) {
        if (competition.canMove(index)) {
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

    @Override
    public void close() {
        statusMessageUpdater.cancel();
        if (networkOpponentService != null)
            networkOpponentService.close();
    }

    public void clickedRetry(ActionEvent actionEvent) {
        networkOpponentService.restart();
        // What happens with onSucceded?
    }

    public void restartClicked(ActionEvent actionEvent) {
        if (alert == null) {
            alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to restart the game?");
            alert.setHeaderText(null);
        }
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                if (executing.get()) {
                    System.err.println("Not possible while executing");
                    return;
                }
                setCompetition();
            }
        });
    }

    private class StatusMessageUpdater extends ScheduledService {
        @Override
        protected Task createTask() {
            if (statusMessages.size() == 1) {
                String msg = statusMessages.get(0);
                statusText.setText(msg);
            }
            return new Task() {
                @Override
                protected Object call() throws Exception {
                    synchronized (StatusMessageUpdater.this) {
                        StatusMessageUpdater.this.wait(50);
                    }
                    return null;
                }
            };
        }

        @Override
        protected void succeeded() {
            // Update
            String msg;
            if (statusMessages.size() >= 1) {
                if (statusMessages.size() == 1) {
                    msg = statusMessages.get(0);
                } else {
                    msg = statusMessages.remove(0);
                }
                statusText.setText(msg);
            }
            if (statusMessages.size() <= 1) {
                cancel();
            }
            super.succeeded();
        }
    }
}
