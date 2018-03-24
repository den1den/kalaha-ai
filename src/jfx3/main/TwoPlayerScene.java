package jfx3.main;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import jfx3.App;
import jfx3.components.BoardAnimator;
import jfx3.components.BoardDrawer;
import jfx3.components.PaneManager;
import jfx3.components.StatusTextQueue1;
import jfx3.opponent.AiManager;
import jfx3.opponent.Result;
import marblegame.gamemechanics.BoardState;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;

public class TwoPlayerScene extends PaneManager.PaneTab {

    public Text leftScoreText;
    public GridPane gridpane;
    public Text rightScoreText;
    public Text playerNameText;
    public ChoiceBox<Integer> difficultyChoiceBox;
    public ChoiceBox<Long> animationSpeedChoiceBox;
    public Text opponentNameText;
    public Text statusText;
    private StatusTextQueue1 statusTextQueue;
    private BoardDrawer drawer;
    private BoardAnimator animator;

    private boolean automatic = false; // DEBUGGING

    @FXML
    void initialize() {
        // setup local components
        statusTextQueue = new StatusTextQueue1(statusText.textProperty());

        drawer = new BoardDrawer(gridpane, leftScoreText, rightScoreText);

        animator = new BoardAnimator(drawer);
        animator.animationTimeout.bind(animationSpeedChoiceBox.valueProperty());

        gridpane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                case SPACE:
                    doMove();
            }
        });

        // fill defaults
        animationSpeedChoiceBox.getItems().addAll(
            750L,
            350L,
            100L,
            2000L,
            0L
        );
        animationSpeedChoiceBox.getSelectionModel().selectFirst();

        difficultyChoiceBox.getItems().addAll(
            2,
            3,
            4,
            5
        );
        difficultyChoiceBox.getSelectionModel().selectLast();

        System.out.println(getClass() + " initialized");
    }

    @Override
    public void onBind() {
        // Bind the scene to the game mechanics
        playerNameText.textProperty().bind(app.playerName);
        opponentNameText.textProperty().bind(app.opponentName);
        app.aiManager.depth.bind(difficultyChoiceBox.valueProperty());
        app.match.addListener(this::matchChanged);
        app.opponentType.addListener(this::setupOpponent);

        // TextQueue is not updating on App Thread so this is not possible:
        // app.windowTitle.bind(statusText.textProperty());
    }

    private void matchChanged(Observable ignore, Match old, Match m) {
        if (old != null) {
            app.turn.removeListener(this::turnChanged);
        }
        app.turn.set(false);
        if (m != null) {
            drawer.draw(m.getBoardState());
            drawer.bindSelection(m::canMove, ()
                -> statusTextQueue.queue("Cannot do that move"));
            app.turn.addListener(this::turnChanged);
            app.turn.set(true);
        }
    }

    private void setupOpponent(ObservableValue ignore, App.OpponentType oldValue, App.OpponentType newValue) {
        //TODO Add server opponent?
        System.out.println("ignore = [" + ignore + "], oldValue = [" + oldValue + "], newValue = [" + newValue + "]");
        if (newValue != App.OpponentType.HOST) {
            if (app.match.get() == null || app.match.get().isPad()) {
                app.match.setValue(new MatchBuilder(2).createMatch());
            }
        }
    }

    private void turnChanged(Observable o, Boolean old, Boolean turn) {
        if (app.opponentType.get() == App.OpponentType.LOCAL) {
            if (turn) {
                statusTextQueue.setText(app.playerName.get() + " its turn");
            } else {
                statusTextQueue.setText(app.opponentName.get() + " its turn");
            }
        } else if (app.opponentType.get() == App.OpponentType.CLIENT
            || app.opponentType.get() == App.OpponentType.COMPUTER) {
            if (turn) {
                statusTextQueue.setText("Its your turn");
            } else {
                statusTextQueue.setText("Waiting for opponent");
            }
        } else if (app.opponentType.get() == App.OpponentType.HOST) {
            if (turn) {
                statusTextQueue.setText("Its your turn");
            } else {
                statusTextQueue.setText("Waiting for opponent");
            }
        } else throw new
            UnsupportedOperationException();
    }

    @Override
    public void activeImpl() {
        gridpane.requestFocus();
    }

    @Override
    public void deactivatedImpl() {
    }

    @Override
    public void activatedImpl() {
        drawer.listenHighlightFields();
        if (!statusTextQueue.isRunning()) {
            statusTextQueue.start();
        }
    }

    @Override
    public void closeImpl() {
        statusTextQueue.close();
    }

    public void doMove(ActionEvent event) {
        doMove();
    }

    private void doMove() {
        App.OpponentType type = app.opponentType.get();
        if (type == App.OpponentType.LOCAL) {
            int move = drawer.selectionField.get();
            Match match = app.match.get();
            if (!match.canMove(move)) {
                statusTextQueue.queue("You cannot do that move");
                return;
            }
            drawer.unSelect();

            {
                BoardState pre = match.getBoardState();
                int win = match.move(move);
                System.out.println("win = " + win);
                animator.moveHuman(
                    move, pre, win > 0, match.getBoardState(),
                    false, this::nextTurn
                );
            }
            return;
        } else if (type == App.OpponentType.COMPUTER) {
            if (!app.turn.get()) {
                statusTextQueue.queue("Not your turn");
            }
            int move = drawer.selectionField.get();
            Match match = app.match.get();
            if (!match.canMove(move)) {
                statusTextQueue.queue("You cannot do that move");
                return;
            }
            drawer.unSelect();

            {
                BoardState pre = match.getBoardState();
                int win = match.move(move);
                System.out.println("win = " + win);
                animator.moveHuman(
                    move, pre, win > 0, match.getBoardState(),
                    true, this::nextTurn
                );
            }

            AiManager aiManager = app.aiManager;
            aiManager.cancel();
            aiManager.setMatch(match);
            aiManager.setOnSucceeded((e) -> {
                Result r = aiManager.getValue();
                if (r.isGiveUp()) {
                    statusTextQueue.setText("Opponent gave up, you won!");
                    aiManager.cancel();
                    return;
                }
                BoardState pre = match.getBoardState();
                int ai_win = match.move(r.move);
                System.out.println("ai_win = " + ai_win);
                if (ai_win != 0) {
                    statusTextQueue.queue("Opponent made a winning move of " + ai_win);
                }
                animator.moveComputer(
                    r.move, pre, ai_win > 0, match.getBoardState(), false,
                    () -> {
                        drawer.selectionField.set(match.nextPossibleMove());
                        app.turn.setValue(true);
                        if (automatic) {
                            drawer.selectionField.set(match.nextPossibleMove());
                            Platform.runLater(this::doMove);
                        }
                    }
                );
            });
            aiManager.restart();

            return;
        }

        int humanMove = drawer.selectionField.get();
        Match match = app.match.get();
        if (!app.turn.get()) {
            statusTextQueue.queue("Turn of " + app.playerName.get());
            return;
        }
        if (!match.canMove(humanMove)) {
            statusTextQueue.queue("You cannot do that move");
            return;
        }
        drawer.unSelect();

        {
            BoardState pre = match.getBoardState();
            int diff = match.move(humanMove);
            animator.moveHuman(
                humanMove, pre, diff > 0, match.getBoardState(),
                true, () -> app.turn.setValue(false)
            );
        }

        AiManager aiManager = app.aiManager;
        aiManager.cancel();
        aiManager.setMatch(match);
        aiManager.setOnSucceeded((e) -> {
            Result r = aiManager.getValue();
            if (r.isGiveUp()) {
                statusTextQueue.setText("Opponent gave up, you won!");
                aiManager.cancel();
                return;
            }
            BoardState pre = match.getBoardState();
            int res = match.move(r.move);
            if (res != 0) {
                statusTextQueue.queue("Opponent made a winning move of " + res);
            }
            animator.moveComputer(
                r.move, pre, res > 0, match.getBoardState(), false,
                () -> {
                    drawer.selectionField.set(match.nextPossibleMove());
                    app.turn.setValue(true);
                    if (automatic && false) {
                        drawer.selectionField.set(match.nextPossibleMove());
                        Platform.runLater(() -> {
                            doMove();
                        });
                    }
                }
            );
        });
        aiManager.restart();
    }

    private void nextTurn() {
        app.turn.setValue(!app.turn.get());
    }
}
