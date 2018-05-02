package talf.jfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import talf.ai.AiSolverB;
import talf.mechanics.Coordinate;
import talf.mechanics.Match;
import talf.mechanics.Move;
import talf.mechanics.board.BoardModel;
import talf.mechanics.board.BoardStateConstructor;

public class MatchController {
    public BorderPane rootPane;
    public Text statusText;
    AiSolverB ai = new AiSolverB();
    boolean aiIsSilver = false;
    boolean aiStarts = true;
    private Match match;

    private BoardController boardController;


    private void move(Coordinate source, Coordinate target) {
        int win = match.move(source, target);
        if (win > 0) {
            System.out.println("win = " + win);
        }
        BorderPane s = cells[source.x][source.y];
        BorderPane t = cells[target.x][target.y];

        Node n = s.getCenter();
        s.setCenter(null);
        t.setCenter(n);

        BoardView.PieceHandler pieceHandler = (BoardView.PieceHandler) n.getOnMouseClicked();
        pieceHandler.c = target;

        onMoved();
    }

    @FXML
    public void initialize() {
        BoardModel boardModel = BoardStateConstructor.breakthru();
        boardModel = BoardStateConstructor.simpleBoardState();
        boardModel = BoardStateConstructor.verySimpleBoardState();

        boolean silverFirst = (aiStarts && aiIsSilver) || (!aiStarts && !aiIsSilver);

        match = new Match(boardModel, silverFirst);
        boolean canMoveSilver = true;
        boolean canMoveGold = true;
        boardController = new BoardController(match.board, canMoveSilver, canMoveGold);

        boardController.setOnMoved(() -> {
            if (isAiTurn()) {
                Platform.runLater(MatchController.this::doAi);
            }
        });
        rootPane.setCenter(boardView);

        if (isAiTurn())
            doAi();
    }

    private boolean isAiTurn() {
        return (aiIsSilver && match.isTurnSilver())
            || (!aiIsSilver && match.isTurnGold());
    }

    private void doAi() {
        if (!match.isTurnGold()) {
            throw new UnsupportedOperationException("AI mechanics not changed yet");
        }
        Move aiMove = ai.solve(match);
        match.move(aiMove);
        if (match.isTurnGold()) {
            aiMove = ai.solve(match);
            match.move(aiMove);
            assert !match.isTurnSilver();
        }
        boardView.updatePieces();
    }

    public static class App extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(
                MatchController.class.getResource("Match.fxml")
            );
            Parent root = loader.load();
            MatchController controller = loader.getController();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        }
    }
}
