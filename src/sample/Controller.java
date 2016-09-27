package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import nl.lexram.Match;
import nl.lexram.Player;
import nl.lexram.State;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Controller {
    private final Match match = Match.construct();
    public Label playerLabel;
    public Label yourPointsLabel;
    public Label computerPointsLabel;
    private Player.Default human = (Player.Default) match.getPlayer(0);
    State.Move humanMove = null;
    State.Move computerMove = null;

    public GridPane drawPane;

    public Executor e = Executors.newSingleThreadExecutor();

    public void clickOk(ActionEvent actionEvent) {
        if (this.drawPane.getChildren().isEmpty()) {
            drawBoard(match.getState());
            return;
        }
        e.execute(new TurnRunnable());
    }

    class TurnRunnable implements Runnable {

        @Override
        public void run() {
            humanMove = match.doNextMove();
            final String playerLabelText0 = "Computer";
            final State state0 = match.getState();
            Platform.runLater(() -> {
                Controller.this.playerLabel.setText(playerLabelText0);
                drawBoard(state0);
            });

            long t0 = System.currentTimeMillis();
            computerMove = match.doNextMove();
            final String playerLabelText1 = "Human";
            final State state1 = match.getState();
            waitUntil(t0 + 2000);
            Platform.runLater(() -> {
                Controller.this.playerLabel.setText(playerLabelText1);
                drawBoard(state1);
            });
        }
    }

    private void waitUntil(long t1) {

        long t0 = System.currentTimeMillis();
        while (t0 < t1) {
            long dt = t1 - t0;
            try {
                synchronized (this) {
                    wait(dt);
                }
            } catch (InterruptedException e1) {
            }
            t0 = System.currentTimeMillis();
        }
    }

    public void drawBoard(State state) {
        int[] board = state.getBoard();
        System.out.println("Board: " + Arrays.toString(board));
        double radius = 15;
        Paint notSelectedFill = Color.GRAY;
        Paint selectedFill = Color.GRAY.brighter();
        Paint computerChanged = Color.GRAY.interpolate(Color.ALICEBLUE, 0.2);
        this.drawPane.getChildren().clear();
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 6; col++) {
                final int index;
                if (row == 0) {
                    index = 6 + (6 - 1 - col);
                } else {
                    index = col;
                }
                int cellValue = board[index];
                boolean selected = human.selectedMove() == index;
                Paint fill;
                Paint border = null;
                if (selected) {
                    fill = selectedFill;
                } else if (computerMove != null && computerMove.affects(index)) {
                    fill = computerChanged;
                    if (index == computerMove.getMoveIndex()) {
                        border = Color.BLACK;
                    }
                } else {
                    fill = notSelectedFill;
                }
                final Circle circle = new Circle(radius, fill);
                if (border != null) {
                    circle.setStroke(border);
                }
                StackPane node = new StackPane(circle, new Label("" + cellValue));
                node.addEventFilter(MouseEvent.MOUSE_CLICKED, (e -> {
                    if (index > 6) {
                        return;
                    }
                    human.setMove(index);
                    for (Node n : this.drawPane.getChildren()) {
                        Circle c = (Circle) ((StackPane) n).getChildren().get(0);
                        c.setFill(notSelectedFill);
                    }
                    circle.setFill(selectedFill);
                }));
                this.drawPane.add(node, col, row);
            }
        }
        yourPointsLabel.setText(String.valueOf(state.points(0)));
        computerPointsLabel.setText(String.valueOf(state.points(1)));
    }
}
