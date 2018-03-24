package jfx3.components;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import jfx3.App;

import java.util.ArrayList;
import java.util.List;

public class PaneManager extends AnchorPane {
    private List<PaneTab> tabs = new ArrayList<>();
    private PaneTransition transition = new PaneTransition();

    public PaneManager() {
        //super(30);
        heightProperty().addListener(this::heightChanged);
    }

    public void activateFirstPane() {
        tabs.get(0).activeImpl();
        tabs.get(0).activatedImpl();
    }

    public void addPane(Node n, PaneTab tab) {
        tabs.add(tab);
        getChildren().add(n);
    }

    public void nextPane() {
        transition.play();
    }

    public boolean isFirst() {
        return transition.frac == 0;
    }

    public void bind(App app) {
        // Enlarge the root of the loaded element to the scene size
        for (Node c : getChildren()) {
            Pane child = (Pane) c;
            child.prefHeightProperty().bind(heightProperty());
            child.prefWidthProperty().bind(widthProperty());
        }
        for (PaneTab t : tabs) {
            t.bind(app);
        }
        System.out.println("All panes binded...");
    }

    private void heightChanged(ObservableValue o, Number oldValue, Number newValue) {
        transition.total_height = (double) newValue;
        transition.set();
    }

    public void closePanes() {
        for (PaneTab t : tabs) {
            t.closeImpl();
        }
    }

    public static abstract class PaneTab {
        protected App app;

        void bind(App app) {
            this.app = app;
            onBind();
        }

        public void closeImpl() {
        }

        public void activatedImpl() {
        }

        public void activeImpl() {
        }

        public void deactivatedImpl() {
        }

        /**
         * Bind to this.app
         */
        public abstract void onBind();
    }


    private class PaneTransition extends Transition {
        private double frac;
        private double total_height;

        {
            setCycleDuration(Duration.millis(400));
            setOnFinished(this::finished);
            setInterpolator(Interpolator.SPLINE(0.75, 0, 0.25, 1));
        }

        private void set() {
            AnchorPane.setTopAnchor(getChildren().get(0), -total_height * frac);
            AnchorPane.setTopAnchor(getChildren().get(1), total_height * (1.0 - frac));
        }

        @Override
        protected void interpolate(double frac) {
            this.frac = frac;
            set();
        }

        private void finished(ActionEvent finished) {
            set();
            double r = getRate();
            setRate(-r);
            if (r > 0) {
                tabs.get(0).deactivatedImpl();
                tabs.get(1).activatedImpl();
            } else {
                tabs.get(0).activatedImpl();
                tabs.get(1).deactivatedImpl();
            }
        }

        @Override
        public void play() {
            if (getRate() > 0) {
                tabs.get(1).activeImpl();
            } else {
                tabs.get(0).activeImpl();
            }
            super.play();
        }
    }
}
