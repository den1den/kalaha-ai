package talf;

import javafx.application.Platform;
import javafx.concurrent.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import talf.ai.AiSolverB;
import talf.jfx.BoardPaneWithStatus;
import talf.mechanics.Match;
import talf.mechanics.Move;
import talf.mechanics.board.BoardModel;
import talf.mechanics.board.BoardStateConstructor;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Application extends javafx.application.Application {
    Match match;
    BoardPaneWithStatus boardPaneController;

    boolean playerFirst = false;
    boolean playerIsSilver = true;

    AiCalculatorService aiCalculatorService;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        aiCalculatorService = new AiCalculatorService();
        aiCalculatorService.setSolver(new AiSolverB());
        aiCalculatorService.setOnScheduled(this::computerDone);

        this.primaryStage = primaryStage;
        initBoard();
    }

    private void initBoard() {
        BoardModel state = BoardStateConstructor.breakthru();
        match = new Match(state, playerIsSilver == playerFirst);
        boardPaneController = new BoardPaneWithStatus(match, playerIsSilver, !playerIsSilver);

        Scene s = new Scene(boardPaneController);
        primaryStage.setScene(s);
        primaryStage.show();

        aiGame();
    }

    private void computerDone(WorkerStateEvent event) {
        AiCalculatorService.Result result = aiCalculatorService.getValue();
        if(result == null) {
            aiCalculatorService.cancel();
            return;
        }
        if(result.status == AiCalculatorService.Result.COMPLETE) {
            Move move = result.resultingMove;
            match.move(move);
            if (!match.isFirsTurn()) {
                aiCalculatorService.start();
            } else {

            }
        }
    }

    private void reset() {
        initBoard();
    }

    private void aiGame() {
        match.turnSilverProperty().addListener((o, old, isSilver) -> {
            if (isSilver == null) return;
            if (isSilver == playerIsSilver) {
                // Players turn
            } else {
                // Player not turn
                aiCalculatorService.cancel();
                aiCalculatorService.reset();
                aiCalculatorService.setMatch(match);
                aiCalculatorService.start();
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
//                                s.setRoot(new BoardView(match));
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
//                                s.setRoot(new BoardView(match));
//                                System.out.println("win after "+match.getTurns());
//                                reset();
//                                wins.getAndIncrement();
//                                break tsk;
//                            }
                        }
//                        s.setRoot(new BoardView(match, canMoveSilver, canMoveGold));
                    }
                    long t1 = System.currentTimeMillis();
                    //System.out.println("t1-t0 = " + (t1 - t0));
                });
            }
        };
        //t.schedule(task, 0);
        t.scheduleAtFixedRate(task, 0, 1000);
    }

    private static class AiCalculatorService extends Service<AiCalculatorService.Result> {

        Match match;
        AiSolverB solver;

        public void setSolver(AiSolverB solver) {
            this.solver = solver;
        }

        public void setMatch(Match match) {
            this.match = match;
        }

        @Override
        protected Task<Result> createTask() {
            final Match match = this.match;
            final AiSolverB solver = this.solver;
            return new Task<>() {
                @Override
                protected Result call() throws Exception {
                    Move move = solver.solve(match);
                    return new Result(Result.COMPLETE, move);
                }
            };
        }

        static class Result {
            public static final int COMPLETE = 0;
            private final int status;
            private final Move resultingMove;

            public Result(int status, Move resultingMove) {
                this.status = status;
                this.resultingMove = resultingMove;
            }
        }
    }
}
