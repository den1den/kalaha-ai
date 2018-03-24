package jfx3.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import marblegame.gamemechanics.BoardState;

import java.util.function.Predicate;
import java.util.logging.Logger;

public class BoardDrawer {
    private static final Logger LOGGER = Logger.getGlobal();
    private static Color DEFAULT_COLOR = Color.BLACK;
    private static Color HOVER_PAINT = Color.rgb(0, 144, 0);
    private static Color NORMAL_PAINT = Color.BLACK;
    public final IntegerProperty selectionField = new SimpleIntegerProperty(-1);
    private final IntegerProperty clickedField = new SimpleIntegerProperty(-1);
    private final IntegerProperty hoverField = new SimpleIntegerProperty(-1);
    private GridPane gridpane;
    private Text[] scores;
    private Color selectionColor = HOVER_PAINT.desaturate();
    private Color highLightColor = HOVER_PAINT.brighter();
    private ChangeListener<Number> clickedListener = null;

    public BoardDrawer(GridPane gridpane,
                       Text... scores) {
        this.gridpane = gridpane;
        this.scores = scores;
        selectionField.addListener(this::onSelectionChanged);
        {
            // Click listeners
            int fieldIndex = 0;
            for (Node child : gridpane.getChildren()) {
                child.getProperties().put(this, fieldIndex);
                fieldIndex++;
                child.setOnMouseEntered(this::onMouseEntered);
                child.setOnMouseExited(this::onMouseExited);
                child.setOnMouseClicked(this::onMouseClicked);
            }
            gridpane.getParent().setOnMouseClicked(this::onMouseClicked);
        }
    }

    public void listenHighlightFields() {
        hoverField.addListener(this::onHoverChanged);
    }

    public void bindSelection(
        final Predicate<Integer> selectionCriteria,
        final Runnable onFailure
    ) {
        if (clickedListener != null) {
            clickedField.removeListener(clickedListener);
        }
        clickedListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int clicked = newValue.intValue();
                if (clicked != -1) {
                    if (selectionCriteria.test(clicked)) {
                        selectionField.set(clicked);
                    } else {
                        System.err.println("Cannot select this field");
                        clicked = -1;
                        onFailure.run();
                    }
                }
                selectionField.set(clicked);
            }
        };
        clickedField.addListener(clickedListener);
    }

    public void unSelect() {
        selectionField.set(-1);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        Node source = (Node) mouseEvent.getSource();
        if (!this.gridpane.equals(source.getParent())) {
            clickedField.setValue(-1);
        } else {
            int index = (int) source.getProperties().get(this);
            clickedField.setValue(index);
            mouseEvent.consume();
        }
    }

    public void draw(BoardState boardState) {
        int[] fields = boardState.getFieldsCopy();
        for (int i = 0; i < fields.length; i++) {
            setField(i, fields[i]);
        }
        for (int i = 0; i < scores.length; i++) {
            String text = String.valueOf(boardState.getPlayerPoints(i));
            scores[i].setText(text);
        }
    }

    public void setField(int fieldIndex, int value) {
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(fieldIndex));
        Text shape = (Text) selectedParent.getChildren().get(1);
        shape.setText(String.valueOf(value));
    }

    private void onMouseExited(MouseEvent mouseEvent) {
        hoverField.set(-1);
    }

    private void onMouseEntered(MouseEvent mouseEvent) {
        Pane source = (Pane) mouseEvent.getSource();
        if (!this.gridpane.equals(source.getParent())) {
            System.out.println("ignored: " + mouseEvent);
            return;
        }

        int index = (int) source.getProperties().get(this);
        Text shape = (Text) source.getChildren().get(1);
        String text = shape.getText();
        try {
            int value = Integer.valueOf(text);
            hoverField.set((index + value) % gridpane.getChildren().size());
        } catch (NumberFormatException e) {
            hoverField.set(-1);
        }
    }

    private Shape getShape(int index) {
        Pane selectedParent = ((Pane) this.gridpane.getChildren().get(index));
        return (Circle) selectedParent.getChildren().get(0);
    }

    private void onHoverChanged(ObservableValue o, Number oldValue, Number newValue) {
        // System.out.println("onHoverChanged: oldValue = [" + oldValue + "], newValue = ["
        // + newValue + "]");
        if (oldValue.intValue() != -1) {
            Shape shape = getShape(oldValue.intValue());
            shape.setStroke(NORMAL_PAINT);
            shape.setStrokeWidth(1);
        }
        if (newValue.intValue() != -1) {
            Shape shape = getShape(newValue.intValue());
            shape.setStroke(HOVER_PAINT);
            shape.setStrokeWidth(5);
        }
    }

    private void onSelectionChanged(ObservableValue o, Number oldValue, Number newValue) {
        if (oldValue != null && oldValue.intValue() != -1) {
            getShape(oldValue.intValue()).setFill(DEFAULT_COLOR);
        }
        if (newValue.intValue() != -1) {
            getShape(newValue.intValue()).setFill(selectionColor);
        }
    }
}
