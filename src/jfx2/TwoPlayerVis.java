package jfx2;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import marblegame.gamemechanics.Match;

public abstract class TwoPlayerVis implements Controller {
    private static String FIELD_INDEX_PROPERTY_KEY = "field index";
    public GridPane gridpane;
    public Text leftScoreText;
    public Text rightScoreText;
    static Paint HOVER_PAINT = Color.RED;
    static Paint NORMAL_PAINT = Color.BLACK;
    public Text statusText;
    IntegerProperty hoverField = new SimpleIntegerProperty(-1);

    @FXML
    void initialize() {
        // setup buttons
        ObservableList<Node> fields = gridpane.getChildren();
        int fieldIndex = 0;
        for (Node child : fields) {
            child.getProperties().put(FIELD_INDEX_PROPERTY_KEY, fieldIndex);
            fieldIndex++;
        }
        hoverField.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue.intValue() != -1) {
                    getShape(oldValue.intValue()).setStroke(NORMAL_PAINT);
                }
                if (newValue.intValue() != -1) {
                    getShape(newValue.intValue()).setStroke(HOVER_PAINT);
                }
            }
        });

    }

    public void setFields(int[] fields) {
        for (int i = 0; i < fields.length; i++) {
            setField(i, fields[i]);
        }
    }

    public void setFields(Match m) {
        int[] fields = m.getFieldsCopy();
        setFields(fields);
        leftScoreText.setText(getPointsDisplay("Computer", 1, m));
        rightScoreText.setText(getPointsDisplay("Human", 0, m));
    }

    void resetField() {
        leftScoreText.setText("");
        rightScoreText.setText("");
        for (Node n : this.gridpane.getChildren()) {
            Text txt = (Text) ((Pane) n).getChildren().get(1);
            txt.setText("");
        }
    }

    void setField(int index, int number) {
        // System.out.println("index = [" + index + "], number = [" + number + "]");
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        Text shape = (Text) selectedParent.getChildren().get(1);
        shape.setText(String.valueOf(number));
    }

    String getPointsDisplay(String name, int index, Match m) {
        int pointsToWin = m.getPointsToWin(index);
        if (pointsToWin > 6) {
            return name + ": " + m.getPoints(index);
        } else if (pointsToWin <= 0) {
            return name + ": " + m.getPoints(index) + "\n" + "Winner!";
        } else {
            return name + ": " + m.getPoints(index) + "\n" + "(Needs +" + pointsToWin + " to win)";
        }
    }

    Circle getShape(int index) {
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        return (Circle) selectedParent.getChildren().get(0);
    }

    public void clickedField(MouseEvent mouseEvent) {
        Node source = (Node) mouseEvent.getSource();
        if (this.gridpane.equals(source.getParent())) {
            int index = (int) source.getProperties().get(FIELD_INDEX_PROPERTY_KEY);
            clickedField(index);
        }
    }

    public void enterField(MouseEvent mouseEvent) {
        Pane source = (Pane) mouseEvent.getSource();
        if (this.gridpane.equals(source.getParent())) {
            int index = (int) source.getProperties().get(FIELD_INDEX_PROPERTY_KEY);
            Text shape = (Text) source.getChildren().get(1);
            int value = Integer.valueOf(shape.getText());
            enterField(index, value);
        }
    }

    private void enterField(int index, int value) {
        hoverField.set((index + value) % gridpane.getChildren().size());
    }

    abstract void clickedField(int index);

    @Override
    public void close() {
    }
}
