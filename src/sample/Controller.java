package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import nl.lexram.Match;
import nl.lexram.Player;
import nl.lexram.State;

public class Controller {
    public Label turn;

    final Match match = Match.construct();
    public Canvas drawArea;
    int turnI = 0;

    public void doStart(ActionEvent actionEvent) {
        turn.setText("Turn: " + turnI);
        // Run the task in a background thread
        Thread backgroundThread = new Thread(new TurnTask(turnI));
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
    }

    class TurnTask extends Task<State.Move> {
        final int turn;

        public TurnTask(int turn) {
            this.turn = turn;
        }

        @Override
        protected nl.lexram.State.Move call() throws Exception {
            int color = turn % match.players();
            Player player = match.getPlayer(color);
            System.out.println(player);
            nl.lexram.State.Move m = match.doMove(player, color);
            final String newText = "Turn: " + turnI + ", " + m;
            Platform.runLater(() -> {
                Controller.this.turn.setText(newText);
            });
            turnI++;
            return m;
        }
    }

    public void drawBoard(int[] board) {
        this.drawArea.
    }
}
