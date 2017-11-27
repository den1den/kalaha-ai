package javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import marblegame.Competition;
import marblegame.Match;
import marblegame.MatchBuilder;

/**
 * Created by dennis on 17-11-17.
 */
public abstract class ControllerTwoPlayers {
    protected static Paint defaultColor = Color.DODGERBLUE;
    static Paint selectedLeftColor = Color.BLUE;
    static Paint selectedRightColor = Color.RED;
    public GridPane gridpane;
    public Label leftScoreLabel;
    public Label rightScoreLabel;
    protected Competition competition;
    int leftScore = 0;
    int rightScore = 0;

    @FXML
    protected void initialize() {
        int i = 0;
        for (Node child :
                gridpane.getChildren()) {
            child.getProperties().put("field leftIndex", i++);
        }
        startNewGame();
        setLeftScoreText();
        setRightScoreText();
    }

    public void startNewGame(ActionEvent event) {
        startNewGame();
    }

    protected void startNewGame() {
        competition = createNewPlayers(new MatchBuilder().setPlayers(2).createMatch());
        setBoardText();
    }

    protected abstract Competition createNewPlayers(Match match);


    void setRightScoreText() {
        rightScoreLabel.setText(String.format("Human score: %d", rightScore));
    }

    void setLeftScoreText() {
        leftScoreLabel.setText(String.format("Ai score: %d", leftScore));
    }

    Shape getShape(int index) {
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        Circle shape = (Circle) selectedParent.getChildren().get(0);
        return shape;
    }

    protected void setBoardText() {
        int[] fields = competition.getMatch().getState().getFields();
        int i = 0;
        for (Node child :
                gridpane.getChildren()) {
            StackPane pane = (StackPane) child;
            Label label = (Label) pane.getChildren().get(1);
            label.setText(String.valueOf(fields[i++]));
        }
    }
}