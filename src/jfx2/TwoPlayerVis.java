package jfx2;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public abstract class TwoPlayerVis implements Controller {
    private static String FIELD_INDEX_PROPERTY_KEY = "field index";
    public GridPane gridpane;
    public Text opponentText;
    public Text leftScoreText;
    public Text rightScoreText;
    public Text statusTextLeft;

    @FXML
    void initialize() {
        // setup buttons
        ObservableList<Node> fields = gridpane.getChildren();
        int fieldIndex = 0;
        for (Node child : fields) {
            child.getProperties().put(FIELD_INDEX_PROPERTY_KEY, fieldIndex);
            fieldIndex++;
        }
    }

    void setFields(int[] fields, int[] points) {
        for (int i = 0; i < fields.length; i++) {
            setField(i, fields[i]);
        }
        leftScoreText.setText("Computer: " + points[1]);
        rightScoreText.setText("Human: " + points[0]);
    }

    void setField(int index, int number) {
        // System.out.println("index = [" + index + "], number = [" + number + "]");
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        Text shape = (Text) selectedParent.getChildren().get(1);
        shape.setText(String.valueOf(number));
    }

    Shape getShape(int index) {
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        Circle shape = (Circle) selectedParent.getChildren().get(0);
        return shape;
    }

    public void clickedField(MouseEvent event) {
        Node source = (Node) event.getSource();
        if (this.gridpane.equals(source.getParent())) {
            int index = (int) source.getProperties().get(FIELD_INDEX_PROPERTY_KEY);
            clickedField(index);
        }
    }

    abstract void clickedField(int index);

    @Override
    public void close() {
    }
}
