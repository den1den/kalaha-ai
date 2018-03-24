package talf.jfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import talf.ai.AiSolverB;
import talf.mechanics.Match;
import talf.mechanics.Move;
import talf.mechanics.board.BoardState;
import talf.mechanics.board.BoardStateConstructor;

public class MatchController {
    public BorderPane rootPane;
    public Text statusText;
    AiSolverB ai = new AiSolverB();
    BoardPane boardPane;
    boolean aiIsSilver = false;
    boolean aiStarts = true;
    private Match match;

    @FXML
    public void initialize() {
        BoardState boardState = BoardStateConstructor.breakthru();
        boardState = BoardStateConstructor.simpleBoardState();
        boardState = BoardStateConstructor.verySimpleBoardState();

        boolean silverFirst = (aiStarts && aiIsSilver) || (!aiStarts && !aiIsSilver);
        match = new Match(boardState, silverFirst);

        boardPane = new BoardPane(match, true, true);
        boardPane.setOnMoved(() -> {
            if (isAiTurn()) {
                Platform.runLater(MatchController.this::doAi);
            }
        });
        rootPane.setCenter(boardPane);

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
        boardPane.updatePieces();
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
