package talf;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import talf.ai.AiSolverB;
import talf.jfx.BoardPaneWithStatus;
import talf.mechanics.Match;
import talf.mechanics.Move;
import talf.mechanics.board.BoardState;
import talf.mechanics.board.BoardStateConstructor;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Application extends javafx.application.Application {
    BoardState state;
    Match match;
    Scene s;
    BoardPaneWithStatus boardPaneController;

    boolean playerFirst = true;
    boolean playerIsSilver = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        state = BoardStateConstructor.breakthru();
        match = new Match(state, playerIsSilver == playerFirst);
        boardPaneController = new BoardPaneWithStatus(match, playerIsSilver, !playerIsSilver);

        s = new Scene(boardPaneController);
        primaryStage.setScene(s);
        primaryStage.show();

        aiGame();
    }

    private void reset() {
        state = BoardStateConstructor.breakthru();
        match.reset(state, true);
    }

    private void aiGame() {
        match.turnSilverProperty().addListener((o, old, isSilver) -> {
            if (isSilver == null) return;
            if (isSilver == playerIsSilver) {
                // Players turn
            } else {
                // Player not turn
                AiSolverB solverB = new AiSolverB();
                Move move = solverB.solve(match);
                match.move(move);
                if (!match.isFirsTurn()) {
                    move = solverB.solve(match);
                    match.move(move);
                }
            }
        });
    }

    private void atomatic() {
        Random rand = new Random(798321);
        Timer t = new Timer();

        AtomicInteger wins = new AtomicInteger();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    long t0 = System.currentTimeMillis();
                    tsk:
                    {
                        for (int i = 0; i < 1500; i++) {
//                            Map<Coordinate, List<Coordinate>> allMoves = new IdentityHashMap<>();
//                            match.getAllMoves(allMoves);
//                            ArrayList<Coordinate> pieces = new ArrayList<>(allMoves.keySet());
//                            if (pieces.isEmpty()) {
//                                s.setRoot(new BoardPane(match));
//                                System.out.println("done");
//                                reset();
//                                break tsk;
//                            }
//                            Collections.sort(pieces);
//                            Coordinate piece = pieces.get(rand.nextInt(pieces.size()));
//                            List<Coordinate> moves = allMoves.get(piece);
//                            Collections.sort(moves);
//                            Coordinate move = moves.get(rand.nextInt(moves.size()));
//
//                            int r = match.move(piece, move);
//                            if(r == Integer.MAX_VALUE){
//                                s.setRoot(new BoardPane(match));
//                                System.out.println("win after "+match.getTurns());
//                                reset();
//                                wins.getAndIncrement();
//                                break tsk;
//                            }
                        }
//                        s.setRoot(new BoardPane(match, canMoveSilver, canMoveGold));
                    }
                    long t1 = System.currentTimeMillis();
                    //System.out.println("t1-t0 = " + (t1 - t0));
                });
            }
        };
        //t.schedule(task, 0);
        t.scheduleAtFixedRate(task, 0, 1000);
    }
}
